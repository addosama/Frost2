package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.client.core.FrostCore;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(
            method = "setRotation",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetRotation(float yaw, float pitch, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerSP) {
            FrostCore.getHelpers().getRotationManager().setPlayerYaw(yaw % 360.0F);
            FrostCore.getHelpers().getRotationManager().setPlayerPitch(pitch % 360.0F);
            ci.cancel();
        }
    }
}
