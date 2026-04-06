package pub.frost.client.feature.screen;

import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;
import pub.frost.wrappers.shared.client.WMinecraft;

public abstract class ClientScreen {
    protected final WMinecraft.Instance mc = FrostCore.getInstance().getWrapperManager().getWrapper(WMinecraft.class).getInstance();

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
        if (!allowCursorGrabbing()) mc.ungrabMouse();
    }
    public void onClose() {

    }

    public void onInput(InputDevice device, int code, int action) {

    }
}
