package pub.frost.client.property.overriding;

import lombok.Setter;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.function.Supplier;

public class OverrideData<T> {
    @Setter
    private Supplier<Boolean> applySupplier;
    private final Mutable<T> value;

    public OverrideData(Supplier<Boolean> applySupplier, T defaultValue) {
        this.applySupplier = applySupplier;
        this.value = new MutableObject<>(defaultValue);
    }

    public boolean shouldApply() {
        return applySupplier.get();
    }

    public T getValue() {
        return value.get();
    }
    public void setValue(T value) {
        this.value.setValue(value);
    }
}
