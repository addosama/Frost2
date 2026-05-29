package pub.frost.client.property.descriptor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Localizable;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

@Accessors(chain = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyDescriptorFactory {
    private final Object target;
    public static PropertyDescriptorFactory createForObject(Object target) {
        return new PropertyDescriptorFactory(target);
    }

    @Setter private String translationKeyFormat = "~";
    @Setter private boolean skipDeprecated = true;
    @Setter private List<PropertyDescriptor> topLevelDescriptorList = new ArrayList<>();

    private List<PropertyDescriptor> descriptorList = null;

    private Map<String, PropertyDescriptor> descriptorMap = null;
    private Map<String, PropertyDescriptor> flattenedDescriptorMap = null;

    private final Deque<String> prefixDeque = new ArrayDeque<>();
    private final Map<String, List<PropertyDescriptor>> descriptorListMap = new HashMap<>();
    private final Map<String, AbstractProperty<?, ?>> groupMainPropMap = new HashMap<>();
    private final Map<String, VisibilitySupplier> groupVisibilityMap = new HashMap<>();

    public PropertyDescriptorFactory build() {
        descriptorListMap.put("", new ArrayList<>(topLevelDescriptorList));
        descriptorList = processObject(
                target,
                translationKeyFormat
        );

        return this;
    }

    private List<PropertyDescriptor> processObject(
            Object object,
            String translationKeyFormat
    ) {
        if (object instanceof ManualDescriptorProvider) {
            String keyPrefix = getCurrentPrefix();
            return ((ManualDescriptorProvider) object).provideDescriptors(
                    keyPrefix,
                    p -> groupMainPropMap.put(keyPrefix, p),
                    p -> p
            );
        }

        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                final boolean isPropertyField, isGroupHeadField, isInsertField;
                // field filter
                {
                    if (field.isAnnotationPresent(ExcludeProperty.class)) continue;
                    if (skipDeprecated && field.isAnnotationPresent(Deprecated.class)) continue;

                    isPropertyField = AbstractProperty.isPropertyField(field);
                    isGroupHeadField = AbstractProperty.isGroupHead(field);
                    isInsertField = field.isAnnotationPresent(InsertProperty.class);

                    if (!isPropertyField && !isGroupHeadField && !isInsertField) continue;
                }
                // make accessible
                field.setAccessible(true);

                boolean startGroup = false, endGroup = false;
                VisibilitySupplier groupVisibilitySupplier = null;

                if (isGroupHeadField) {
                    startGroup = true;
                    PropertyGroupHead annotation = field.getAnnotation(PropertyGroupHead.class);
                    prefixDeque.offerLast(annotation.value());
                    Supplier<Boolean> groupHead = (Supplier<Boolean>) field.get(object);
                    groupVisibilitySupplier = groupHead::get;
                }
                else if (isInsertField) {
                    InsertProperty annotation = field.getAnnotation(InsertProperty.class);
                    if (annotation.value() != null && !annotation.value().isEmpty()) {
                        startGroup = true;
                        endGroup = true;
                        prefixDeque.offerLast(annotation.value());
                        Object objInserting = field.get(object);
                        if (objInserting instanceof VisibilitySupplier)
                            groupVisibilitySupplier = (VisibilitySupplier) objInserting;
                    }
                }
                else {
                    Property annotation =  field.getAnnotation(Property.class);
                    if (annotation.startGroup() != null && !annotation.startGroup().isEmpty()) {
                        startGroup = true;
                        prefixDeque.offerLast(annotation.startGroup());
                    }
                    endGroup = annotation.endGroup();
                }

                String keyPrefix = getCurrentPrefix();

                if (startGroup) {
                    if (groupVisibilitySupplier == null) {
                        groupVisibilitySupplier = () -> true;
                    }
                    descriptorListMap.put(keyPrefix, new ArrayList<>());
                    groupMainPropMap.put(keyPrefix, null);
                    groupVisibilityMap.put(keyPrefix, groupVisibilitySupplier);
                }

                List<PropertyDescriptor> contextList = descriptorListMap.get(keyPrefix);

                String translationKey; {
                    TranslationKey annotation = field.getAnnotation(TranslationKey.class);
                    if (annotation != null) translationKey = annotation.value();
                    else translationKey = translationKeyFormat;
                }

                if (isPropertyField) {
                    AbstractProperty<?, ?> property = (AbstractProperty<?, ?>) field.get(object);
                    if (field.isAnnotationPresent(PropertyGroupMain.class)) {
                        groupMainPropMap.put(keyPrefix, property);
                    }
                    Property annotation = field.getAnnotation(Property.class);
                    String propKey = keyPrefix + annotation.value();
                    contextList.add(new PropertyDescriptor(
                            propKey,
                            property,
                            Localizable.format(translationKey, propKey)
                    ));
                } else if (isInsertField) {
                    processObject(field.get(object), translationKey);
                }

                if (endGroup) {
                    prefixDeque.pollLast();
                    String groupKey = keyPrefix.substring(0, keyPrefix.length() - 1);
                    descriptorListMap.get(getCurrentPrefix()).add(new PropertyDescriptor(
                            groupKey,
                            groupMainPropMap.get(keyPrefix),
                            contextList,
                            groupVisibilityMap.get(keyPrefix)::isVisible,
                            Localizable.format(translationKey, groupKey)
                    ));
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return descriptorListMap.get("");
    }

    private String getCurrentPrefix() {
        if (prefixDeque.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (String prefix : prefixDeque) {
            builder.append(prefix).append(".");
        }
        return builder.toString();
    }
    private boolean isBuilt() {
        return descriptorList != null;
    }
    private void assertBuilt() {
        FrostCore.debugAssert(isBuilt(), "call build() first");
    }

    public Map<String, PropertyDescriptor> getDescriptorMap() {
        assertBuilt();

        if (descriptorMap == null) {
            descriptorMap = new LinkedHashMap<>();
            for (PropertyDescriptor descriptor : descriptorList) {
                descriptorMap.put(descriptor.getKey(), descriptor);
            }
        }
        return descriptorMap;
    }
    public Map<String, PropertyDescriptor> getFlattenedDescriptorMap() {
        assertBuilt();

        if (flattenedDescriptorMap == null) {
            flattenedDescriptorMap = new HashMap<>();
            Deque<PropertyDescriptor> stack = new ArrayDeque<>(descriptorList);

            while (!stack.isEmpty()) {
                PropertyDescriptor descriptor = stack.pop();
                flattenedDescriptorMap.put(descriptor.getKey(), descriptor);
                if (descriptor.isGroup()) stack.addAll(descriptor.getChildProperties());
            }
        }

        return flattenedDescriptorMap;
    }
}
