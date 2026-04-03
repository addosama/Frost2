package pub.frost.client.property.overriding.suppliers;

import lombok.Getter;
import lombok.Setter;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.bindable.api.IBindable;

public class OverrideOnKey extends OverrideSupplier implements IBindable {
    @Getter @Setter
    private int keybind;
    @Setter
    private boolean hold;

    public OverrideOnKey(int keybind, boolean hold) {
        this.keybind = keybind;
        this.hold = hold;
    }
    public OverrideOnKey(int keybind) {
        this(keybind, false);
    }
    public OverrideOnKey() {
        this(0, false);
    }

    @Override
    public void onActive(int action) {
        setState(!isState());
    }

    @Override
    public boolean shouldActiveWhenRelease() {
        return hold;
    }

    @Override
    public void onRegistered() {
        FrostCore.getInstance().getBindableManager().register(this);
    }

    @Override
    public void onUnregistered() {
        FrostCore.getInstance().getBindableManager().unregister(this);
    }
}
