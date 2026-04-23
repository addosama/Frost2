package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

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

    public void setItemInUse(Object instance, Object itemStack, int duration) {
        cast(instance, EntityPlayer.class).setItemInUse((ItemStack) itemStack, duration);
    }
    public void stopUsingItem(Object instance) {
        cast(instance, EntityPlayer.class).stopUsingItem();
    }
}
