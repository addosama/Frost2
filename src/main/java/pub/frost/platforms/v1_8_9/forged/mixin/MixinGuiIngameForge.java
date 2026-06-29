package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.rendering.ClientRenderContext;
import pub.frost.client.core.FrostCore;

@Mixin(GuiIngameForge.class)
public class MixinGuiIngameForge {
    @Inject(
            method = "renderGameOverlay",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/GuiIngameForge;renderCrosshairs(II)V"
            )
    )
    public void preRenderCrosshair(float partialTicks, CallbackInfo ci) {
        ClientRenderContext.draw(
                () -> FrostCore.getEventBus().call(new EventRender2D(partialTicks))
        );
    }
}
