package pub.frost.wrappers.shared.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WItemStack extends Wrapper implements FakeInstanceWrapper<ItemStack> {
    public WItemStack() {
        super(ItemStack.class);
    }

    public Object getItem(Object instance) {
        return cast(instance).getItem();
    }

    public boolean canHarvestBlock(Object instance, Object block) {
        return cast(instance).canHarvestBlock((Block) block);
    }
    public float getStrVsBlock(Object instance, Object block) {
        return cast(instance).getStrVsBlock((Block) block);
    }
}
