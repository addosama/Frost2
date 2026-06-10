package pub.frost.client.feature.screen;

import net.minecraft.client.Minecraft;
import pub.frost.base.event.impl.types.InputDevice;

public abstract class ClientScreen {
    protected final Minecraft mc = Minecraft.getMinecraft();

    public abstract void render(boolean dummy, float tickDelta);

    public boolean shouldBlockKeyboardInput() {
        return true;
    }
    public boolean shouldBlockMouseInput() {
        return true;
    }
    public boolean allowCursorGrabbing() {
        return false;
    }

    public void onDisplay() {
        if (!allowCursorGrabbing()) mc.mouseHelper.ungrabMouseCursor();
    }
    public void onClose() {

    }

    public void onInput(InputDevice device, int code, int action) {

    }
}
