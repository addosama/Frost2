package pub.frost.client.property;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Setter;
import lombok.experimental.Accessors;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupHead;
import pub.frost.client.property.overriding.OverrideData;
import pub.frost.client.property.overriding.Overriding;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractProperty<T> {
    @Accessors(chain = true) @Setter
    private transient BiConsumer<T, T> valueChangeListener = (o, n) -> {};
    @Accessors(chain = true) @Setter
    private transient Supplier<Boolean> visibilitySupplier = () -> true;
    public final boolean isVisible() {
        return visibilitySupplier.get();
    }
    public final <P extends AbstractProperty<T>> P setVisibilitySupplier(Class<P> returnType, Supplier<Boolean> visibilitySupplier) {
        return returnType.cast(setVisibilitySupplier(visibilitySupplier));
    }

    @JSONField(name = "overriding")
    private final Overriding<T> overriding = new Overriding<>();
    public boolean isOverridingEnabled() {
        return overriding.isEnabled();
    }
    public void enableOverriding() {
        overriding.enableOverriding();
    }
    public void setOverrideDisplayString(Supplier<String> displayString) {
        overriding.setDisplayStringSupplier(displayString);
    }
    public List<OverrideData<T>> getOverrideData() {
        return overriding.getDataList();
    }

    private transient final Consumer<OverrideData<T>> overrideDataProcessor = data -> {
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
        data.getApplySupplier().setState(false);
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
            if (overriding.isEnabled()) {
                if (
                        value == overriding.getOverrideValue(null)
                        || overriding.isActive()
                ) return;
            }
            valueChangeListener.accept(old, value);
        }
    }
    public final boolean isOverrideActive() {
        return overriding.isActive();
    }

    public T deserializeValue(Object obj) {
        return (T) obj;
    }
    public abstract T getValue();
    protected abstract boolean setValue(T oldValue, T newValue);

    public static boolean isPropertyField(Field field) {
        return field.isAnnotationPresent(Property.class) && AbstractProperty.class.isAssignableFrom(field.getType());
    }
    public static boolean isGroupHead(Field field) {
        if (field.isAnnotationPresent(PropertyGroupHead.class)) {
            Type type = field.getGenericType();
            if (!(type instanceof ParameterizedType)) return false;
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() != Supplier.class) return false;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            return actualTypeArguments.length == 1 && actualTypeArguments[0] == Boolean.class;
        }
        return false;
    }
}
