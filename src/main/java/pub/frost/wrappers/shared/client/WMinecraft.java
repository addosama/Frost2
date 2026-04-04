package pub.frost.wrappers.shared.client;

import net.minecraft.client.Minecraft;
import pub.frost.base.wrapping.impl.InstanceWrapper;
import pub.frost.base.wrapping.impl.StaticWrapper;
import pub.frost.wrappers.FakeInstanceWrapper;
import pub.frost.wrappers.shared.entity.WEntityLivingBase;
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


    public class Instance extends InstanceWrapper implements FakeInstanceWrapper<Minecraft> {
        private WEntityLivingBase cachedPlayer;
        private WWorld cachedWorld;

        public Instance(Object wrappedObject) {
            super(wrappedObject);
        }

        public WEntityLivingBase getPlayer() {
            if (cachedPlayer == null || cachedPlayer.getWrappedObject().hashCode() != cast().thePlayer.hashCode()) {
                cachedPlayer = new WEntityLivingBase(cast().thePlayer);
            }
            return cachedPlayer;
        }

        public WWorld getWorld() {
            if (cachedWorld == null || cachedWorld.getWrappedObject().hashCode() != cast().theWorld.hashCode()) {
                cachedWorld = new WWorld(cast().theWorld);
            }
            return cachedWorld;
        }

        public void grabMouse() {
            cast().setIngameFocus();
        }
        public void ungrabMouse() {
            cast().setIngameNotInFocus();
        }
    }
}
