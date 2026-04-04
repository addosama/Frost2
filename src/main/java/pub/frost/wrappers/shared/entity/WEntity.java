package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import pub.frost.base.wrapping.impl.InstanceWrapper;
import pub.frost.utils.MathUtils;
import pub.frost.utils.data.BoundingBox;
import pub.frost.wrappers.FakeInstanceWrapper;

import javax.vecmath.Vector3d;

public class WEntity extends InstanceWrapper implements FakeInstanceWrapper<Entity> {
    public WEntity(Object wrappedObject) {
        super(wrappedObject);
    }

    public boolean isSprinting() {
        return cast().isSprinting();
    }
    public void setSprinting(boolean sprinting) {
        cast().setSprinting(sprinting);
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

    public float getYaw() {
        return cast().rotationYaw;
    }
    public float getPitch() {
        return cast().rotationPitch;
    }

    public Vector3d getPositionVector() {
        return new Vector3d(getX(), getY(), getZ());
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
}
