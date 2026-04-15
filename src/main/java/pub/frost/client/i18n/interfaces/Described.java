package pub.frost.client.i18n.interfaces;

import pub.frost.client.core.FrostCore;

public interface Described {
    default String getDescription() {
        return FrostCore.getLocalizer().getDescription(this.toString());
    }
}
