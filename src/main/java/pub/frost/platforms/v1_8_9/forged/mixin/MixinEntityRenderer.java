package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.EventRender3D;
import pub.frost.base.rendering.ImGuiContext;
import pub.frost.client.core.FrostCore;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Inject(
            method = "renderWorldPass",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    public void preRenderHand(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        GL11.glPushMatrix();
        ImGuiContext.draw(
                () -> FrostCore.getInstance().getEventBus().call(new EventRender3D(partialTicks))
        );
        GL11.glPopMatrix();
    }
}
