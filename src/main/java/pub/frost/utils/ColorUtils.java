package pub.frost.utils;

import imgui.ImVec4;

public class ColorUtils {
    public static int reAlpha(int colorABGR, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return (colorABGR & 0x00FFFFFF) | (alpha << 24);
    }

    public static ImVec4 toImVec4(int colorABGR) {
        float r = (colorABGR & 0xFF) / 255.0f;
        float g = ((colorABGR >> 8) & 0xFF) / 255.0f;
        float b = ((colorABGR >> 16) & 0xFF) / 255.0f;
        float a = ((colorABGR >> 24) & 0xFF) / 255.0f;

        return new ImVec4(r, g, b, a);
    }

    public static int toABGR(int colorARGB) {
        int a = (colorARGB >> 24) & 0xFF;
        int r = (colorARGB >> 16) & 0xFF;
        int g = (colorARGB >> 8)  & 0xFF;
        int b = (colorARGB)       & 0xFF;

        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}
