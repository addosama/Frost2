package pub.frost.client.property.descriptor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import pub.frost.client.i18n.interfaces.Described;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupHead;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyDescriptor implements Named, Described {
    private final String key;
    private final AbstractProperty<?> property;
    private final List<PropertyDescriptor> childProperties;
    private final Supplier<Boolean> visibilitySupplier;

    public PropertyDescriptor(String key, AbstractProperty<?> property) {
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

    public static List<PropertyDescriptor> buildDescriptorListForObject(
            Object object,
            String keyPrefix, Function<String, String> keyProcessor
    ) {
        if (keyProcessor == null) keyProcessor = k -> k;
        int unnamedIndex = 0;

        final Stack<String> keyPrefixStack = new Stack<>();
        final Stack<Supplier<Boolean>> visibilityStack = new Stack<>();
        final Stack<List<PropertyDescriptor>> listStack = new Stack<>();

        keyPrefixStack.push("");
        listStack.push(new ArrayList<>());

        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Deprecated.class)) continue;
            boolean startGroup = false;
            boolean endGroup = false;

            boolean propField = AbstractProperty.isPropertyField(field);
            Property anno = propField? field.getAnnotation(Property.class) : null;

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
                }

                if (startGroup) {
                    keyPrefixStack.push(groupKey);
                    visibilityStack.push(groupVisibility);
                    listStack.push(new ArrayList<>());
                }

                if (propField) {
                    AbstractProperty<?> currentProp = (AbstractProperty<?>) field.get(object);
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
                    listStack.peek().add(new PropertyDescriptor(propKey, currentProp));
                }

                if (endGroup) {
                    List<PropertyDescriptor> poppedList = listStack.pop();
                    listStack.peek().add(new PropertyDescriptor(
                            keyProcessor.apply(keyPrefix + keyPrefixStack.pop()),
                            poppedList,
                            visibilityStack.pop()
                    ));
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
