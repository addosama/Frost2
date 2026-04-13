package pub.frost.wrappers.shared.player;

import net.minecraft.entity.player.InventoryPlayer;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WInventoryPlayer extends Wrapper implements FakeInstanceWrapper<InventoryPlayer> {
    public WInventoryPlayer() {
        super(InventoryPlayer.class);
    }

    public int getCurrentItem(Object instance) {
        return cast(instance).currentItem;
    }

    public void setCurrentItem(Object instance, int item) {
        cast(instance).currentItem = item;
    }

    public Object getStackInSlot(Object instance, int slot) {
        return cast(instance).getStackInSlot(slot);
    }
}
