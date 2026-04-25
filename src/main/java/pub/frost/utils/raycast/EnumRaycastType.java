package pub.frost.utils.raycast;

import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;

@TranslationKey("strings.enum.raycast.~")
@RequiredArgsConstructor
public enum EnumRaycastType implements Named {
    DISABLED("disabled"),
    LEGIT("legit"),
    DEFAULT("default");
    final String key;

    @Override
    public String toString() {
        return key;
    }
}
