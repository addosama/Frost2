package pub.frost.client.property.overriding.suppliers;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class OverrideSupplier implements Supplier<Boolean> {
    @Setter
    private Consumer<Boolean> stateChangeConsumer;

    public void onRegistered() {}
    public void onUnregistered() {}

    @Getter(AccessLevel.PROTECTED)
    private transient boolean state = false;
    public void setState(boolean state) {
        if (this.state != state) {
            this.state = state;
            stateChangeConsumer.accept(this.state);
        }
    }
    @Override
    public Boolean get() {
        return state;
    }

    @JSONField(name = "type")
    private final int type = getSupplierType();
    protected abstract int getSupplierType();
}
