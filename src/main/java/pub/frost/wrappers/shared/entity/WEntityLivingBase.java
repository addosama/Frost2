package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.EntityLivingBase;

public class WEntityLivingBase extends WEntity {
    public WEntityLivingBase(Object obj) {
        super(obj);
    }

    public float getHealth() {
        return cast().getHealth();
    }
    public float getMaxHealth() {
        return cast().getMaxHealth();
    }
    public int getHurtTime() {
        return cast().hurtTime;
    }

    public float getYawHead() {
        return cast().getRotationYawHead();
    }
    public float getPrevYawHead() {
        return cast().prevRotationYawHead;
    }

    public void setYawHead(float rotationYawHead) {
        cast().setRotationYawHead(rotationYawHead);
    }
    public void setPrevYawHead(float prevRotationYawHead) {
        cast().prevRotationYawHead  = prevRotationYawHead;
    }

    @Override
    public EntityLivingBase cast() {
        return cast(getWrappedObject(), EntityLivingBase.class);
    }
}
