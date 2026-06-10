package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventPreTickLoop;
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
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.preset.legacy.RotationSetting;
import pub.frost.client.property.preset.legacy.TickDeltaFixSetting;
import pub.frost.utils.BoundingBoxUtils;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.RotationUtils;
import pub.frost.utils.data.Rotation;
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
    @Property("TickTiming")
    public final ModeProperty<TickTiming> tickTiming = new ModeProperty<>(TickTiming.PRE_GAME_TICK);
    @InsertProperty
    public final TickDeltaFixSetting tickDeltaFix = new TickDeltaFixSetting(() -> tickTiming.is(TickTiming.PRE_GAME_TICK));
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
    @Property("Prediction")
    public final BooleanProperty prediction = new BooleanProperty(false);
    @Property("PredictionValue")
    public final FloatProperty predictionValue = new FloatProperty(0, 2, 0.01f, 0.5f)
            .setVisibilitySupplier(prediction::get);

    @InsertProperty
    public final RotationSetting rotationSetting = new RotationSetting();

    private final AbstractRotationProvider rotationProvider = new BasicRotationProvider();

    private Entity target = null;
    private Rotation targetRotation = null;
    private boolean tickRotProvided;

    public Entity getTarget() {
        return target;
    }
    private void resetTarget() {
        target = null;
    }

    @EventHandler
    private void onPreTickLoop(EventPreTickLoop event) {
        if (tickTiming.is(TickTiming.PRE_TICK_LOOP)) tick(event.getTickDelta());
    }

    @EventHandler
    private void onRotation(EventRotation event) {
        if (tickTiming.is(TickTiming.PRE_GAME_TICK)) tick(tickDeltaFix.get());
        if (targetRotation != null) {
            event.setYaw(targetRotation.getYaw());
            event.setPitch(targetRotation.getPitch());
            event.setSpeed(rotationSetting.getSpeed());
            event.setLockView(rotationSetting.isLockViewEnabled());
            event.setProcessors(rotationSetting.getEnabledProcessors());
        }
    }

    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (isGoodTick()) {
            attacking.updateCpsLimiter();
        }
    }

    @EventHandler
    private void onProcessInteract(EventPreProcessInteract e) {
        autoBlock.stopBlocking();
        if (isGoodTick()) {
            attacking.doAttack(target, 1);
            if (autoBlock.enabled.get() && isHoldingSword()) {
                double distance = target.getDistance(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ
                );
                autoBlock.tryMakeBlocking(distance);
            }
        }
        attacking.resetAttackCount();
    }

    private void tick(float tickDelta) {
        tickRotProvided = false;
        List<Entity> validTargets = searching.searchTargets();
        if (validTargets.isEmpty()) {
            resetTarget();
            targetRotation = null;
        } else {
            target = targeting.selectBestTarget(validTargets, tickDelta);

            if (target != null) {
                targetRotation = getRotation(target, tickDelta);
                if (targetRotation != null) tickRotProvided = true;
            }
        }
    }

    public Rotation getRotation(Entity target, float tickDelta) {
        tickDelta += prediction.get()? predictionValue.get(): 0;
        AxisAlignedBB box = prediction.get()
                ? BoundingBoxUtils.lerp(EntityUtils.getPrevBoundingBox(target), target.getEntityBoundingBox(), tickDelta)
                : target.getEntityBoundingBox();
        Vec3 eyePos = mc.thePlayer.getPositionEyes(tickDelta);
        Vec3 targetEyePos = target.getPositionEyes(tickDelta);
        Rotation rotationAimingEyePos = RotationUtils.getRotationAimingPoint(
                eyePos,
                targetEyePos
        );

        // return current rotation if our eyePos inside targetBoundingBox
        if (box.isVecInside(eyePos)) {
            return new Rotation(rotationAimingEyePos.getYaw(), FrostCore.getHelpers().getRotationManager().getSilentPitch());
        }
        // search rotation that available to hit target
        float finalTickDelta = tickDelta;
        return rotationProvider.getRotation(
                eyePos, box, targetEyePos,
                rot -> rayTraceTarget(target, rot, finalTickDelta),
                true
        );
    }

    public boolean rayTraceTarget(Entity target, Rotation rotation, float tickDelta) {
        return rayTraceTarget(target, rotation, targeting.mode.is(Mode.SINGLE)? searching.targetRange.get() : attacking.getRealAttackRange(), true, tickDelta);
    }
    public boolean rayTraceTarget(Entity target, Rotation r, double reach, boolean useDefaultIfOutOfRange, float tickDelta) {
        if (rayCast.is(EnumRaycastType.DISABLED)) return true;
        tickDelta = Math.max(0, Math.min(1, tickDelta));

        final Entity player = mc.thePlayer;
        final Vec3 playerEyePos = player.getPositionEyes(tickDelta);
        final float yaw = r.getYaw(), pitch = r.getPitch();

        final boolean outofRange = useDefaultIfOutOfRange && playerEyePos.distanceTo(target.getPositionEyes(tickDelta)) > reach;

        if (rayCast.is(EnumRaycastType.DEFAULT) || outofRange) {
            double cmpReach = outofRange? searching.targetRange.get() : reach;
            Map.Entry<Boolean, Vec3> result = RayCastUtils.getSimpleHitResult(
                    playerEyePos,
                    r.getYaw(), r.getPitch(),
                    target.getEntityBoundingBox()
            );
            if (result.getKey()) {
                // check rotation hitting walls
                final double distToHitPoint = playerEyePos.distanceTo(result.getValue());
                if (distToHitPoint <= cmpReach) {
                    if (wallsCheck.get()) {
                        MovingObjectPosition hitResult = RayCastUtils.raytraceBlocks(
                                player.worldObj, playerEyePos,
                                RotationUtils.getVectorForRotation(pitch, yaw),
                                searching.targetRange.get()
                        );
                        if (hitResult != null && hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                            return playerEyePos.distanceTo(hitResult.hitVec) > distToHitPoint;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        else if (attacking.mode.is(EnumInteractType.LEGIT) || rayCast.is(EnumRaycastType.LEGIT)) {
            MovingObjectPosition result = EntityUtils.getLookingObject(
                    player,
                    RotationUtils.getVectorForRotation(r.getPitch(), r.getYaw()),
                    reach, tickDelta
            );
            if (result == null) return false;
            if (result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                return result.entityHit == target;
            }
            return false;
        }

        return false;
    }

    private boolean isGoodTick() {
        return target != null && tickRotProvided;
    }

    private boolean isHoldingSword() {
        ItemStack itemHeld = getPlayerHeldItem();
        if (itemHeld == null) return false;
        return itemHeld.getItem() instanceof ItemSword;
    }
    public ItemStack getPlayerHeldItem() {
        return mc.thePlayer.getHeldItem();
    }
    public int getCurrentItemIndex() {
        return mc.thePlayer.inventory.currentItem;
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
        @Override public String toString() {
            return key;
        }
    }

    @TranslationKey("strings.enum.killaura.ticktiming.~")
    @RequiredArgsConstructor
    public enum TickTiming implements Named {
        PRE_TICK_LOOP("PreTickLoop"),
        PRE_GAME_TICK("PreGameTick");
        final String key;
        @Override public String toString() {
            return key;
        }
    }
}
