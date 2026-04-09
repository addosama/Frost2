package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import pub.frost.base.wrapping.impl.InstanceWrapper;
import pub.frost.utils.MathUtils;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.wrappers.FakeInstanceWrapper;

import org.joml.Vector3d;
import pub.frost.wrappers.shared.world.WWorld;

import java.util.List;

public class WEntity extends InstanceWrapper implements FakeInstanceWrapper<Entity> {
    public WEntity(Object wrappedObject) {
        super(wrappedObject);
    }

    public WWorld getWorld() {
        return new WWorld(cast().worldObj);
    }

    public boolean isDead() {
        return cast().isDead;
    }

    public boolean isSprinting() {
        return cast().isSprinting();
    }
    public void setSprinting(boolean sprinting) {
        cast().setSprinting(sprinting);
    }

    public float getEyesHeight() {
        return cast().getEyeHeight();
    }
    public Vector3d getPositionEyes(float tickDelta) {
        Vec3 pos = cast().getPositionEyes(tickDelta);
        return new Vector3d(
                pos.xCoord, pos.yCoord, pos.zCoord
        );
    }

    public String getName() {
        return cast().getName();
    }

    public double getPrevX() {
        return cast().prevPosX;
    }
    public double getPrevY() {
        return cast().prevPosY;
    }
    public double getPrevZ() {
        return cast().prevPosZ;
    }

    public double getX() {
        return cast().posX;
    }
    public double getY() {
        return cast().posY;
    }
    public double getZ() {
        return cast().posZ;
    }

    public double getLerpedX(float tickDelta) {
        return MathUtils.lerp(getPrevX(), getX(), tickDelta);
    }
    public double getLerpedY(float tickDelta) {
        return MathUtils.lerp(getPrevY(), getY(), tickDelta);
    }
    public double getLerpedZ(float tickDelta) {
        return MathUtils.lerp(getPrevZ(), getZ(), tickDelta);
    }

    public float getPrevYaw() {
        return cast().prevRotationYaw;
    }
    public float getPrevPitch() {
        return cast().prevRotationPitch;
    }

    public float getYaw() {
        return cast().rotationYaw;
    }
    public float getPitch() {
        return cast().rotationPitch;
    }
    public void setYaw(float yaw) {
        cast().rotationYaw = yaw;
    }
    public void setPitch(float pitch) {
        cast().rotationPitch = pitch;
    }

    public Vector3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vector3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

    public Vector3d getLook(float tickDelta) {
        if (tickDelta == 1.0F) {
            return this.getVectorForRotation(getPitch(), getYaw());
        } else {
            float f = getPrevPitch() + (getPitch() - getPrevPitch()) * tickDelta;
            float f1 = getPrevYaw() + (getYaw() - getPrevYaw()) * tickDelta;
            return this.getVectorForRotation(f, f1);
        }
    }

    public Vector3d getPositionVector() {
        return new Vector3d(getX(), getY(), getZ());
    }
    public Vector3d getLerpedPositionVector(float tickDelta) {
        return new Vector3d(
                getLerpedX(tickDelta),
                getLerpedY(tickDelta),
                getLerpedZ(tickDelta)
        );
    }

    public float getWidth() {
        return cast().width;
    }
    public float getHeight() {
        return cast().height;
    }
    public float getEyeHeight() {
        return cast().getEyeHeight();
    }

    public double distanceTo(double x, double y, double z) {
        return cast().getDistance(x, y, z);
    }
    public double distanceTo(Vector3d pos) {
        return distanceTo(pos.x, pos.y, pos.z);
    }

    public BoundingBox getBoundingBox() {
        double x = getX();
        double y = getY();
        double z = getZ();
        float width = getWidth();
        float height = getHeight();

        return new BoundingBox(
                x - width / 2,
                y,
                z - width / 2,
                x + width / 2,
                y + height,
                z + width / 2
        );
    }
    public BoundingBox getPrevBoundingBox() {
        double x = getPrevX();
        double y = getPrevY();
        double z = getPrevZ();
        float width = getWidth();
        float height = getHeight();

        return new BoundingBox(
                x - width / 2,
                y,
                z - width / 2,
                x + width / 2,
                y + height,
                z + width / 2
        );
    }
    public BoundingBox getLerpedBoundingBox(float delta) {
        double x = MathUtils.lerp(getPrevX(), getX(), delta);
        double y = MathUtils.lerp(getPrevY(), getY(), delta);
        double z = MathUtils.lerp(getPrevZ(), getZ(), delta);
        float width = getWidth();
        float height = getHeight();

        return new BoundingBox(
                x - width / 2,
                y,
                z - width / 2,
                x + width / 2,
                y + height,
                z + width / 2
        );
    }

    public boolean isInvisible() {
        return cast().isInvisible();
    }

    public HitResult raytraceBlocks(Vector3d eyePos, Vector3d lookVec, double blockReachDistance) {
        Vector3d vec32 = new Vector3d(eyePos).add(
                lookVec.x() * blockReachDistance,
                lookVec.y() * blockReachDistance,
                lookVec.z() * blockReachDistance
        );
        return getWorld().raytraceBlocks(eyePos, vec32, false, false, true);
    }

    public boolean canBeCollidedWith() {
        return cast().canBeCollidedWith();
    }
    public float getCollisionBorderSize() {
        return cast().getCollisionBorderSize();
    }

    public HitResult rayTrace(Vector3d lookingVec, double reachDistance, float partialTicks) {
        HitResult objectMouseOver;
        WEntity pointedEntity = null;

        Vector3d eyePos = getPositionEyes(partialTicks);
        objectMouseOver = raytraceBlocks(eyePos, lookingVec, reachDistance);

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
        List<WEntity> list = getWorld().getEntitiesInAABBExcluding(
                this,
                this.getBoundingBox().addCoord(
                        lookingVec.x() * reachDistance,
                        lookingVec.y() * reachDistance,
                        lookingVec.z() * reachDistance
                ).expand(f, f, f),
                en -> EntitySelectors.NOT_SPECTATING.apply(en.cast()) && en.canBeCollidedWith()
        );
        double d2 = d1;

        for (WEntity entity : list) {
            float f1 = entity.getCollisionBorderSize();
            BoundingBox boundingBox = entity.getBoundingBox().expand(f1, f1, f1);
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
                    if (entity == this.getRidingEntity() && !this.canRiderInteract()) {
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

    public WEntity getRidingEntity() {
        Entity riding = cast().ridingEntity;
        return riding != null? new WEntity(riding): null;
    }
    public boolean canRiderInteract() {
        return cast().canRiderInteract();
    }
}
