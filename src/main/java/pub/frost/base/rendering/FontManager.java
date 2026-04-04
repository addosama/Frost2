package pub.frost.base.rendering;

import imgui.ImFont;
import imgui.ImGui;
import pub.frost.utils.ResourceGetter;

import java.io.IOException;

public class FontManager {
    private static ImFont getFont(String name, float size) {
        try {
            return ImGui.getIO().getFonts().addFontFromMemoryTTF(
                    ResourceGetter.readAllBytes(ResourceGetter.getClientResourceAsStream("fonts/" + name)),
                    size
            );
        } catch (IOException e) {
            return ImGui.getFont();
        }
    }
    private static ImFont getFont(String name, float size, short[] glyphRanges) {
        try {
            return ImGui.getIO().getFonts().addFontFromMemoryTTF(
                    ResourceGetter.readAllBytes(ResourceGetter.getClientResourceAsStream("fonts/" + name)),
                    size, glyphRanges
            );
        } catch (IOException e) {
            return ImGui.getFont();
        }
    }

    public static FontManager INSTANCE;
    public FontManager() {
        INSTANCE = this;
    }

    private ImFont getPuhui(float size) {
        return getFont("Alibaba-PuHuiTi-Regular.otf", size, ImGui.getIO().getFonts().getGlyphRangesChineseFull());
    }

    public final ImFont
    puHui18 = getPuhui(25),
    puHui10 = getPuhui(14);
}
