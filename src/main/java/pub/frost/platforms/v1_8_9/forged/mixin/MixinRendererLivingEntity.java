package pub.frost.platforms.v1_8_9.forged.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pub.frost.client.core.FrostCore;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {
    @Redirect(
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevRenderYawOffset:F", opcode = Opcodes.GETFIELD)
    )
    public float getPrevRenderYawOffset(EntityLivingBase instance) {
        if (instance instanceof EntityPlayerSP) return FrostCore.getInstance().getRotationManager().getPrevSilentYaw();
        return instance.prevRenderYawOffset;
    }

    @Redirect(
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;renderYawOffset:F", opcode = Opcodes.GETFIELD)
    )
    public float getRenderYawOffset(EntityLivingBase instance) {
        if (instance instanceof EntityPlayerSP) return FrostCore.getInstance().getRotationManager().getSilentYaw();
        return instance.renderYawOffset;
    }

    @Redirect(
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevRotationYawHead:F", opcode = Opcodes.GETFIELD)
    )
    public float getPrevRotationYawHead(EntityLivingBase instance) {
        if (instance instanceof EntityPlayerSP) return FrostCore.getInstance().getRotationManager().getPrevSilentYaw();
        return instance.prevRotationYawHead;
    }

    @Redirect(
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYawHead:F", opcode = Opcodes.GETFIELD)
    )
    public float getRotationYawHead(EntityLivingBase instance) {
        if (instance instanceof EntityPlayerSP) return FrostCore.getInstance().getRotationManager().getSilentYaw();
        return instance.rotationYawHead;
    }

    @Redirect(
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;prevRotationPitch:F", opcode = Opcodes.GETFIELD)
    )
    public float getPrevRotationPitch(EntityLivingBase instance) {
        if (instance instanceof EntityPlayerSP) return FrostCore.getInstance().getRotationManager().getPrevSilentPitch();
        return instance.prevRotationPitch;
    }

    @Redirect(
            method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationPitch:F", opcode = Opcodes.GETFIELD)
    )
    public float getRotationPitch(EntityLivingBase instance) {
        if (instance instanceof EntityPlayerSP) return FrostCore.getInstance().getRotationManager().getSilentPitch();
        return instance.rotationPitch;
    }
}
