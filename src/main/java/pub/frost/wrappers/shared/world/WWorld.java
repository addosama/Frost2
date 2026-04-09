package pub.frost.wrappers.shared.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.joml.Vector3d;
import pub.frost.base.wrapping.impl.InstanceWrapper;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.EnumDirection;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.wrappers.FakeInstanceWrapper;
import pub.frost.wrappers.shared.entity.WEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class WWorld extends InstanceWrapper implements FakeInstanceWrapper<World> {
    public WWorld(Object wrappedObject) {
        super(wrappedObject);
    }

    public List<WEntity> getLoadedEntityList() {
        List<WEntity> list = new ArrayList<>();
        cast().getLoadedEntityList().forEach(
                en -> list.add(new WEntity(en))
        );
        return list;
    }

    public List<WEntity> getEntitiesInAABBExcluding(WEntity entityIn, BoundingBox boundingBox, Predicate<? super WEntity> predicate) {
        List<WEntity> list = new ArrayList<>();
        cast().getEntitiesInAABBexcluding(
                (Entity) entityIn.getWrappedObject(),
                new AxisAlignedBB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                e -> predicate.test(new WEntity(e))
        ).forEach(entity -> list.add(new WEntity(entity)));
        return list;
    }

    public HitResult raytraceBlocks(Vector3d start, Vector3d end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLast) {
        MovingObjectPosition result = cast().rayTraceBlocks(
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
                case ENTITY: return HitResult.buildEntityHit(new WEntity(result.entityHit), blockPos, direction, hitVec);
                case BLOCK: return HitResult.buildBlockHit(blockPos, direction, hitVec);
                case MISS: return HitResult.buildMissHit(blockPos, direction, hitVec);
            }
        }
        return null;
    }
}
