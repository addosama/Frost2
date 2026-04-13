package pub.frost.utils;

import pub.frost.utils.data.EnumTextFormatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatUtils {
    public static int getFirstColor(String string) {
        Matcher matcher = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]").matcher(string);
        while (matcher.find()) {
            String str = matcher.group();
            if (str.isEmpty()) continue;
            int formatting = EnumTextFormatting.getColorByCode(str.charAt(1));
            if (formatting == 0) continue;
            return formatting;
        }
        return 0;
    }
}
