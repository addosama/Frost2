package pub.frost.utils.data.raytrace.impl;

import lombok.Getter;
import org.joml.Vector3d;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.EnumDirection;
import pub.frost.utils.data.raytrace.HitResult;

@Getter
public class EntityHitResult extends HitResult {
    private final Object hitEntity;
    public EntityHitResult(BlockPosition blockPos, EnumDirection hitDirection, Vector3d hitVec, Object hitEntity) {
        super(EnumHitType.ENTITY, blockPos, hitDirection, hitVec);
        this.hitEntity = hitEntity;
    }
}
