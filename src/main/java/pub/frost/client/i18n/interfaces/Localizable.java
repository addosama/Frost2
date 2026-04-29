package pub.frost.client.i18n.interfaces;

import pub.frost.client.i18n.annotations.TranslationKey;

public interface Localizable {
    default String getTranslationKey() {
        if (this.getClass().isAnnotationPresent(TranslationKey.class)) {
            return format(this.getClass().getAnnotation(TranslationKey.class).value());
        }
        return this.toString();
    }

    default String format(String key) {
        return format(this, key);
    }
    static String format(Object obj, String key) {
        return key.replaceAll("~", obj.toString());
    }
}
