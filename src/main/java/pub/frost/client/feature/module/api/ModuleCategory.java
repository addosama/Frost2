package pub.frost.client.feature.module.api;

import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.interfaces.Named;

@RequiredArgsConstructor
public enum ModuleCategory implements Named {
    COMBAT("combat"),
    MOVEMENT("movement"),
    VISUAL("visual"),;

    final String key;
    @Override
    public String toString() {
        return "categories." + key;
    }
}
