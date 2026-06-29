package pub.frost.utils;

import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;

@TranslationKey("strings.enum.moduletoggletype.~")
@RequiredArgsConstructor
public enum EnumModuleToggleType implements Named {
    ON_ENABLE("OnEnable"),
    ON_DISABLE("OnDisable");

    final String key;
    @Override
    public String toString() {
        return key;
    }
}
