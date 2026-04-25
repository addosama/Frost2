package pub.frost.wrappers.shared.client.controller;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WPlayerControllerMP extends Wrapper implements FakeInstanceWrapper<PlayerControllerMP> {
    public WPlayerControllerMP() {
        super(PlayerControllerMP.class);
    }

    public void attackEntity(Object instance, Object playerIn, Object targetEntity) {
        cast(instance).attackEntity(
                (EntityPlayer) playerIn, (Entity) targetEntity
        );
    }
}
