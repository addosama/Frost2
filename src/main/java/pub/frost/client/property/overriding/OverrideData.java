package pub.frost.client.property.overriding;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;
import pub.frost.client.property.overriding.suppliers.OverrideSupplier;

public class OverrideData<T> {
    @Getter @JSONField(name = "supplier")
    private final OverrideSupplier applySupplier;
    @Setter @Getter
    @JSONField(name = "value")
    private T value;

    public OverrideData(OverrideSupplier applySupplier, T defaultValue) {
        this.applySupplier = applySupplier;
        this.value = defaultValue;
    }

    public boolean shouldApply() {
        return applySupplier.get();
    }

    public void onRegistered() {
        applySupplier.onRegistered();
    }
    public void onUnregistered() {
        applySupplier.onUnregistered();
    }
}
