package pub.frost.client.i18n;

import pub.frost.client.core.FrostCore;

import java.util.Properties;

public class Localizer {
    private final Properties properties;

    public Localizer(Properties properties) {
        this.properties = properties;
    }

    public String get(String key, boolean smartLocalize, boolean markUnlocalized) {
        String value = getOrDefault(key, null);
        if (value == null) {
            if (!smartLocalize) return key.toLowerCase();
            String prefix = markUnlocalized? "§l*§r" : "";
            String[] array = key.split("\\.");
            if (array.length < 2) return prefix + key;
            return prefix + array[array.length - 1];
        }
        else return value;
    }
    public String get(String key) {
        return get(
                key,
                FrostCore.getInstance().getClientSettings().localizing.smartLocalize.get(),
                FrostCore.getInstance().getClientSettings().localizing.markUnlocalizedString.get()
        );
    }

    public String getOrDefault(String key, String defaultValue) {
        return properties.getProperty(key.toLowerCase(), defaultValue);
    }

    public String getName(String key) {
        return getOrDefault(key + ".name", get(key));
    }
    public String getDescription(String key) {
        return getOrDefault(key + ".descriptions", "");
    }
    public String getNoDescriptionText() {
        return get("strings.nodescriptions");
    }
}
