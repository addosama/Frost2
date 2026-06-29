package pub.frost.base.rendering;

import imgui.ImFont;
import imgui.ImGui;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pub.frost.utils.ResourceGetter;

import java.io.IOException;
import java.util.function.Supplier;

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
        for (EnumFont f :  EnumFont.values()) f.getBaseSizeFontSupplier().get();
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

    @Deprecated
    public final ImFont
            puHui18 = getPuhui(25),
            puHui16 = getPuhui(22),
            puhui14 = getPuhui(19),
            puHui12 = getPuhui(16),
            puHui10 = getPuhui(14);

    @Deprecated
    public final ImFont
            icon14 = getIcon(14),
            icon16 = getIcon(16);

    public static void pushFont(EnumFont font, float size) {
        ImGui.pushFont(font.getBaseSizeFontSupplier().get(), (size / 18) * font.size18);
    }

    @Deprecated
    public static void pushFont(ImFont font) {
        ImGui.pushFont(font, 0);
    }

    @AllArgsConstructor @Getter
    public enum EnumFont {
        PuHui("AlibabaPuHui", 25, new Supplier<ImFont>() {
            ImFont font = null;
            @Override
            public ImFont get() {
                if (font == null)
                    font = getFont(
                            "Alibaba-PuHuiTi-Regular.otf", 25,
                            ImGui.getIO().getFonts().getGlyphRangesChineseFull()
                    );
                return font;
            }
        }),
        Icon("IconFont", 18, new Supplier<ImFont>() {
            ImFont font = null;
            @Override
            public ImFont get() {
                if (font == null)
                    font = getFont(
                            "iconfont.ttf", 18,
                            new short[] {(short)0xE000, (short)0xF8FF, 0}
                    );
                return font;
            }
        })
        ;

        final String name;
        final float size18;
        final Supplier<ImFont> baseSizeFontSupplier;
    }
}
