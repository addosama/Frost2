package pub.frost.client.property.overriding;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Overriding<T> {
    private @Getter boolean enabled;
    private @Getter List<OverrideData<T>> dataList;
    private @Setter Supplier<String> displayStringSupplier;

    public void enableOverriding() {
        if (!enabled) {
            enabled = true;
            dataList = new ArrayList<>();
            displayStringSupplier = () -> "";
        }
    }

    public String getDisplayString() {
        return displayStringSupplier.get();
    }
    public T getOverrideValue(T fallback) {
        if (!isEnabled()) return fallback;
        T value = fallback;
        for (OverrideData<T> data : dataList) {
            if (data.shouldApply()) value = data.getValue();
        }
        return value;
    }
}
