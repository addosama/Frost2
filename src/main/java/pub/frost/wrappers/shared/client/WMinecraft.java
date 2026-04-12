package pub.frost.wrappers.shared.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import pub.frost.base.wrapping.legacy.impl.InstanceWrapper;
import pub.frost.base.wrapping.legacy.impl.StaticWrapper;
import pub.frost.platforms.v1_8_9.forged.mixin.AccessorMinecraft;
import pub.frost.wrappers.FakeInstanceWrapper;
import pub.frost.wrappers.shared.entity.WEntityClientPlayer;
import pub.frost.wrappers.shared.world.WWorld;

public class WMinecraft extends StaticWrapper {
    public WMinecraft() {
        super(Minecraft.class);
    }

    private Instance cachedInstance;
    public Instance getInstance() {
        if (cachedInstance == null) {
            cachedInstance = new Instance(Minecraft.getMinecraft());
        }
        return cachedInstance;
    }


    public static class Instance extends InstanceWrapper implements FakeInstanceWrapper<Minecraft> {
        public Instance(Object wrappedObject) {
            super(wrappedObject);
        }

        public WEntityClientPlayer getPlayer() {
            EntityPlayerSP player = cast().thePlayer;
            if (player == null) return null;
            return new WEntityClientPlayer(player);
        }

        public WWorld getWorld() {
            World world = cast().theWorld;
            if (world == null) return null;
            return new WWorld(world);
        }

        public void grabMouse() {
            cast().setIngameFocus();
        }
        public void ungrabMouse() {
            cast().setIngameNotInFocus();
        }

        public void clickLMB() {
            ((AccessorMinecraft) getWrappedObject()).callClickMouse();
        }
        public void clickRMB() {
            ((AccessorMinecraft) getWrappedObject()).callRightClickMouse();
        }
    }
}
