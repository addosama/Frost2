package pub.frost.wrappers.shared.entity;

import net.minecraft.entity.EntityLivingBase;

public class WEntityLivingBase extends WEntity {
    public WEntityLivingBase() {
        super(EntityLivingBase.class);
    }
    public WEntityLivingBase(Class<?> targetClass) {
        super(targetClass);
    }

    public float getHealth(Object instance) {
        return cast(instance, EntityLivingBase.class).getHealth();
    }
    public float getMaxHealth(Object instance) {
        return cast(instance, EntityLivingBase.class).getMaxHealth();
    }
    public int getHurtTime(Object instance) {
        return cast(instance, EntityLivingBase.class).hurtTime;
    }

    public Object getHeldItem(Object instance) {
        return cast(instance, EntityLivingBase.class).getHeldItem();
    }

    public void swingItem(Object instance) {
        cast(instance, EntityLivingBase.class).swingItem();
    }
}
