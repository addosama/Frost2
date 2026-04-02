package pub.frost.client.i18n;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.client.core.FrostCore;
import pub.frost.utils.ResourceGetter;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class I18n {
    @RequiredArgsConstructor
    public enum Language {
        ENGLISH("en_US"),
        CHINESE("zh_CN");

        final String code;
    }

    private static final Language DEFAULT_LANGUAGE = Language.ENGLISH;
    private final Map<Language, Localizer> localizerMap = new EnumMap<>(Language.class);
    private @Getter Localizer currentLocalizer;

    public void loadLanguages() {
        for (Language language : Language.values()) {
            boolean isDefault = language == DEFAULT_LANGUAGE;
            if (!registerLanguage(language)) {
                if (isDefault) {
                    throw new RuntimeException("Failed to load client: could not load default language");
                }
                System.err.println("Failed to load language: " + language.code);
            }
        }

        setLanguage(DEFAULT_LANGUAGE);
    }
    private boolean registerLanguage(Language language) {
        String fileName = language.code;
        return localizerMap.computeIfAbsent(language, key -> {
            Properties properties = new Properties();
            InputStream stream = ResourceGetter.getClientResourceAsStream("lang/" + fileName + ".properties");
            if (FrostCore.DEBUG) assert stream != null : "Language not found: " + fileName;
            try {
                properties.load(stream);
                return new Localizer(properties);
            } catch (IOException e) {
                System.err.println("Failed to load language: " + fileName);
            }
            return null;
        }) != null;
    }

    public void setLanguage(Language language) {
        Localizer targetLocalizer = localizerMap.get(language);
        if (targetLocalizer == null) return;
        currentLocalizer = targetLocalizer;
    }
}
