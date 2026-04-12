package pub.frost.wrappers.shared.client;

import net.minecraft.client.Minecraft;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.platforms.v1_8_9.forged.mixin.AccessorMinecraft;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WMinecraft extends Wrapper implements FakeInstanceWrapper<Minecraft> {
    public WMinecraft() {
        super(Minecraft.class);
    }

    public Object getInstance() {
        return Minecraft.getMinecraft();
    }

    public Object getPlayer(Object mc) {
        return cast(mc).thePlayer;
    }

    public Object getWorld(Object mc) {
        return cast(mc).theWorld;
    }

    public void grabMouse(Object mc) {
        cast(mc).setIngameFocus();
    }
    public void ungrabMouse(Object mc) {
        cast(mc).setIngameNotInFocus();
    }

    public void clickLMB(Object mc) {
        ((AccessorMinecraft) mc).callClickMouse();
    }
    public void clickRMB(Object mc) {
        ((AccessorMinecraft) mc).callRightClickMouse();
    }
}
