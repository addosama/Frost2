package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pub.frost.base.event.impl.events.EventSetJumpDelay;
import pub.frost.client.core.FrostCore;

@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase {
    @Shadow
    private int jumpTicks;

    @Redirect(
            method = "onLivingUpdate",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/EntityLivingBase;jumpTicks:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 1
            )
    )
    private void putJumpTick(EntityLivingBase instance, int value) {
        int delay = value;
        if (instance instanceof EntityPlayerSP) {
            EventSetJumpDelay event = new EventSetJumpDelay(delay);
            FrostCore.getInstance().getEventBus().call(event);
            delay = event.getDelay();
        }
        this.jumpTicks = delay;
    }
}
