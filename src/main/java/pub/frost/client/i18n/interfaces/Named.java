package pub.frost.client.i18n.interfaces;

import pub.frost.client.core.FrostCore;

public interface Named {
    default String getName() {
        return FrostCore.getLocalizer().getName(this.toString());
    }
}
