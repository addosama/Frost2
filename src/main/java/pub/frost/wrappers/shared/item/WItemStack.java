package pub.frost.wrappers.shared.item;

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
}
