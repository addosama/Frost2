package pub.frost.utils;

import org.lwjgl.input.Keyboard;

public class InputUtils {
    public static String getKeyName(int keyCode) {
        if (keyCode < 0) {
            keyCode = Math.abs(keyCode);
            if (keyCode == 1) return "LMB";
            else if (keyCode == 2) return "RMB";
            else return "M" + keyCode;
        } else return Keyboard.getKeyName(keyCode);
    }
}
