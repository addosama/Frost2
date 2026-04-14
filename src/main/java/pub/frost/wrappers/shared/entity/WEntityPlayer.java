package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.player.EntityPlayer;

public class WEntityPlayer extends WEntityLivingBase {
    public WEntityPlayer() {
        super(EntityPlayer.class);
    }
    public WEntityPlayer(Class<?> targetClass) {
        super(targetClass);
    }

    public boolean isSpectator(Object instance) {
        return cast(instance, EntityPlayer.class).isSpectator();
    }

    public Object getInventory(Object instance) {
        return cast(instance, EntityPlayer.class).inventory;
    }
}
