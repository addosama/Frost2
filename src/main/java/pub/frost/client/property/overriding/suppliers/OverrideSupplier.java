package pub.frost.client.property.overriding.suppliers;

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
    private boolean state = false;
    protected void setState(boolean state) {
        this.state = state;
        stateChangeConsumer.accept(this.state);
    }
    @Override
    public Boolean get() {
        return state;
    }
}
