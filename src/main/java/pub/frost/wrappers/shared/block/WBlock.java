package pub.frost.wrappers.shared.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.BoundingBox;
import pub.frost.wrappers.FakeInstanceWrapper;

import java.util.ArrayList;
import java.util.List;

public class WBlock extends Wrapper implements FakeInstanceWrapper<Block> {
    public WBlock() {
        super(Block.class);
    }

    public boolean isCollidable(Object instance) {
        return cast(instance).isCollidable();
    }

    public List<BoundingBox> getCollisionBoxes(
            Object instance,
            Object worldIn,
            BlockPosition pos,
            Object state,
            BoundingBox mask,
            Object collidingEntity
    ) {
        List<AxisAlignedBB> boxList = new ArrayList<>();
        cast(instance).addCollisionBoxesToList(
                (World) worldIn,
                new BlockPos(pos.x, pos.y, pos.z),
                (IBlockState) state,
                new AxisAlignedBB(
                        mask.minX, mask.minY, mask.minZ,
                        mask.maxX, mask.maxY, mask.maxZ
                ),
                boxList,
                (Entity) collidingEntity
        );
        return boxList.stream().collect(
                ArrayList::new,
                (list, box) -> new BoundingBox(
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ
                ),
                ArrayList::addAll
        );
    }
}
