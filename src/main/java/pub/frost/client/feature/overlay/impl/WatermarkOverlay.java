package pub.frost.client.feature.overlay.impl;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.overlay.ClientOverlay;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;

public class WatermarkOverlay extends ClientOverlay {
    public WatermarkOverlay() {
        super("overlays.watermark");
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(true);

    private final String text = "Frost";

    @Override
    protected void preRender(boolean dummy) {
        super.preRender(dummy);
        ImGui.pushFont(FontManager.INSTANCE.puHui18);
    }
    @Override
    protected void postRender(boolean dummy) {
        ImGui.popFont();
        super.postRender(dummy);
    }

    @Override
    protected void doRender(boolean dummy, boolean input, float tickDelta) {
        if (!enabled.get()) return;
        int windowFlags = DEFAULT_WINDOW_FLAGS;
        if (!input) windowFlags |= ImGuiWindowFlags.NoInputs;

        ImGui.begin(this.toString(), windowFlags);

        final int bgColor = ImColor.rgba(20, 25, 51, 204);
        final int textColor = ImColor.rgba("#E5EAFFFF");
        ImDrawList draws = ImGui.getWindowDrawList();

        // icon
        {
            ImGui.dummy(30, 30);
            if (!dummy) {
                final float x = ImGui.getItemRectMinX(), y = ImGui.getItemRectMinY();
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
            }
        }
        ImGui.sameLine(0, 5);
        // text
        {
            final float textWidth = ImGui.calcTextSizeX(text) + 18f;
            ImGui.dummy(textWidth, 30);
            if (!dummy) {
                final float x = ImGui.getItemRectMinX(), y = ImGui.getItemRectMinY();
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

        ImGui.end();
    }
}
