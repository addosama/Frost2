package pub.frost.wrappers.shared.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import pub.frost.wrappers.ClassEnum;

@Getter @RequiredArgsConstructor
public enum EnumEntity implements ClassEnum {
    Entity(Entity.class),
    EntityLivingBase(EntityLivingBase.class),
    EntityPlayer(EntityPlayer.class),
    EntityAnimal(EntityAnimal.class),
    EntityMob(EntityMob.class),
    EntityVillager(EntityVillager.class)
    ;
    final Class<?> clazz;
}
