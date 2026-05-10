package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import org.joml.Vector3d;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRotation;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.module.impl.combat.killaura.KillAuraAttacking;
import pub.frost.client.feature.module.impl.combat.killaura.KillAuraAutoBlock;
import pub.frost.client.feature.module.impl.combat.killaura.KillAuraSearching;
import pub.frost.client.feature.module.impl.combat.killaura.KillAuraTargeting;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.client.property.preset.RotationSetting;
import pub.frost.utils.RotationUtils;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.Rotation;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.utils.data.raytrace.impl.EntityHitResult;
import pub.frost.utils.interacting.EnumInteractType;
import pub.frost.utils.raycast.EnumRaycastType;
import pub.frost.utils.raycast.RaycastUtils;

import java.util.List;
import java.util.Map;

@Module(
        key = "KillAura",
        category = ModuleCategory.COMBAT
)
public class KillAura extends AbstractModule {
    @InsertProperty("targeting")
    public final KillAuraTargeting targeting = new KillAuraTargeting();
    @InsertProperty("searching")
    public final KillAuraSearching searching = new KillAuraSearching();
    @InsertProperty("attacking")
    public final KillAuraAttacking attacking = new KillAuraAttacking();
    @InsertProperty("AutoBlock")
    public final KillAuraAutoBlock autoBlock = new KillAuraAutoBlock();
    @Property("RayCast")
    public final ModeProperty<EnumRaycastType> rayCast = new ModeProperty<>(EnumRaycastType.DEFAULT).setVisibilitySupplier(() -> attacking.mode.is(EnumInteractType.PACKET));

    @InsertProperty
    public final RotationSetting rotationSetting = new RotationSetting();

    private Object target = null;
    public Object getTarget() {
        return target;
    }
    private void resetTarget() {
        target = null;
    }
    private boolean rotationProvided = false;

    @EventHandler
    private void onRotation(EventRotation event) {
        rotationProvided = false;
        List<Object> validTargets = searching.searchTargets();
        if (validTargets.isEmpty()) {
            resetTarget();
        } else {
            target = targeting.selectBestTarget(validTargets);

            if (target != null) {
                Rotation rotation = getRotation(target);
                if (rotation != null) {
                    event.setYaw(rotation.getYaw());
                    event.setPitch(rotation.getPitch());
                    event.setSpeed(rotationSetting.getSpeed());
                    event.setLockView(rotationSetting.isLockViewEnabled());
                    event.setProcessors(rotationSetting.getEnabledProcessors());
                    rotationProvided = true;
                }
            }
        }
    }

    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (target != null && rotationProvided) {
            attacking.updateCpsLimiter();
        }
    }

    @EventHandler
    private void onProcessInteract(EventPreProcessInteract e) {
        autoBlock.stopBlocking();
        if (target != null && rotationProvided) {
            attacking.doAttack(target);
            if (autoBlock.enabled.get() && isHoldingSword()) {
                double distance = Entity.distanceTo(
                        target,
                        Entity.getPositionVector(Minecraft.getPlayer(mc))
                );
                autoBlock.tryMakeBlocking(distance);
            }
        }
        attacking.resetAttackCount();
    }

    public Rotation getRotation(Object target) {
        BoundingBox box = Entity.getBoundingBox(target);
        Vector3d eyePos = Entity.getPositionEyes(Minecraft.getPlayer(mc), 1);
        Rotation rotationAimingEyePos = RotationUtils.getRotationAimingPoint(
                eyePos,
                Entity.getPositionEyes(target, 1)
        );

        // aim eyePos when the target out of attackRange
        if (eyePos.distance(Entity.getPositionVector(target)) > attacking.attackRange.get()) {
            return rotationAimingEyePos;
        }

        // return current rotation if our eyePos inside targetBoundingBox
        if (box.isVecInside(eyePos)) {
            return new Rotation(rotationAimingEyePos.getYaw(), FrostCore.getHelpers().getRotationManager().getSilentPitch());
        }
        // can we hit target directly when aiming eyePos of target?
        {
            if (rayTraceTarget(target, rotationAimingEyePos)) return rotationAimingEyePos;
        }
        // search rotation that available to hit target
        return RotationUtils.searchRotationHittingBoundingBox(
                eyePos,
                box,
                rot -> rayTraceTarget(target, rot),
                2
        );
    }

    public boolean rayTraceTarget(Object target, Rotation rotation) {
        return rayTraceTarget(target, rotation, targeting.mode.is(Mode.SINGLE)? searching.targetRange.get() : attacking.getRealAttackRange());
    }
    public boolean rayTraceTarget(Object target, Rotation r, double reach) {
        if (attacking.mode.is(EnumInteractType.LEGIT) || rayCast.is(EnumRaycastType.LEGIT)) {
            HitResult result = Entity.rayTrace(
                    Minecraft.getPlayer(mc),
                    RotationUtils.getVectorForRotation(r.getPitch(), r.getYaw()),
                    reach, 1
            );
            if (result == null) return false;
            if (result.getType() == HitResult.EnumHitType.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) result;
                return entityHit.getHitEntity() == target;
            }
            else return false;
        } else if (rayCast.is(EnumRaycastType.DEFAULT)) {
            Map.Entry<Boolean, Vector3d> result = RaycastUtils.raycast(
                    Entity.getPositionEyes(Minecraft.getPlayer(mc), 1),
                    r.getYaw(), r.getPitch(),
                    Entity.getBoundingBox(target)
            );
            if (result.getKey()) {
                return Entity.distanceTo(Minecraft.getPlayer(mc), result.getValue()) <= reach;
            }
            else return false;
        } else return rayCast.is(EnumRaycastType.DISABLED);
    }

    private boolean isHoldingSword() {
        Object itemHeld = getPlayerHeldItem();
        if (itemHeld == null) return false;
        return ItemSword.isTarget(
                ItemStack.getItem(itemHeld).getClass()
        );
    }
    private Object getPlayer() {
        return Minecraft.getPlayer(mc);
    }
    public Object getPlayerHeldItem() {
        return EntityLivingBase.getHeldItem(getPlayer());
    }
    public int getCurrentItemIndex() {
        return InventoryPlayer.getCurrentItem(EntityPlayer.getInventory(getPlayer()));
    }

    @Override
    protected void onEnabled() {
        resetTarget();
        attacking.resetCpsLimiter();
        autoBlock.resetStates();
    }

    @Override
    protected void onDisabled() {
        autoBlock.stopBlocking();
    }

    @TranslationKey("strings.enum.killaura.modes.~")
    @RequiredArgsConstructor
    public enum Mode implements Named {
        SINGLE("single"),
        SWITCH("switch"),;
        final String key;

        @Override
        public String toString() {
            return key;
        }
    }
}
