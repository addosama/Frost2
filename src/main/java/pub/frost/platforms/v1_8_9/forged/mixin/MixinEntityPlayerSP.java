package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovementInput;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventPlayerUseItemSlowdown;
import pub.frost.base.event.impl.events.EventPrePlayerMotionUpdate;
import pub.frost.base.event.impl.events.EventSprint;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Shadow
    public MovementInput movementInput;

    @Shadow
    protected int sprintToggleTimer;

    @Inject(
            method = "onUpdate",
            at = @At("HEAD")
    )
    private void preUpdateTick(CallbackInfo ci) {
        FrostCore.getEventBus().call(new EventPlayerUpdateTick(TickType.PRE));
    }

    @Inject(
            method = "onUpdate",
            at = @At("TAIL")
    )
    private void postUpdateTick(CallbackInfo ci) {
        FrostCore.getEventBus().call(new EventPlayerUpdateTick(TickType.POST));
    }

    @Inject(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;isRiding()Z"
            )
    )
    private void preMotionUpdate(CallbackInfo ci) {
        FrostCore.getEventBus().call(EventPrePlayerMotionUpdate.INSTANCE);
    }

    @Redirect(
            method = "onLivingUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"
            )
    )
    private boolean onSprint(KeyBinding instance) {
        EventSprint event = new EventSprint(instance.isKeyDown());
        FrostCore.getEventBus().call(event);
        return event.isKeyDown();
    }

    @Redirect(
            method = "onLivingUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;isRiding()Z"
            )
    )
    private boolean onUseItemSlowdown(EntityPlayerSP instance) {
        if (!instance.isRiding()) {
            EventPlayerUseItemSlowdown event = new EventPlayerUseItemSlowdown(0.2f, 0.2f);
            FrostCore.getEventBus().call(event);
            movementInput.moveStrafe *= event.getStrafe();
            movementInput.moveForward *= event.getForward();
            sprintToggleTimer = 0;
        }
        return true;
    }

    @Redirect(
            method = "updateEntityActionState",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotationYaw:F",
                    opcode = Opcodes.GETFIELD
            )
    )
    private float redirectRotYaw(EntityPlayerSP instance) {
        return FrostCore.getHelpers().getRotationManager().getPlayerYaw();
    }

    @Redirect(
            method = "updateEntityActionState",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotationPitch:F",
                    opcode = Opcodes.GETFIELD
            )
    )
    private float redirectRotPitch(EntityPlayerSP instance) {
        return FrostCore.getHelpers().getRotationManager().getPlayerPitch();
    }
}
