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
        return cast(instance).getHealth();
    }
    public float getMaxHealth(Object instance) {
        return cast(instance).getMaxHealth();
    }
    public int getHurtTime(Object instance) {
        return cast(instance).hurtTime;
    }

    @Override
    public EntityLivingBase cast(Object instance) {
        return cast(instance, EntityLivingBase.class);
    }
}
