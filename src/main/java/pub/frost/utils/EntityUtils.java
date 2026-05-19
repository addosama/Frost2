package pub.frost.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import org.joml.Vector3d;
import pub.frost.base.event.impl.events.EventTestPlayerLookingEntity;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.wrappers.shared.item.WItem;

import java.util.List;

public class EntityUtils implements Wrappers {
    public static String tryGetDisplayName(Object entity) {
        return Entity.getDisplayName(entity);
    }

    public static BoundingBox getBoundingBoxAtPosition(Object entity, Vector3d position) {
        double x = position.x;
        double y = position.y;
        double z = position.z;
        float width = Entity.getWidth(entity);
        float height = Entity.getHeight(entity);

        return new BoundingBox(
                x - width / 2,
                y,
                z - width / 2,
                x + width / 2,
                y + height,
                z + width / 2
        );
    }
    public static boolean isHoldingItem(Object livingEntity, WItem targetItem) {
        Object itemHeld = EntityLivingBase.getHeldItem(livingEntity);
        if (itemHeld == null) return false;
        return targetItem.isTarget(
                ItemStack.getItem(itemHeld)
        );
    }

    public static HitResult getLookingObject(Object instance, Vector3d lookingVec, double reachDistance, float tickDelta) {
        return getLookingObject(
                instance, lookingVec, reachDistance, tickDelta,
                true
        );
    }

    public static HitResult getLookingObject(
            Object instance, Vector3d lookingVec, double reachDistance, float tickDelta,
            boolean callEvent
    ) {
        HitResult objectMouseOver;
        Object pointedEntity = null;

        Vector3d eyePos = Entity.getPositionEyes(instance, tickDelta);
        objectMouseOver = Entity.raytraceBlocks(instance, eyePos, lookingVec, reachDistance);

        double d1 = reachDistance;
        boolean flag = reachDistance > 3.0D;

        if (objectMouseOver != null) {
            d1 = objectMouseOver.getHitVec().distance(eyePos);
        }

        Vector3d vec32 = new Vector3d(eyePos).add(
                lookingVec.x() * reachDistance,
                lookingVec.y() * reachDistance,
                lookingVec.z() * reachDistance
        );
        Vector3d vec33 = null;
        float f = 1.0F;
        List<Object> list = World.getEntitiesInAABBExcluding(
                Entity.getWorld(instance),
                instance,
                Entity.getBoundingBox(instance).addCoord(
                        lookingVec.x() * reachDistance,
                        lookingVec.y() * reachDistance,
                        lookingVec.z() * reachDistance
                ).expand(f, f, f),
                en -> EntitySelectors.NOT_SPECTATING.apply((Entity) en) && Entity.canBeCollidedWith(en)
        );
        double d2 = d1;

        for (Object entity : list) {
            float f1 = Entity.getCollisionBorderSize(entity);
            EventTestPlayerLookingEntity event;
            {
                event = new EventTestPlayerLookingEntity(
                        tickDelta, instance, entity, eyePos,
                        lookingVec, reachDistance,
                        Entity.getBoundingBox(entity).expand(f1, f1, f1)
                );
                if (callEvent) FrostCore.getEventBus().call(event);
            }
            BoundingBox boundingBox = event.getHitbox();
            HitResult hitResult = event.isUseDefaultHitResult()? boundingBox.calculateIntercept(eyePos, vec32) : event.getHitResult();
            if (boundingBox.isVecInside(eyePos)) {
                if (d2 >= 0.0D) {
                    pointedEntity = entity;
                    vec33 = hitResult == null ? eyePos : hitResult.getHitVec();
                    d2 = 0.0D;
                }
            } else if (hitResult != null) {
                double d3 = eyePos.distance(hitResult.getHitVec());
                if (d3 < d2 || d2 == 0.0D) {
                    if (entity == Entity.getRidingEntity(instance) && !Entity.canRiderInteract(instance)) {
                        if (d2 == 0.0D) {
                            pointedEntity = entity;
                            vec33 = hitResult.getHitVec();
                        }
                    } else {
                        pointedEntity = entity;
                        vec33 = hitResult.getHitVec();
                        d2 = d3;
                    }
                }
            }
        }

        if (pointedEntity != null && flag && eyePos.distance(vec33) > 3.0D) {
            pointedEntity = null;
            objectMouseOver = HitResult.buildMissHit(new BlockPosition(vec33), null, vec33);
        }

        if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
            objectMouseOver = HitResult.buildEntityHit(
                    pointedEntity,
                    null, null,
                    vec33
            );
        }

        return objectMouseOver;
    }
}
