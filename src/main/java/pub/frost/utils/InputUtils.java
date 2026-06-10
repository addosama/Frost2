package pub.frost.utils;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import pub.frost.platforms.v1_8_9.forged.mixin.AccessorMinecraft;

public class InputUtils {
    public static String getKeyName(int keyCode) {
        if (keyCode < 0) {
            keyCode = Math.abs(keyCode);
            if (keyCode == 1) return "LMB";
            else if (keyCode == 2) return "RMB";
            else return "M" + keyCode;
        } else return Keyboard.getKeyName(keyCode);
    }

    public static boolean isKeyDown(int keyCode) {
        return Keyboard.isKeyDown(keyCode);
    }
    public static boolean isMouseDown(int button) {
        return Mouse.isButtonDown(button);
    }
    public static float getMouseEventWheel() {
        return Mouse.getDWheel() / 120f;
    }

    public static void clickLMB() {
        ((AccessorMinecraft) Minecraft.getMinecraft()).callClickMouse();
    }
    public static void clickRMB() {
        ((AccessorMinecraft) Minecraft.getMinecraft()).callRightClickMouse();
    }
}
