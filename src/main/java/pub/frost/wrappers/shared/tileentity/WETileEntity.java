package pub.frost.wrappers.shared.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.BoundingBox;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WETileEntity extends Wrapper implements FakeInstanceWrapper<TileEntity> {
    public WETileEntity() {
        super(TileEntity.class);
    }

    public boolean isChest(Object instance) {
        return instance instanceof TileEntityChest || instance instanceof TileEntityEnderChest;
    }

    public boolean isEnderChest(Object instance) {
        return instance instanceof TileEntityEnderChest;
    }

    public BlockPosition getPosition(Object instance) {
        BlockPos pos = cast(instance).getPos();
        return new BlockPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    public BoundingBox getBoundingBox(Object instance) {
        BlockPos pos = cast(instance).getPos();
        return new BoundingBox(
                pos.getX() + 0.0625,
                pos.getY(),
                pos.getZ() + 0.0625,
                pos.getX() + 0.9375,
                pos.getY() + 0.875,
                pos.getZ() + 0.9375
        );
    }

    public double distanceTo(Object instance, double x, double y, double z) {
        BlockPos pos = cast(instance).getPos();
        double dx = pos.getX() + 0.5 - x;
        double dy = pos.getY() + 0.5 - y;
        double dz = pos.getZ() + 0.5 - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
