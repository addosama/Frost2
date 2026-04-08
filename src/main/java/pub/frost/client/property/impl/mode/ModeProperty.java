package pub.frost.client.property.impl.mode;

import lombok.AllArgsConstructor;
import pub.frost.client.property.AbstractProperty;

@AllArgsConstructor
public class ModeProperty<T extends Enum<T>> extends AbstractProperty<T> {
    private T value;

    @Override
    public T getValue() {
        return value;
    }
    @Override
    protected boolean setValue(T oldValue, T newValue) {
        this.value = newValue;
        return oldValue != value;
    }

    public T[] getModes() {
        return value.getDeclaringClass().getEnumConstants();
    }
    public boolean is(T value) {
        return getValue() == value;
    }
}
