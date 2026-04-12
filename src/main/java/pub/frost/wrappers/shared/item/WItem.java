package pub.frost.wrappers.shared.item;

import net.minecraft.item.Item;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WItem extends Wrapper implements FakeInstanceWrapper<Item> {
    public WItem() {
        super(Item.class);
    }
    protected WItem(Class<?> targetClass) {
        super(targetClass);
    }
}
