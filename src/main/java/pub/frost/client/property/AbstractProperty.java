package pub.frost.client.property;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.experimental.Accessors;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupHead;
import pub.frost.client.property.descriptor.VisibilitySupplier;
import pub.frost.client.property.overriding.OverrideData;
import pub.frost.client.property.overriding.Overriding;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public abstract class AbstractProperty<T, SELF extends AbstractProperty<T, SELF>> {
    @Accessors(chain = true)
    private transient BiConsumer<T, T> valueChangeListener = (o, n) -> {};
    @Accessors(chain = true)
    private transient VisibilitySupplier visibilitySupplier = () -> true;
    public final boolean isVisible() {
        return visibilitySupplier.isVisible();
    }
    public final SELF setValueChangeListener(BiConsumer<T, T> valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
        return (SELF) this;
    }
    public final SELF setVisibilitySupplier(VisibilitySupplier visibilitySupplier) {
        this.visibilitySupplier = visibilitySupplier;
        return (SELF) this;
    }

    @Deprecated
    public final SELF setVisibilitySupplier(Class<SELF> returnType, VisibilitySupplier visibilitySupplier) {
        return setVisibilitySupplier(visibilitySupplier);
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
    public void clearOverridingData() {
        overriding.unregisterAll();
    }

    private transient final Consumer<OverrideData<T>> overrideDataProcessor = data -> {
        data.getApplySupplier().setDisplayNameSupplier(overriding.getDisplayStringSupplier());
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
            return field.getType() == VisibilitySupplier.class;
        }
        return false;
    }
}
