package pub.frost.wrappers.shared.block;

import net.minecraft.block.state.IBlockState;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WIBlockState extends Wrapper implements FakeInstanceWrapper<IBlockState> {
    public WIBlockState() {
        super(IBlockState.class);
    }

    public Object getBlock(Object instance) {
        return cast(instance).getBlock();
    }
}
