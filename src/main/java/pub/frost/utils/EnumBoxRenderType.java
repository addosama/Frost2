package pub.frost.utils;

import lombok.RequiredArgsConstructor;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;

@TranslationKey("strings.enum.rendering.box.~")
@RequiredArgsConstructor
public enum EnumBoxRenderType implements Named {
    RECT("rect"),
    BOX_2D("box2d"),
    BOX_3D("box3d"),;

    final String key;
    @Override
    public String toString() {
        return key;
    }
}
