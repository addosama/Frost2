package pub.frost.client.feature.screen;

import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;
import pub.frost.wrappers.shared.client.WMinecraft;

public abstract class ClientScreen {
    protected final Object mc = FrostCore.getInstance().getWrapperManager().getWrapper(WMinecraft.class).getInstance();
    protected final WMinecraft mcWrapper = FrostCore.getInstance().getWrapperManager().getWrapper(WMinecraft.class);

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
        if (!allowCursorGrabbing()) mcWrapper.ungrabMouse(mc);
    }
    public void onClose() {

    }

    public void onInput(InputDevice device, int code, int action) {

    }
}
