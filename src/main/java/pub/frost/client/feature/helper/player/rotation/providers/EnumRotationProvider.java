package pub.frost.client.feature.helper.player.rotation.providers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.client.feature.helper.player.rotation.providers.impl.BasicRotationProvider;
import pub.frost.client.feature.helper.player.rotation.providers.impl.LazyRotationProvider;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;

@TranslationKey("strings.enum.rotation.mode.~")
@RequiredArgsConstructor
public enum EnumRotationProvider implements Named {
    BASIC("basic", BasicRotationProvider.class),
    LAZY("lazy", LazyRotationProvider.class),;

    final String key;
    @Getter
    final Class<? extends AbstractRotationProvider> providerClass;
    @Override
    public String toString() {
        return key;
    }
}
