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
        return format(key, this.toString());
    }
    static String format(
            String key,
            String objectKey
    ) {
        return key.replaceAll("~", objectKey);
    }
}
