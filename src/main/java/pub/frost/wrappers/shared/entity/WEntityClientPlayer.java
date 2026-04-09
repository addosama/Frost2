package pub.frost.wrappers.shared.entity;

import net.minecraft.client.entity.EntityPlayerSP;

public class WEntityClientPlayer extends WEntityPlayer {
    public WEntityClientPlayer(Object obj) {
        super(obj);
    }
    @Override
    public EntityPlayerSP cast() {
        return cast(getWrappedObject(), EntityPlayerSP.class);
    }

    public float getRenderArmYaw() {
        return cast().renderArmYaw;
    }
    public float getRenderArmPitch() {
        return cast().renderArmPitch;
    }
    public float getPrevRenderArmYaw() {
        return cast().prevRenderArmYaw;
    }
    public float getPrevRenderArmPitch() {
        return cast().prevRenderArmPitch;
    }

    public void setRenderArmYaw(float yaw) {
        cast().renderArmYaw = yaw;
    }
    public void setRenderArmPitch(float pitch) {
        cast().renderArmPitch = pitch;
    }
    public void setPrevRenderArmYaw(float yaw) {
        cast().prevRenderArmYaw = yaw;
    }
    public void setPrevRenderArmPitch(float pitch) {
        cast().prevRenderArmPitch = pitch;
    }
}
