package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pub.frost.base.event.impl.events.EventPlayerAttackSlowdown;
import pub.frost.client.core.FrostCore;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer {
    @Redirect(
            method = "attackTargetEntityWithCurrentItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;setSprinting(Z)V"
            )
    )
    private void onAttackSlowdown(EntityPlayer instance, boolean b) {
        if (instance instanceof EntityPlayerSP) {
            EventPlayerAttackSlowdown event = new EventPlayerAttackSlowdown();
            FrostCore.getInstance().getEventBus().call(event);
            instance.motionX *= event.getXMultiplier();
            instance.motionZ *= event.getZMultiplier();
            if (!event.isCancelSprint()) {
                return;
            }
        }
        instance.setSprinting(false);
    }
}
