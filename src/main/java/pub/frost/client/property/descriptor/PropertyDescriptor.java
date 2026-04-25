package pub.frost.client.property.descriptor;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Described;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupHead;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyDescriptor implements Named, Described {
    @JSONField(name = "key")
    private final String key;
    @JSONField(name = "data")
    private final AbstractProperty property;
    @JSONField(name = "childList")
    private final List<PropertyDescriptor> childProperties;
    private transient final Supplier<Boolean> visibilitySupplier;

    public PropertyDescriptor(String key, AbstractProperty property) {
        this(key, property, null, property::isVisible);
    }
    public PropertyDescriptor(String key, List<PropertyDescriptor> childProperties, Supplier<Boolean> visibilitySupplier) {
        this(key, null, childProperties, visibilitySupplier);
    }

    public boolean isGroup() {
        return childProperties != null;
    }

    public boolean isVisible() {
        return visibilitySupplier.get();
    }

    // todo factory
    public static List<PropertyDescriptor> buildDescriptorListForObject(
            Object object,
            String keyPrefix, Function<String, String> keyProcessor
    ) {
        if (keyProcessor == null) keyProcessor = k -> k;
        int unnamedIndex = 0;

        final Stack<String> keyPrefixStack = new Stack<>();
        final Stack<Supplier<Boolean>> visibilityStack = new Stack<>();
        final Stack<List<PropertyDescriptor>> listStack = new Stack<>();

        final Stack<String> groupTranslationKeyStack = new Stack<>();
        final Stack<String> propTranslationKeyStack = new Stack<>();

        keyPrefixStack.push("");
        listStack.push(new ArrayList<>());

        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Deprecated.class)) continue;
            boolean startGroup = false;
            boolean endGroup = false;

            boolean propField = AbstractProperty.isPropertyField(field);
            Property anno = propField? field.getAnnotation(Property.class) : null;

            boolean insertObject = false;

            try {
                String groupKey = null;
                Supplier<Boolean> groupVisibility = null;

                if (propField) {
                    field.setAccessible(true);
                    groupKey = anno.startGroup();
                    startGroup = !StringUtils.isBlank(groupKey);
                    groupVisibility = () -> true;
                    endGroup = anno.endGroup();
                } else if (AbstractProperty.isGroupHead(field)) {
                    field.setAccessible(true);
                    PropertyGroupHead head = field.getAnnotation(PropertyGroupHead.class);
                    groupKey = head.value();
                    startGroup = !StringUtils.isBlank(groupKey);
                    groupVisibility = (Supplier<Boolean>) field.get(object);
                } else if (field.isAnnotationPresent(InsertProperty.class)) {
                    field.setAccessible(true);
                    String key = field.getAnnotation(InsertProperty.class).value();
                    boolean asGroup = !StringUtils.isBlank(key);
                    if (asGroup) {
                        // set basic flags
                        {
                            startGroup = true;
                            endGroup = true;
                            insertObject = true;
                        }
                        // set group data
                        {
                            groupKey = key;
                            // set visibility
                            {
                                Object instance = field.get(object);
                                Type[] typeArray = instance.getClass().getGenericInterfaces();
                                for (Type type : typeArray) {
                                    if (type instanceof ParameterizedType) {
                                        ParameterizedType parameterizedType = (ParameterizedType) type;
                                        if (parameterizedType.getRawType() == Supplier.class) {
                                            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                                            if (actualTypeArguments.length == 1 && actualTypeArguments[0] == Boolean.class) {
                                                groupVisibility = (Supplier<Boolean>) instance;
                                            }
                                        }
                                    }
                                }
                                if (groupVisibility == null) groupVisibility = () -> true;
                            }
                        }
                    }
                }

                {
                    TranslationKey translationKey = field.getAnnotation(TranslationKey.class);
                    String key = translationKey == null? "~" : translationKey.value();
                    if (startGroup) groupTranslationKeyStack.push(key);
                    if (propField) propTranslationKeyStack.push(key);
                }


                if (startGroup) {
                    keyPrefixStack.push(groupKey.toLowerCase());
                    visibilityStack.push(groupVisibility);
                    listStack.push(new ArrayList<>());
                }

                if (propField) {
                    AbstractProperty currentProp = (AbstractProperty) field.get(object);
                    if (anno.allowOverriding()) currentProp.enableOverriding();

                    String propKey = anno.value(); {
                        if (StringUtils.isBlank(propKey)) {
                            propKey = "unnamed-property-" + unnamedIndex;
                            unnamedIndex++;
                        }
                        String groupPrefix = keyPrefixStack.peek();
                        if (!StringUtils.isBlank(groupPrefix)) {
                            propKey = groupPrefix + ".subprops." + propKey;
                        }
                        propKey = keyProcessor.apply(keyPrefix + propKey).toLowerCase();
                    }

                    final String translationKey = propTranslationKeyStack.pop();
                    listStack.peek().add(new PropertyDescriptor(propKey, currentProp) {
                        @Override
                        public String getTranslationKey() {
                            return format(translationKey);
                        }
                    });
                }
                else if (insertObject) {
                    Object insertedObj = field.get(object);
                    if (insertedObj != null) {
                        String groupPrefix = keyPrefixStack.peek();
                        Function<String, String> finalKeyProcessor = keyProcessor;
                        List<PropertyDescriptor> objectProps = buildDescriptorListForObject(
                                insertedObj,
                                "", // 不要带 prefix
                                propKey -> {
                                    String prefix = keyPrefixStack.peek();
                                    if (!StringUtils.isBlank(prefix)) {
                                        propKey = finalKeyProcessor.apply(prefix + ".subprops." + propKey);
                                    }
                                    return keyPrefix + propKey;
                                }
                        );

                        listStack.peek().addAll(objectProps);
                    }
                }

                if (endGroup) {
                    List<PropertyDescriptor> poppedList = listStack.pop();
                    final String translationKey = groupTranslationKeyStack.pop();
                    listStack.peek().add(new PropertyDescriptor(
                            keyProcessor.apply(keyPrefix + keyPrefixStack.pop()),
                            poppedList,
                            visibilityStack.pop()
                    ) {
                        @Override
                        public String getTranslationKey() {
                            return format(translationKey);
                        }
                    });
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return listStack.pop();
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}
