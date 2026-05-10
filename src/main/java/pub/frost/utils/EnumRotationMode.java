package pub.frost.utils;

import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;

@TranslationKey("strings.enum.rotation.mode.~")
@RequiredArgsConstructor
public enum EnumRotationMode implements Named {
    BASIC("basic"),
    LAZY("lazy"),;

    final String key;
    @Override
    public String toString() {
        return key;
    }
}
