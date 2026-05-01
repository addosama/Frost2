package pub.frost.client.feature.overlay.impl;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.overlay.ClientOverlay;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;

@TranslationKey("modules.hud.props.watermark.name")
public class WatermarkOverlay extends ClientOverlay implements Named {
    public WatermarkOverlay() {
        super("overlays.watermark");
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(true);

    private final String text = "Frost";

    @Override
    protected ImVec2[] preRender(boolean dummy, boolean input) {
        ImGui.pushFont(FontManager.INSTANCE.puHui18);
        return super.preRender(dummy, input);
    }
    @Override
    protected void postRender(boolean dummy, boolean input) {
        super.postRender(dummy, input);
        ImGui.popFont();
    }

    @Override
    protected void doRender(ImVec2 pos, ImVec2 normalizedOffset, ImDrawList draws, boolean input, float tickDelta) {
        final int bgColor = 0xCC331914;
        final int textColor = 0xFFFFEAE5;

        float xOffset = pos.x, yOffset = pos.y;
        if (normalizedOffset.y < 0) yOffset -= 30;
        // icon
        {
            final float x = xOffset, y = yOffset;
            ImGui.pushFont(FontManager.INSTANCE.icon14);
            draws.addRectFilled(
                    x, y, x + 30, y + 30,
                    bgColor, 12f
            );
            draws.addText(
                    x + 8, y + 7,
                    textColor, "\ue601"
            );
            ImGui.popFont();
            xOffset += 30;
        }
        xOffset += 6;
        // text
        {
            final float textWidth = ImGui.calcTextSizeX(text) + 18f;

            final float x = xOffset, y = yOffset;
            draws.addRectFilled(
                    x, y, x + textWidth, y + 30,
                    bgColor, 12f
            );
            draws.addText(
                    x + 9, y + 2,
                    textColor, text
            );
        }
    }

    @Override
    public boolean isVisible() {
        return enabled.get();
    }

    @Override
    public String getName() {
        return Named.super.getName();
    }
}
