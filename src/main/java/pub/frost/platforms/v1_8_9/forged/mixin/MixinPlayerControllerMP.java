package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventPlayerAttackEntity;
import pub.frost.client.core.FrostCore;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(
            method = "attackEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preAttack(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        EventPlayerAttackEntity event = new EventPlayerAttackEntity(targetEntity);
        FrostCore.getInstance().getEventBus().call(event);
        if (event.isCancelled()) ci.cancel();
    }
}
