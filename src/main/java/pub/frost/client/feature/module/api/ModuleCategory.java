package pub.frost.client.feature.module.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.interfaces.Named;

@RequiredArgsConstructor
public enum ModuleCategory implements Named {
    COMBAT("\ue88a", "combat"),
    MOVEMENT("\ue86b", "movement"),
    VISUAL("\ue869", "visual"),;

    @Getter
    final String icon;
    final String key;
    @Override
    public String toString() {
        return "categories." + key;
    }
}
