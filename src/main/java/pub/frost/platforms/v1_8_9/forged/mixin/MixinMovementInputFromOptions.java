package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.client.core.FrostCore;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions extends MovementInput {
    @Inject(
            method = "updatePlayerMoveState",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/util/MovementInputFromOptions;sneak:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void updatePlayerMoveState(CallbackInfo ci) {
        EventUpdateMovementInput event = new EventUpdateMovementInput(
                this.moveForward, this.moveStrafe,
                this.jump, this.sneak
        );
        FrostCore.getInstance().getEventBus().call(event);
        this.moveForward = event.getMoveForward();
        this.moveStrafe = event.getMoveStrafe();
        this.jump = event.isJump();
        this.sneak = event.isSneak();
    }
}
