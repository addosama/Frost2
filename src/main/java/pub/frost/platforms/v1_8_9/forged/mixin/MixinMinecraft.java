package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.client.core.FrostCore;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
                    ordinal = 1
            )
    )
    private void preGameTick(CallbackInfo ci) {
        FrostCore.getInstance().getEventBus().call(EventGameTick.PRE);
    }

    @Inject(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                    ordinal = 0
            )
    )
    private void postGameTick(CallbackInfo ci) {
        FrostCore.getInstance().getEventBus().call(EventGameTick.POST);
    }
}
