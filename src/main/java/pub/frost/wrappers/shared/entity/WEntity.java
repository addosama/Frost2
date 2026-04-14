package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.utils.MathUtils;
import pub.frost.utils.RotationUtils;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.wrappers.FakeInstanceWrapper;

import org.joml.Vector3d;
import pub.frost.wrappers.shared.world.WWorld;

import java.util.List;

public class WEntity extends Wrapper implements FakeInstanceWrapper<Entity>, Wrappers {
    public WEntity() {
        super(Entity.class);
    }

    public WEntity(Class<?> targetClass) {
        super(targetClass);
    }

    public Object getWorld(Object instance) {
        return cast(instance, Entity.class).worldObj;
    }

    public boolean isDead(Object instance) {
        return cast(instance, Entity.class).isDead;
    }

    public boolean isSprinting(Object instance) {
        return cast(instance, Entity.class).isSprinting();
    }
    public void setSprinting(Object instance, boolean sprinting) {
        cast(instance, Entity.class).setSprinting(sprinting);
    }

    public float getEyesHeight(Object instance) {
        return cast(instance, Entity.class).getEyeHeight();
    }
    public Vector3d getPositionEyes(Object instance, float tickDelta) {
        Vec3 pos = cast(instance, Entity.class).getPositionEyes(tickDelta);
        return new Vector3d(
                pos.xCoord, pos.yCoord, pos.zCoord
        );
    }

    public String getName(Object instance) {
        return cast(instance, Entity.class).getName();
    }
    public String getDisplayName(Object instance) {
        return cast(instance, Entity.class).getDisplayName().getFormattedText();
    }

    public double getPrevX(Object instance) {
        return cast(instance, Entity.class).prevPosX;
    }
    public double getPrevY(Object instance) {
        return cast(instance, Entity.class).prevPosY;
    }
    public double getPrevZ(Object instance) {
        return cast(instance, Entity.class).prevPosZ;
    }

    public double getX(Object instance) {
        return cast(instance, Entity.class).posX;
    }
    public double getY(Object instance) {
        return cast(instance, Entity.class).posY;
    }
    public double getZ(Object instance) {
        return cast(instance, Entity.class).posZ;
    }

    public double getLerpedX(Object instance, float tickDelta) {
        return MathUtils.lerp(getPrevX(instance), getX(instance), tickDelta);
    }
    public double getLerpedY(Object instance, float tickDelta) {
        return MathUtils.lerp(getPrevY(instance), getY(instance), tickDelta);
    }
    public double getLerpedZ(Object instance, float tickDelta) {
        return MathUtils.lerp(getPrevZ(instance), getZ(instance), tickDelta);
    }

    public float getPrevYaw(Object instance) {
        return cast(instance, Entity.class).prevRotationYaw;
    }
    public float getPrevPitch(Object instance) {
        return cast(instance, Entity.class).prevRotationPitch;
    }
    public void setPrevYaw(Object instance, float prevYaw) {
        cast(instance, Entity.class).prevRotationYaw = prevYaw;
    }
    public void setPrevPitch(Object instance, float prevPitch) {
        cast(instance, Entity.class).prevRotationPitch = prevPitch;
    }

    public float getYaw(Object instance) {
        return cast(instance, Entity.class).rotationYaw;
    }
    public float getPitch(Object instance) {
        return cast(instance, Entity.class).rotationPitch;
    }
    public void setYaw(Object instance, float yaw) {
        cast(instance, Entity.class).rotationYaw = yaw;
    }
    public void setPitch(Object instance, float pitch) {
        cast(instance, Entity.class).rotationPitch = pitch;
    }

    public Vector3d getLook(Object instance, float tickDelta) {
        Vec3 ret = cast(instance, Entity.class).getLook(tickDelta);
        return new Vector3d(
                ret.xCoord, ret.yCoord, ret.zCoord
        );
    }

    public Vector3d getPositionVector(Object instance) {
        return new Vector3d(getX(instance), getY(instance), getZ(instance));
    }
    public Vector3d getLerpedPositionVector(Object instance, float tickDelta) {
        return new Vector3d(
                getLerpedX(instance, tickDelta),
                getLerpedY(instance, tickDelta),
                getLerpedZ(instance, tickDelta)
        );
    }

    public float getWidth(Object instance) {
        return cast(instance, Entity.class).width;
    }
    public float getHeight(Object instance) {
        return cast(instance, Entity.class).height;
    }
    public float getEyeHeight(Object instance) {
        return cast(instance, Entity.class).getEyeHeight();
    }

    public double distanceTo(Object instance, double x, double y, double z) {
        return cast(instance, Entity.class).getDistance(x, y, z);
    }
    public double distanceTo(Object instance, Vector3d pos) {
        return distanceTo(instance, pos.x, pos.y, pos.z);
    }

    public BoundingBox getBoundingBox(Object instance) {
        double x = getX(instance);
        double y = getY(instance);
        double z = getZ(instance);
        float width = getWidth(instance);
        float height = getHeight(instance);

        return new BoundingBox(
                x - width / 2,
                y,
                z - width / 2,
                x + width / 2,
                y + height,
                z + width / 2
        );
    }
    public BoundingBox getPrevBoundingBox(Object instance) {
        double x = getPrevX(instance);
        double y = getPrevY(instance);
        double z = getPrevZ(instance);
        float width = getWidth(instance);
        float height = getHeight(instance);

        return new BoundingBox(
                x - width / 2,
                y,
                z - width / 2,
                x + width / 2,
                y + height,
                z + width / 2
        );
    }
    public BoundingBox getLerpedBoundingBox(Object instance, float delta) {
        double x = MathUtils.lerp(getPrevX(instance), getX(instance), delta);
        double y = MathUtils.lerp(getPrevY(instance), getY(instance), delta);
        double z = MathUtils.lerp(getPrevZ(instance), getZ(instance), delta);
        float width = getWidth(instance);
        float height = getHeight(instance);

        return new BoundingBox(
                x - width / 2,
                y,
                z - width / 2,
                x + width / 2,
                y + height,
                z + width / 2
        );
    }

    public boolean isInvisible(Object instance) {
        return cast(instance, Entity.class).isInvisible();
    }

    public HitResult raytraceBlocks(Object instance, Vector3d eyePos, Vector3d lookVec, double blockReachDistance) {
        Vector3d vec32 = new Vector3d(eyePos).add(
                lookVec.x() * blockReachDistance,
                lookVec.y() * blockReachDistance,
                lookVec.z() * blockReachDistance
        );
        return World.raytraceBlocks(
                getWorld(instance),
                eyePos, vec32, false, false, true
        );
    }

    public boolean canBeCollidedWith(Object instance) {
        return cast(instance, Entity.class).canBeCollidedWith();
    }
    public float getCollisionBorderSize(Object instance) {
        return cast(instance, Entity.class).getCollisionBorderSize();
    }

    public HitResult rayTrace(Object instance, Vector3d lookingVec, double reachDistance, float partialTicks) {
        HitResult objectMouseOver;
        Object pointedEntity = null;

        Vector3d eyePos = getPositionEyes(instance, partialTicks);
        objectMouseOver = raytraceBlocks(instance, eyePos, lookingVec, reachDistance);

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
                getWorld(instance),
                instance,
                this.getBoundingBox(instance).addCoord(
                        lookingVec.x() * reachDistance,
                        lookingVec.y() * reachDistance,
                        lookingVec.z() * reachDistance
                ).expand(f, f, f),
                en -> EntitySelectors.NOT_SPECTATING.apply((Entity) en) && canBeCollidedWith(en)
        );
        double d2 = d1;

        for (Object entity : list) {
            float f1 = getCollisionBorderSize(entity);
            BoundingBox boundingBox = getBoundingBox(entity).expand(f1, f1, f1);
            HitResult hitResult = boundingBox.calculateIntercept(eyePos, vec32);
            if (boundingBox.isVecInside(eyePos)) {
                if (d2 >= 0.0D) {
                    pointedEntity = entity;
                    vec33 = hitResult == null ? eyePos : hitResult.getHitVec();
                    d2 = 0.0D;
                }
            } else if (hitResult != null) {
                double d3 = eyePos.distance(hitResult.getHitVec());
                if (d3 < d2 || d2 == 0.0D) {
                    if (entity == this.getRidingEntity(instance) && !this.canRiderInteract(instance)) {
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

    public Object getRidingEntity(Object instance) {
        return cast(instance, Entity.class).ridingEntity;
    }
    public boolean canRiderInteract(Object instance) {
        return cast(instance, Entity.class).canRiderInteract();
    }
}
