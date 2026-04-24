package pub.frost.client.i18n.interfaces;

import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.annotations.TranslationKey;

public interface Named extends Localizable {
    default String getName() {
        return FrostCore.getLocalizer().getName(getTranslationKey());
    }
}
