package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import org.joml.Vector3d;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPreProcessInteract;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.events.EventRotation;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.module.impl.combat.killaura.KillAuraAttacking;
import pub.frost.client.feature.module.impl.combat.killaura.KillAuraSearching;
import pub.frost.client.feature.module.impl.combat.killaura.KillAuraTargeting;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.client.property.impl.number.PercentProperty;
import pub.frost.utils.RotationUtils;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.EnumDirection;
import pub.frost.utils.data.Rotation;
import pub.frost.utils.data.raytrace.HitResult;
import pub.frost.utils.data.raytrace.impl.EntityHitResult;
import pub.frost.utils.interacting.EnumInteractType;
import pub.frost.utils.raycast.EnumRaycastType;
import pub.frost.utils.raycast.RaycastUtils;
import pub.frost.wrappers.shared.network.packet.impl.play.c2s.WPlayerDiggingPacket;

import java.util.List;
import java.util.Map;

@Module(
        key = "KillAura",
        category = ModuleCategory.COMBAT
)
public class KillAura extends AbstractModule {
    @InsertProperty("targeting")
    public final KillAuraTargeting targeting = new KillAuraTargeting(this);
    @InsertProperty("searching")
    public final KillAuraSearching searching = new KillAuraSearching(this);
    @InsertProperty("attacking")
    public final KillAuraAttacking attacking = new KillAuraAttacking(this);
    @Property("RayCast")
    public final ModeProperty<EnumRaycastType> rayCast = new ModeProperty<>(EnumRaycastType.DEFAULT).setVisibilitySupplier(() -> attacking.mode.is(EnumInteractType.PACKET));

    @Property("BlockHit")
    public final BooleanProperty blockHit = new BooleanProperty(true);
    // @Property("BlockMode")
    public final ModeProperty<EnumInteractType> blockMode = new ModeProperty<>(EnumInteractType.LEGIT).setVisibilitySupplier(blockHit::get).setValueChangeListener(
            (o, n) -> {
                if (o == EnumInteractType.PACKET) packetUnblock();
            }
    );
    // @Property("SwitchItemUnblock")
    public final BooleanProperty switchItemUnblock = new BooleanProperty(false).setVisibilitySupplier(() -> blockHit.get() && blockMode.is(EnumInteractType.PACKET));
    @Property("NotWhileHurt")
    public final BooleanProperty notWhileHurt = new BooleanProperty(true).setVisibilitySupplier(blockHit::get);
    @Property("BlockRange")
    public final FloatProperty blockRange = new FloatProperty(0, 6, 0.01f, 3f).setVisibilitySupplier(blockHit::get);
    @Property("BlockChance")
    public final PercentProperty blockChance = new PercentProperty(0, 1, 1).setVisibilitySupplier(blockHit::get);
    @Property("DistanceBasedChance")
    public final BooleanProperty distanceBasedChance = new BooleanProperty(true).setVisibilitySupplier(blockHit::get);

    @Property("RotationSpeed")
    public final IntegerProperty rotationSpeed = new IntegerProperty(0, 180, 1, 180);
    @Property("LockView")
    public final BooleanProperty lockView = new BooleanProperty(false);

    private Object target = null;
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
                Rotation rotation = getRotation();
                if (rotation != null) {
                    event.setYaw(rotation.getYaw());
                    event.setPitch(rotation.getPitch());
                    event.setSpeed(rotationSpeed.get());
                    event.setLockView(lockView.get());
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
        stopBlocking();
        if (target != null && rotationProvided) {
            attacking.doAttack(target);
            if (blockHit.get() && isHoldingSword()) {
                if (notWhileHurt.get() && EntityLivingBase.getHurtTime(Minecraft.getPlayer(mc)) > 0) return;
                double distance = Entity.distanceTo(
                        target,
                        Entity.getPositionVector(Minecraft.getPlayer(mc))
                );
                double range = blockRange.get();
                if (range == 0) return;
                if (distance > range) return;
                float chance = blockChance.get();
                if (distanceBasedChance.get()) {
                    chance *= (float) ((range - distance) / range);
                }
                if (Math.random() <= chance) makeBlocking();
            }
        }
        attacking.resetAttackCount();
    }

    private Rotation getRotation() {
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
            if (rayTraceTarget(rotationAimingEyePos)) return rotationAimingEyePos;
        }
        // search rotation that available to hit target
        return RotationUtils.searchRotationHittingBoundingBox(
                eyePos,
                box,
                this::rayTraceTarget,
                2
        );
    }

    public boolean rayTraceTarget(Rotation rotation) {
        return rayTraceTarget(rotation, attacking.getRealAttackRange());
    }
    public boolean rayTraceTarget(Rotation r, double reach) {
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

    private void makeBlocking() {
        if (blockMode.is(EnumInteractType.LEGIT)) Minecraft.clickRMB(mc);
        else packetBlock();
    }
    private void stopBlocking() {
        if (blockMode.is(EnumInteractType.PACKET)) packetUnblock();
    }

    private boolean packetBlockState;
    private int switchedFromSlot;
    private void packetBlock() {
        if (!packetBlockState) {
            EntityPlayer.setItemInUse(getPlayer(), getPlayerHeldItem(), 72000);
            FrostCore.getHelpers().getPacketManager().sendPacket(
                    Wrappers.PlayerBlockPlacementPacket.build(
                            getPlayerHeldItem()
                    ), true
            );
            packetBlockState = true;
        }
    }
    private void packetUnblock() {
        if (packetBlockState) {
            if (switchItemUnblock.get()) {
                if (FrostCore.getHelpers().getPlayerListener().getTicksSinceHeldItemChange() >= 1) {
                    if (switchedFromSlot != -1) {
                        InventoryPlayer.setCurrentItem(
                                EntityPlayer.getInventory(getPlayer()),
                                switchedFromSlot
                        );
                        switchedFromSlot = -1;
                    } else {
                        switchedFromSlot = getCurrentItemIndex();
                        int switchSlot = switchedFromSlot + 1;
                        InventoryPlayer.setCurrentItem(
                                EntityPlayer.getInventory(getPlayer()),
                                switchSlot > 8? 0 : switchSlot
                        );
                    }
                }
            } else {
                FrostCore.getHelpers().getPacketManager().sendPacket(PlayerDiggingPacket.build(
                        WPlayerDiggingPacket.RELEASE_USE_ITEM,
                        new BlockPosition(-1, -1, -1),
                        EnumDirection.DOWN
                ), true);
            }
            EntityPlayer.stopUsingItem(getPlayer());
            packetBlockState = false;
        }
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
    private Object getPlayerHeldItem() {
        return EntityLivingBase.getHeldItem(getPlayer());
    }
    private int getCurrentItemIndex() {
        return InventoryPlayer.getCurrentItem(EntityPlayer.getInventory(getPlayer()));
    }

    @Override
    protected void onEnabled() {
        resetTarget();
        attacking.resetCpsLimiter();
        packetBlockState = false;
        switchedFromSlot = -1;
    }

    @Override
    protected void onDisabled() {
        if (packetBlockState) stopBlocking();
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
