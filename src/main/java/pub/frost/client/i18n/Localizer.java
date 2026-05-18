package pub.frost.client.i18n;

import java.util.Properties;

public class Localizer {
    private final Properties properties;

    public Localizer(Properties properties) {
        this.properties = properties;
    }

    public String get(String key) {
        return getOrDefault(key, key);
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
