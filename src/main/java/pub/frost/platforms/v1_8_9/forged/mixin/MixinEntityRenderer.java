package pub.frost.platforms.v1_8_9.forged.mixin;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;
import org.joml.Vector3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pub.frost.base.event.impl.events.*;
import pub.frost.base.rendering.ClientRenderContext;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.data.raytrace.HitResult;

import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow
    private Entity pointedEntity;

    @Inject(
            method = "renderWorldPass",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private void preRenderHand(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        FrostCore.getInstance().getEventBus().call(new EventRender3D(partialTicks));
    }

    @Inject(
            method = "updateCameraAndRender",
            at = @At("TAIL")
    )
    private void postRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        ClientRenderContext.draw(
                () -> FrostCore.getInstance().getEventBus().call(new EventPostRender(partialTicks))
        );
    }

    @Inject(
            method = "updateCameraAndRender",
            at = @At("HEAD")
    )
    private void preRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        FrostCore.getInstance().getEventBus().call(new EventPreRender(partialTicks));
    }

    @Redirect(
            method = "updateLightmap",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/settings/GameSettings;gammaSetting:F",
                    opcode = Opcodes.GETFIELD
            )
    )
    public float onGetGamma(GameSettings instance) {
        EventUpdateLightMap event = new EventUpdateLightMap(instance.gammaSetting);
        FrostCore.getInstance().getEventBus().call(event);
        return event.getGamma();
    }

    @Redirect(
            method = "getMouseOver",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getRenderViewEntity()Lnet/minecraft/entity/Entity;"
            )
    )
    private Entity onTestEntityMouseOver(Minecraft mc, float partialTicks) {
        Entity entity = mc.getRenderViewEntity();
        if (entity != null && mc.theWorld != null) {
            mc.mcProfiler.startSection("pick");
            mc.pointedEntity = null;
            double d0 = (double)mc.playerController.getBlockReachDistance();
            mc.objectMouseOver = entity.rayTrace(d0, partialTicks);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            if (mc.playerController.extendedReach()) {
                d0 = 6.0D;
                d1 = 6.0D;
            } else if (d0 > 3.0D) {
                flag = true;
            }

            if (mc.objectMouseOver != null) {
                d1 = mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = entity.getLook(partialTicks);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            this.pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double)f, (double)f, (double)f), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
                public boolean apply(Entity p_apply_1_) {
                    return p_apply_1_.canBeCollidedWith();
                }
            }));
            double d2 = d1;

            for (Entity entity1 : list) {
                float f1 = entity1.getCollisionBorderSize();

                EventTestPlayerLookingEntity event = new EventTestPlayerLookingEntity(
                        partialTicks,
                        entity,
                        entity1,
                        new Vector3d(vec3.xCoord, vec3.yCoord, vec3.zCoord),
                        new Vector3d(vec31.xCoord, vec31.yCoord, vec31.zCoord),
                        d0,
                        Wrappers.Entity.getBoundingBox(entity1).expand(f1, f1, f1)
                );
                FrostCore.getEventBus().call(event);

                AxisAlignedBB axisalignedbb = new AxisAlignedBB(
                        event.getHitbox().minX,
                        event.getHitbox().minY,
                        event.getHitbox().minZ,
                        event.getHitbox().maxX,
                        event.getHitbox().maxY,
                        event.getHitbox().maxZ
                );
                MovingObjectPosition movingobjectposition;
                if (event.isUseDefaultHitResult())
                    movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                else {
                    HitResult hitResult = event.getHitResult();
                    if (hitResult != null) {
                        MovingObjectPosition.MovingObjectType type;
                        switch (hitResult.getTypeIndex()) {
                            case 0: {
                                type = MovingObjectPosition.MovingObjectType.ENTITY;
                                break;
                            }
                            case 1: {
                                type = MovingObjectPosition.MovingObjectType.BLOCK;
                                break;
                            }
                            default: type = MovingObjectPosition.MovingObjectType.MISS;
                        }
                        movingobjectposition = new MovingObjectPosition(
                                type,
                                new Vec3(hitResult.getHitVec().x, hitResult.getHitVec().y, hitResult.getHitVec().z),
                                hitResult.getHitDirection() == null? null : EnumFacing.VALUES[hitResult.getHitDirection().getIndex()],
                                new BlockPos(hitResult.getBlockPos().x, hitResult.getBlockPos().y, hitResult.getBlockPos().z)
                        );
                    }
                    else movingobjectposition = null;
                }
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        this.pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                    if (d3 < d2 || d2 == 0.0D) {
                        if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                this.pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            this.pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (this.pointedEntity != null && flag && vec3.distanceTo(vec33) > 3.0D) {
                this.pointedEntity = null;
                mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, (EnumFacing)null, new BlockPos(vec33));
            }

            if (this.pointedEntity != null && (d2 < d1 || mc.objectMouseOver == null)) {
                mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);
                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
                    mc.pointedEntity = this.pointedEntity;
                }
            }

            mc.mcProfiler.endSection();
        }
        return null;
    }
}
