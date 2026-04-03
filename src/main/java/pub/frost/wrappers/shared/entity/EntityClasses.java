package pub.frost.wrappers.shared.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import pub.frost.wrappers.ClassEnum;

@Getter @RequiredArgsConstructor
public enum EntityClasses implements ClassEnum {
    Entity(Entity.class),
    EntityLivingBase(EntityLivingBase.class),
    EntityPlayer(EntityPlayer.class),
    ;
    final Class<?> clazz;
}
