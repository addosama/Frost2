package pub.frost.wrappers.shared.block;

import net.minecraft.block.state.IBlockState;
import pub.frost.base.wrapping.legacy.impl.InstanceWrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WIBlockState extends InstanceWrapper implements FakeInstanceWrapper<IBlockState> {
    public WIBlockState(Object wrappedObject) {
        super(wrappedObject);
    }

    public WBlock getBlock() {
        return new WBlock(cast().getBlock());
    }
}
