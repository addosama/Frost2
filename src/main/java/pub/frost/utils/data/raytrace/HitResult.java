package pub.frost.utils.data.raytrace;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3d;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.EnumDirection;
import pub.frost.utils.data.raytrace.impl.EntityHitResult;
import pub.frost.wrappers.shared.entity.WEntity;

@RequiredArgsConstructor @Getter
public class HitResult {
    private final EnumHitType type;
    private final BlockPosition blockPos;
    private final EnumDirection hitDirection;
    private final Vector3d hitVec;

    public enum EnumHitType {
        ENTITY,
        BLOCK,
        MISS
    }

    public static EntityHitResult buildEntityHit(Object entity, BlockPosition blockPos, EnumDirection hitDirection, Vector3d hitVec) {
        return new EntityHitResult(blockPos, hitDirection, hitVec, entity);
    }
    public static HitResult buildBlockHit(BlockPosition blockPos, EnumDirection hitDirection, Vector3d hitVec) {
        return new HitResult(EnumHitType.BLOCK, blockPos, hitDirection, hitVec);
    }
    public static HitResult buildMissHit(BlockPosition blockPos, EnumDirection hitDirection, Vector3d hitVec) {
        return new HitResult(EnumHitType.MISS, blockPos, hitDirection, hitVec);
    }
}
