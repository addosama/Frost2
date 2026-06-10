package pub.frost.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import pub.frost.base.event.impl.events.EventTestPlayerLookingEntity;
import pub.frost.client.core.FrostCore;

import java.util.List;

public class EntityUtils {
    public static String tryGetDisplayName(Entity entity) {
        return entity.getDisplayName().getFormattedText();
    }

    public static AxisAlignedBB getPrevBoundingBox(Entity entity) {
        return getBoundingBoxAtPosition(entity, entity.prevPosX, entity.prevPosY, entity.prevPosZ);
    }
    public static AxisAlignedBB getBoundingBoxAtPosition(Entity entity, double x, double y, double z) {
        float width = entity.width;
        float height = entity.height;

        return new AxisAlignedBB(
                x - width / 2,
                y,
                z - width / 2,
                x + width / 2,
                y + height,
                z + width / 2
        );
    }
    public static AxisAlignedBB getBoundingBoxAtPosition(Entity entity, Vec3 position) {
        return getBoundingBoxAtPosition(entity, position.xCoord, position.yCoord, position.zCoord);
    }
    public static boolean isHoldingItem(EntityLivingBase livingEntity, Class<? extends Item> targetItem) {
        ItemStack itemHeld = livingEntity.getHeldItem();
        if (itemHeld == null) return false;
        return targetItem.isInstance(itemHeld.getItem());
    }

    public static MovingObjectPosition getLookingObject(Entity instance, Vec3 lookingVec, double reachDistance, float tickDelta) {
        return getLookingObject(
                instance, lookingVec, reachDistance, tickDelta,
                true
        );
    }

    public static MovingObjectPosition getLookingObject(
            Entity instance, Vec3 lookingVec, double reachDistance, float tickDelta,
            boolean callEvent
    ) {
        MovingObjectPosition objectMouseOver;
        Entity pointedEntity = null;

        Vec3 eyePos = getPositionEyes(instance, tickDelta);
        Vec3 reachEnd = eyePos.addVector(
                lookingVec.xCoord * reachDistance,
                lookingVec.yCoord * reachDistance,
                lookingVec.zCoord * reachDistance
        );
        objectMouseOver = instance.worldObj.rayTraceBlocks(eyePos, reachEnd, false, true, true);

        double d1 = reachDistance;
        boolean flag = reachDistance > 3.0D;

        if (objectMouseOver != null) {
            d1 = objectMouseOver.hitVec.distanceTo(eyePos);
        }

        Vec3 vec32 = eyePos.addVector(
                lookingVec.xCoord * reachDistance,
                lookingVec.yCoord * reachDistance,
                lookingVec.zCoord * reachDistance
        );
        Vec3 vec33 = null;
        float f = 1.0F;

        // expand reach AABB
        AxisAlignedBB entityBB = instance.getEntityBoundingBox();
        AxisAlignedBB reachBB = new AxisAlignedBB(
                entityBB.minX, entityBB.minY, entityBB.minZ,
                entityBB.maxX, entityBB.maxY, entityBB.maxZ
        );
        reachBB = new AxisAlignedBB(
                Math.min(reachBB.minX, reachBB.minX + lookingVec.xCoord * reachDistance),
                Math.min(reachBB.minY, reachBB.minY + lookingVec.yCoord * reachDistance),
                Math.min(reachBB.minZ, reachBB.minZ + lookingVec.zCoord * reachDistance),
                Math.max(reachBB.maxX, reachBB.maxX + lookingVec.xCoord * reachDistance),
                Math.max(reachBB.maxY, reachBB.maxY + lookingVec.yCoord * reachDistance),
                Math.max(reachBB.maxZ, reachBB.maxZ + lookingVec.zCoord * reachDistance)
        ).expand(f, f, f);

        List<Entity> list = instance.worldObj.getEntitiesInAABBexcluding(
                instance, reachBB,
                e -> EntitySelectors.NOT_SPECTATING.apply(e) && e.canBeCollidedWith()
        );
        double d2 = d1;

        for (Entity entity : list) {
            float f1 = entity.getCollisionBorderSize();
            AxisAlignedBB hitBB = entity.getEntityBoundingBox().expand(f1, f1, f1);
            EventTestPlayerLookingEntity event;
            {
                event = new EventTestPlayerLookingEntity(
                        tickDelta, instance, entity, eyePos,
                        lookingVec, reachDistance,
                        hitBB
                );
                if (callEvent) FrostCore.getEventBus().call(event);
            }
            AxisAlignedBB boundingBox = event.getHitbox();
            MovingObjectPosition hitResult = event.isUseDefualtResult()
                    ? boundingBox.calculateIntercept(eyePos, vec32)
                    : event.getHitResult();
            if (boundingBox.isVecInside(eyePos)) {
                if (d2 >= 0.0D) {
                    pointedEntity = entity;
                    vec33 = hitResult == null ? eyePos : hitResult.hitVec;
                    d2 = 0.0D;
                }
            } else if (hitResult != null) {
                double d3 = eyePos.distanceTo(hitResult.hitVec);
                if (d3 < d2 || d2 == 0.0D) {
                    if (entity == instance.ridingEntity && !instance.canRiderInteract()) {
                        if (d2 == 0.0D) {
                            pointedEntity = entity;
                            vec33 = hitResult.hitVec;
                        }
                    } else {
                        pointedEntity = entity;
                        vec33 = hitResult.hitVec;
                        d2 = d3;
                    }
                }
            }
        }

        if (pointedEntity != null && flag && eyePos.distanceTo(vec33) > 3.0D) {
            pointedEntity = null;
            objectMouseOver = new MovingObjectPosition(
                    MovingObjectPosition.MovingObjectType.MISS,
                    vec33, null, new BlockPos(
                    (int)Math.floor(vec33.xCoord),
                    (int)Math.floor(vec33.yCoord),
                    (int)Math.floor(vec33.zCoord)
            )
            );
        }

        if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
            objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
        }

        return objectMouseOver;
    }

    public static Vec3 getPositionEyes(Entity entity, float tickDelta) {
        if (tickDelta == 1.0F) {
            return new Vec3(entity.posX, entity.posY + (double)entity.getEyeHeight(), entity.posZ);
        } else {
            double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)tickDelta;
            double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)tickDelta + (double)entity.getEyeHeight();
            double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)tickDelta;
            return new Vec3(d0, d1, d2);
        }
    }

    public static Vec3 getLerpedPositionVector(Entity entity, float tickDelta) {
        return new Vec3(
            MathUtils.lerp(entity.prevPosX, entity.posX, tickDelta),
            MathUtils.lerp(entity.prevPosY, entity.posY, tickDelta),
            MathUtils.lerp(entity.prevPosZ, entity.posZ, tickDelta)
        );
    }

    public static double getDistanceToPoint(Entity entity, Vec3 point) {
        return entity.getDistance(point.xCoord, point.yCoord, point.zCoord);
    }

}
