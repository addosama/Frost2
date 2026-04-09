package pub.frost.utils.interacting;

import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.interfaces.Named;

public enum EnumInteractType implements Named {
    LEGIT("legit"),
    PACKET("packet"),;
    final String key;

    EnumInteractType(final String key) {
        this.key = "interact." + key;
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public String getName() {
        return FrostCore.getLocalizer().get("strings." + key);
    }
}
