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
    public float getHurtTime() {
        return cast().hurtTime;
    }

    @Override
    public EntityLivingBase cast() {
        return cast(getWrappedObject(), EntityLivingBase.class);
    }
}
