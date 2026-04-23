package pub.frost.wrappers.shared.block;

import net.minecraft.block.ITileEntityProvider;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WITileEntityProvider extends Wrapper implements FakeInstanceWrapper<ITileEntityProvider> {
    public WITileEntityProvider(Class<?> targetClass) {
        super(targetClass);
    }
    public WITileEntityProvider() {
        super(ITileEntityProvider.class);
    }
}
