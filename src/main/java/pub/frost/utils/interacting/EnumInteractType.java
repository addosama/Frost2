package pub.frost.utils.interacting;

import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;

@TranslationKey("strings.~")
public enum EnumInteractType implements Named {
    LEGIT("legit"),
    PACKET("packet"),;
    final String key;

    EnumInteractType(final String key) {
        this.key = "interact." + key;
    }

    @Override
    public String toString() {
        return key;
    }
}
