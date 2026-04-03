package pub.frost.client.property;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGrouping;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.overriding.OverrideData;
import pub.frost.client.property.overriding.Overriding;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractProperty<T> {
    @Accessors(chain = true) @Setter
    private BiConsumer<T, T> valueChangeListener = (o, n) -> {};
    @Accessors(chain = true) @Setter
    private Supplier<Boolean> visibilitySupplier = () -> true;
    public final boolean isVisible() {
        return visibilitySupplier.get();
    }

    private final Overriding<T> overriding = new Overriding<>();
    public void enableOverriding() {
        overriding.enableOverriding();
    }
    public void setOverrideDisplayString(Supplier<String> displayString) {
        overriding.setDisplayStringSupplier(displayString);
    }

    private final Consumer<OverrideData<T>> overrideDataProcessor = data -> {
        data.getApplySupplier().setStateChangeConsumer(state -> {
            T value = getValue();
            T overrideValue = data.getValue();
            if (overrideValue.equals(value)) return;
            if (state) {
                valueChangeListener.accept(value, overrideValue);
            } else valueChangeListener.accept(overrideValue, value);
        });
    };

    public void addOverrideData(OverrideData<T> data) {
        overrideDataProcessor.accept(data);
        overriding.register(data);
    }
    public void removeOverrideData(OverrideData<T> data) {
        overriding.unregister(data);
    }

    public final T get() {
        T value = getValue();
        if (overriding.isEnabled()) {
            return overriding.getOverrideValue(value);
        }
        return value;
    }
    public final void set(T value) {
        T old = getValue();
        if (setValue(old, value)) {
            valueChangeListener.accept(old, value);
        }
    }

    protected abstract T getValue();
    protected abstract boolean setValue(T oldValue, T newValue);

    public static List<PropertyDescriptor> getPropertyDescriptorsForObject(
            Object object,
            String keyPrefix, Function<String, String> keyProcessor
    ) {
        int unnamedIndex = 0;
        Stack<String> groupKeyPrefixStack = new Stack<>();
        Stack<List<PropertyDescriptor>> groupListStack = new Stack<>();
        groupKeyPrefixStack.push("");
        groupListStack.push(new ArrayList<>());


        for (Field field : object.getClass().getDeclaredFields()) {
            if (!isPropertyField(field)) continue;
            if (field.isAnnotationPresent(Deprecated.class)) continue;

            try {
                Property propDataAnno = field.getAnnotation(Property.class);
                AbstractProperty<?> currentProp = (AbstractProperty<?>) field.get(object);
                if (field.isAnnotationPresent(PropertyGrouping.Push.class)) {
                    PropertyGrouping.Push groupAnno = field.getAnnotation(PropertyGrouping.Push.class);
                    groupKeyPrefixStack.push(groupAnno.value());
                    groupListStack.push(new ArrayList<>());
                }

                String propKey = propDataAnno.value(); {
                    if (StringUtils.isBlank(propKey)) {
                        propKey = "unnamed-property-" + unnamedIndex;
                        unnamedIndex++;
                    }
                    String groupPrefix = groupKeyPrefixStack.peek();
                    if (!StringUtils.isBlank(groupPrefix)) {
                        propKey = groupPrefix + ".subprops." + propKey;
                    }
                    propKey = keyProcessor.apply(keyPrefix + propKey);
                }

                if (propDataAnno.allowOverriding()) currentProp.enableOverriding();

                groupListStack.peek().add(new PropertyDescriptor(propKey, currentProp));
                if (field.isAnnotationPresent(PropertyGrouping.Pop.class)) {
                    List<PropertyDescriptor> poppedList = groupListStack.pop();
                    groupListStack.peek().add(new PropertyDescriptor(
                            keyProcessor.apply(keyPrefix + groupKeyPrefixStack.pop()),
                            poppedList
                    ));
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        return groupListStack.pop();
    }

    private static boolean isPropertyField(Field field) {
        return field.isAnnotationPresent(Property.class) && AbstractProperty.class.isAssignableFrom(field.getType());
    }
}
