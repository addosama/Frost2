package pub.frost.utils;

public class EnumUtils {
    public static <T> T getEnumByString(Class<T> clazz, String str) {
        for (T enumValue : clazz.getEnumConstants()) {
            if (enumValue.toString().equals(str)) return enumValue;
        }
        return null;
    }
}
