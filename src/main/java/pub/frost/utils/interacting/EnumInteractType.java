package pub.frost.utils.interacting;

import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;

@TranslationKey("strings.enum.interacting.~")
@RequiredArgsConstructor
public enum EnumInteractType implements Named {
    LEGIT("legit"),
    PACKET("packet"),;
    final String key;

    @Override
    public String toString() {
        return key;
    }
}
