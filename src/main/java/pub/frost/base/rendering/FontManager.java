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
    private ImFont getIcon(float size) {
        return getFont(
                "iconfont.ttf", size,
                new short[] {(short)0xE000, (short)0xF8FF, 0}
        );
    }

    public final ImFont
            puHui18 = getPuhui(25),
            puhui14 = getPuhui(19),
            puHui12 = getPuhui(16),
            puHui10 = getPuhui(14);

    public final ImFont
            icon14 = getIcon(14),
            icon16 = getIcon(16);
}
