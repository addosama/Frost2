package pub.frost.client.feature.screen;

import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.base.wrapping.Wrappers;

public abstract class ClientScreen implements Wrappers {
    protected final Object mc = Minecraft.getInstance();

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
        if (!allowCursorGrabbing()) Minecraft.ungrabMouse(mc);
    }
    public void onClose() {

    }

    public void onInput(InputDevice device, int code, int action) {

    }
}
