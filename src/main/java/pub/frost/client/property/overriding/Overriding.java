package pub.frost.client.property.overriding;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class Overriding<T> {
    @Getter
    private transient boolean enabled;
    @Getter @JSONField(name = "data")
    private List<OverrideData<T>> dataList;
    @Setter @Getter
    private transient Supplier<String> displayStringSupplier;

    public void enableOverriding() {
        if (!enabled) {
            enabled = true;
            dataList = new CopyOnWriteArrayList<>();
            displayStringSupplier = () -> "";
        }
    }

    public T getOverrideValue(T fallback) {
        if (!isEnabled()) return fallback;
        T value = fallback;
        for (OverrideData<T> data : dataList) {
            if (data.shouldApply()) value = data.getValue();
        }
        return value;
    }
    public boolean isActive() {
        if (!isEnabled()) return false;
        for (OverrideData<T> data : dataList) if (data.shouldApply()) return true;
        return false;
    }

    public void register(OverrideData<T> data) {
        if (!isEnabled()) return;
        dataList.add(data);
        data.onRegistered();
    }
    public void unregister(OverrideData<T> data) {
        if (!isEnabled()) return;
        dataList.remove(data);
        data.onUnregistered();
    }
    public void unregisterAll() {
        if (!isEnabled()) return;
        dataList.forEach(this::unregister);
    }
}
