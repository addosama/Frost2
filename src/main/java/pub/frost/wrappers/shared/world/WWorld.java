package pub.frost.wrappers.shared.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.joml.Vector3d;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.EnumDirection;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.wrappers.FakeInstanceWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class WWorld extends Wrapper implements FakeInstanceWrapper<World> {
    public WWorld() {
        super(World.class);
    }

    public List<Object> getLoadedEntityList(Object instance) {
        return new ArrayList<>(cast(instance).getLoadedEntityList());
    }

    public List<Object> getLoadedTileEntityList(Object instance) {
        return new ArrayList<>(cast(instance).loadedTileEntityList);
    }

    public List<Object> getEntitiesInAABBExcluding(Object instance, Object entityIn, BoundingBox boundingBox, Predicate<? super Object> predicate) {
        return new ArrayList<>(cast(instance).getEntitiesInAABBexcluding(
                (Entity) entityIn,
                new AxisAlignedBB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                predicate::test
        ));
    }

    public List<BoundingBox> getBlockCollisionBoxes(Object instance, BoundingBox box) {
        return cast(instance).getCollisionBoxes(
                new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
        ).stream().collect(
                ArrayList::new,
                (list, aabb) -> list.add(new BoundingBox(
                        aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ
                )),
                ArrayList::addAll
        );
    }
    public List<BoundingBox> getCollidingBoundingBoxes(Object instance, Object entityToCollide, BoundingBox box) {
        return cast(instance).getCollidingBoundingBoxes(
                (Entity) entityToCollide, new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
        ).stream().collect(
                ArrayList::new,
                (list, aabb) -> list.add(new BoundingBox(
                        aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ
                )),
                ArrayList::addAll
        );
    }

    public HitResult raytraceBlocks(Object instance, Vector3d start, Vector3d end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLast) {
        MovingObjectPosition result = cast(instance).rayTraceBlocks(
                new Vec3(start.x, start.y, start.z),
                new Vec3(end.x, end.y, end.z),
                stopOnLiquid, ignoreBlockWithoutBoundingBox, returnLast
        );
        if (result != null) {
            BlockPosition blockPos; {
                BlockPos pos = result.getBlockPos();
                blockPos = new BlockPosition(pos.getX(), pos.getY(), pos.getZ());
            }
            Vector3d hitVec; {
                Vec3 vec = result.hitVec;
                hitVec = new Vector3d(vec.xCoord,  vec.yCoord, vec.zCoord);
            }
            EnumDirection direction = EnumDirection.getByIndex(result.sideHit.getIndex());
            switch (result.typeOfHit) {
                case ENTITY: return HitResult.buildEntityHit(result.entityHit, blockPos, direction, hitVec);
                case BLOCK: return HitResult.buildBlockHit(blockPos, direction, hitVec);
                case MISS: return HitResult.buildMissHit(blockPos, direction, hitVec);
            }
        }
        return null;
    }

    public Object getBlockState(Object instance, BlockPosition pos) {
        return cast(instance).getBlockState(new BlockPos(pos.x, pos.y, pos.z));
    }
    public boolean isAirBlock(Object instance, BlockPosition pos) {
        return cast(instance).isAirBlock(new BlockPos(pos.x, pos.y, pos.z));
    }

    public Object getEntityById(Object instance, int id) {
        return cast(instance).getEntityByID(id);
    }
}
