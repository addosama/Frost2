package pub.frost.utils;

import imgui.ImDrawList;
import imgui.ImGui;
import pub.frost.utils.data.EnumTextFormatting;

import java.util.regex.Matcher;

public class ImTextRenderer {
    public static void drawText(ImDrawList list, String string, float x, float y, int color, int shadowColor) {
        if (shadowColor != 0) list.addText(x + 1, y + 1, shadowColor, string);
        list.addText(x, y, color, string);
    }
    public static void drawText(ImDrawList list, String string, float x, float y, int color, boolean shadow) {
        Matcher matcher = EnumTextFormatting.formattingCodePattern.matcher(string);
        int lastEnd = 0;
        float currentX = x;
        int currentColor = color;
        int currentShadowColor = shadow? (color & 16579836) >> 2 | color & -16777216 : 0;

        while (matcher.find()) {
            // 1. 先画颜色代码之前的纯文本部分
            String content = string.substring(lastEnd, matcher.start());
            if (!content.isEmpty()) {
                drawText(
                        list,
                        content,
                        currentX, y,
                        currentColor,
                        currentShadowColor
                );
                currentX += getTextWidth(content);
            }

            // 2. 根据匹配到的代码更新颜色 (例如 §c -> 红色)
            String code = matcher.group(); // 得到 "§c"
            char colorCode = code.charAt(1);
            if (colorCode == 'r') {
                currentColor = color;
            } else currentColor = EnumTextFormatting.getColorByCode(colorCode);
            if (shadow) currentShadowColor = (currentColor & 16579836) >> 2 | currentColor & -16777216;

            lastEnd = matcher.end();
        }

        // 3. 画出最后剩余的部分
        String remaining = string.substring(lastEnd);
        if (!remaining.isEmpty()) {
           drawText(
                   list,
                   remaining,
                   currentX, y,
                   currentColor, currentShadowColor
           );
        }
    }

    public static void drawText(ImDrawList list, String string, float x, float y, int color) {
        drawText(list, string, x, y, color, false);
    }
    public static void drawShadowedText(ImDrawList list, String string, float x, float y, int color) {
        drawText(list, string, x, y, color, true);
    }
    public static void drawOutlinedText(ImDrawList list, String string, float x, float y, int color, int outlineColor) {
        String unformatted = EnumTextFormatting.removeFormat(string);
        drawText(list, unformatted, x, y - 1, outlineColor, 0);
        drawText(list, unformatted, x - 1, y, outlineColor, 0);
        drawText(list, unformatted, x, y + 1, outlineColor, 0);
        drawText(list, unformatted, x + 1, y, outlineColor, 0);
        drawText(list, string, x, y, color);
    }

    public static float getTextWidth(String text) {
        return ImGui.calcTextSizeX(EnumTextFormatting.removeFormat(text));
    }
    public static float getTextHeight() {
        return ImGui.getTextLineHeight();
    }
}
