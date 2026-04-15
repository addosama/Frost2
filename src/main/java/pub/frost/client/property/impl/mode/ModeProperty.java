package pub.frost.client.property.impl.mode;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import pub.frost.client.property.AbstractProperty;

@AllArgsConstructor
public class ModeProperty<T extends Enum<T>> extends AbstractProperty<T> {
    @JSONField(name = "value")
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
