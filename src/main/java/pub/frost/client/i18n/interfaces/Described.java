package pub.frost.client.i18n.interfaces;

import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.annotations.TranslationKey;

public interface Described {
    default String getDescription() {
        String key = this.toString();
        if (this.getClass().isAnnotationPresent(TranslationKey.class)) {
            TranslationKey annotation = this.getClass().getAnnotation(TranslationKey.class);
            key = annotation.value().replaceAll("~", this.toString());
        }
        return FrostCore.getLocalizer().getDescription(key);
    }
}
