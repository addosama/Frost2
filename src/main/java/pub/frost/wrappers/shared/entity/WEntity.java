package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import pub.frost.base.wrapping.impl.InstanceWrapper;
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
}
