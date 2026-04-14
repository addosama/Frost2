package pub.frost.utils.data;

import imgui.ImColor;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public enum EnumTextFormatting {
    BLACK("BLACK", '0', 0),
    DARK_BLUE("DARK_BLUE", '1', 1),
    DARK_GREEN("DARK_GREEN", '2', 2),
    DARK_AQUA("DARK_AQUA", '3', 3),
    DARK_RED("DARK_RED", '4', 4),
    DARK_PURPLE("DARK_PURPLE", '5', 5),
    GOLD("GOLD", '6', 6),
    GRAY("GRAY", '7', 7),
    DARK_GRAY("DARK_GRAY", '8', 8),
    BLUE("BLUE", '9', 9),
    GREEN("GREEN", 'a', 10),
    AQUA("AQUA", 'b', 11),
    RED("RED", 'c', 12),
    LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13),
    YELLOW("YELLOW", 'e', 14),
    WHITE("WHITE", 'f', 15),

    OBFUSCATED("OBFUSCATED", 'k'),
    BOLD("BOLD", 'l'),
    STRIKETHROUGH("STRIKETHROUGH", 'm'),
    UNDERLINE("UNDERLINE", 'n'),
    ITALIC("ITALIC", 'o'),

    RESET("RESET", 'r');

    static final int[] colors = new int[16];

    final char code;
    final int index;

    EnumTextFormatting(String str, char code) {
        this(code, -1);
    }
    EnumTextFormatting(String str, char code, int index) {
        this(code, index);
    }
    EnumTextFormatting(char code, int index) {
        this.code = code;
        this.index = index;
    }

    @Override public String toString() {
        return "§" + code;
    }

    public static final Pattern formattingCodePattern = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");

    public static EnumTextFormatting getFormatByCode(char code) {
        for (EnumTextFormatting formatting : EnumTextFormatting.values()) {
            if (formatting.getCode() == code) return formatting;
        }
        return null;
    }
    public static int getColorByCode(char code) {
        final String list = "0123456789abcdef";
        return getColorByIndex(list.indexOf(code));
    }
    public static int getColorByIndex(int index) {
        if (index < 0 || index >= 16) return 0;
        int base = (index >> 3 & 1) * 85;
        int r = (index >> 2 & 1) * 170 + base;
        int g = (index >> 1 & 1) * 170 + base;
        int b = (index & 1) * 170 + base;
        if (index == 6) {
            r += 85;
        }
        return ImColor.rgba(r, g, b, 255);
    }

    public boolean isColor() {
        return index != -1;
    }
    public int getColor() {
        return getColorByIndex(index);
    }

    public static String removeFormat(String string) {
        return string.replaceAll(formattingCodePattern.pattern(), "");
    }
    static {
        for (int index = 0; index < 16; index++) {
        }
    }
}
