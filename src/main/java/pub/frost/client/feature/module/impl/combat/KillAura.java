package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import org.joml.Vector3d;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRotation;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.helper.player.rotation.providers.AbstractRotationProvider;
import pub.frost.client.feature.helper.player.rotation.providers.impl.BasicRotationProvider;
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
import pub.frost.client.property.preset.RotationSetting;
import pub.frost.utils.RotationUtils;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.Rotation;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.utils.data.raytrace.impl.EntityHitResult;
import pub.frost.utils.interacting.EnumInteractType;
import pub.frost.utils.raycast.EnumRaycastType;
import pub.frost.utils.raycast.RayCastUtils;

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
    @Property("WallsCheck")
    public final BooleanProperty wallsCheck = new BooleanProperty(true).setVisibilitySupplier(() -> attacking.mode.is(EnumInteractType.PACKET));

    @InsertProperty
    public final RotationSetting rotationSetting = new RotationSetting();

    private final AbstractRotationProvider rotationProvider = new BasicRotationProvider();

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
        Vector3d targetEyePos = Entity.getPositionEyes(target, 1);
        Rotation rotationAimingEyePos = RotationUtils.getRotationAimingPoint(
                eyePos,
                targetEyePos
        );

        // return current rotation if our eyePos inside targetBoundingBox
        if (box.isVecInside(eyePos)) {
            return new Rotation(rotationAimingEyePos.getYaw(), FrostCore.getHelpers().getRotationManager().getSilentPitch());
        }
        // search rotation that available to hit target
        return rotationProvider.getRotation(
                eyePos, box, targetEyePos,
                rot -> rayTraceTarget(target, rot),
                true
        );
    }

    public boolean rayTraceTarget(Object target, Rotation rotation) {
        return rayTraceTarget(target, rotation, targeting.mode.is(Mode.SINGLE)? searching.targetRange.get() : attacking.getRealAttackRange(), true);
    }
    public boolean rayTraceTarget(Object target, Rotation r, double reach, boolean useDefaultIfOutOfRange) {
        if (rayCast.is(EnumRaycastType.DISABLED)) return true;

        final Object player = getPlayer();
        final Vector3d playerEyePos = Entity.getPositionEyes(player, 1);
        final float yaw = r.getYaw(), pitch = r.getPitch();

        final boolean outofRange = useDefaultIfOutOfRange && playerEyePos.distance(Entity.getPositionEyes(target, 1)) > reach;

        if (rayCast.is(EnumRaycastType.DEFAULT) || outofRange) {
            double cmpReach = outofRange? searching.targetRange.get() : reach;
            Map.Entry<Boolean, Vector3d> result = RayCastUtils.getSimpleHitResult(
                    playerEyePos,
                    r.getYaw(), r.getPitch(),
                    Entity.getBoundingBox(target)
            );
            if (result.getKey()) {
                // check rotation hitting walls
                final double distToHitPoint = playerEyePos.distance(result.getValue());
                if (distToHitPoint <= cmpReach) {
                    if (wallsCheck.get()) {
                        HitResult hitResult = Entity.raytraceBlocks(
                                player, playerEyePos,
                                RotationUtils.getVectorForRotation(pitch, yaw),
                                searching.targetRange.get()
                        );
                        if (hitResult != null && hitResult.getType() == HitResult.EnumHitType.BLOCK) {
                            return playerEyePos.distance(hitResult.getHitVec()) > distToHitPoint;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        else if (attacking.mode.is(EnumInteractType.LEGIT) || rayCast.is(EnumRaycastType.LEGIT)) {
            HitResult result = Entity.rayTrace(
                    player,
                    RotationUtils.getVectorForRotation(r.getPitch(), r.getYaw()),
                    reach, 1
            );
            if (result == null) return false;
            if (result.getType() == HitResult.EnumHitType.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) result;
                return entityHit.getHitEntity() == target;
            }
            return false;
        }

        return false;
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
