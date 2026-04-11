package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovementInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventSprint;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Shadow
    public MovementInput movementInput;

    @Inject(
            method = "onUpdate",
            at = @At("HEAD")
    )
    private void preUpdateTick(CallbackInfo ci) {
        FrostCore.getInstance().getEventBus().call(new EventPlayerUpdateTick(TickType.PRE));
    }

    @Inject(
            method = "onUpdate",
            at = @At("TAIL")
    )
    private void postUpdateTick(CallbackInfo ci) {
        FrostCore.getInstance().getEventBus().call(new EventPlayerUpdateTick(TickType.POST));
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
        FrostCore.getInstance().getEventBus().call(event);
        return event.isKeyDown();
    }
}
