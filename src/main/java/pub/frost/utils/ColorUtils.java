package pub.frost.utils;

import imgui.ImVec4;

import java.awt.*;

public class ColorUtils {
    public static int reAlpha(int colorABGR, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return (colorABGR & 0x00FFFFFF) | (alpha << 24);
    }
    public static int getAlpha(int colorAXXX) {
        return (colorAXXX >> 24) & 0xFF;
    }

    public static int[] toRGBA(int colorABGR) {
        int r = (colorABGR & 0xFF);
        int g = ((colorABGR >> 8) & 0xFF);
        int b = ((colorABGR >> 16) & 0xFF);
        int a = ((colorABGR >> 24) & 0xFF);

        return new int[]{r, g, b, a};
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

    public static int HSBtoBGR(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float)Math.floor(hue)) * 6.0f;
            float f = h - (float)java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return 0xff000000 | (b << 16) | (g << 8) | (r);
    }
    public static float[] BGRtoHSB(int colorABGR) {
        int[] rgba = toRGBA(colorABGR);
        return Color.RGBtoHSB(rgba[0], rgba[1], rgba[2], null);
    }
}
