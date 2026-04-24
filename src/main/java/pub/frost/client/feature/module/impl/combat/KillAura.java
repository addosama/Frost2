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
import pub.frost.client.feature.module.impl.utility.Teams;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;
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
import pub.frost.utils.targeting.EnumEntityTarget;
import pub.frost.utils.targeting.EnumEntityTargetPriority;
import pub.frost.wrappers.shared.network.packet.impl.c2s.WPlayerDiggingPacket;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Module(
        key = "KillAura",
        category = ModuleCategory.COMBAT
)
public class KillAura extends AbstractModule {
    @Property("mode")
    public final ModeProperty<Mode> mode = new ModeProperty<>(Mode.SINGLE);
    @Property("fov")
    public final IntegerProperty fov = new IntegerProperty(1, 180, 1, 180);

    @Property("targets")
    public final MultipleBooleanProperty<EnumEntityTarget> targets = new MultipleBooleanProperty<>(EnumEntityTarget.class, EnumEntityTarget.PLAYERS);
    @Property("targetInvisible")
    public final BooleanProperty targetInvisible = new BooleanProperty(true);
    @Property("TeamCheck")
    public final BooleanProperty teamCheck = new BooleanProperty(true);
    @Property("priority")
    public final ModeProperty<EnumEntityTargetPriority> priority = new ModeProperty<>(EnumEntityTargetPriority.ANGLE);

    @Property("targetRange")
    public final FloatProperty targetRange = new FloatProperty(0, 8, 0.01f, 3.2f);

    // @Property("AttackMode")
    public final ModeProperty<InteractMode> attackMode = new ModeProperty<>(InteractMode.LEGIT);
    @Property("attackRange")
    public final FloatProperty attackRange = new FloatProperty(0, 6, 0.01f, 3f).setVisibilitySupplier(() -> attackMode.is(InteractMode.PACKET));

    @Property("cps")
    public final IntegerProperty cps = new IntegerProperty(0, 20, 1, 12);
    @Property("BlockHit")
    public final BooleanProperty blockHit = new BooleanProperty(true);
    // @Property("BlockMode")
    public final ModeProperty<InteractMode> blockMode = new ModeProperty<>(InteractMode.LEGIT).setVisibilitySupplier(blockHit::get).setValueChangeListener(
            (o, n) -> {
                if (o == InteractMode.PACKET) packetUnblock();
            }
    );
    // @Property("SwitchItemUnblock")
    public final BooleanProperty switchItemUnblock = new BooleanProperty(false).setVisibilitySupplier(() -> blockHit.get() && blockMode.is(InteractMode.PACKET));
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
        List<Object> validTargets = provideValidTargetList();
        if (validTargets.isEmpty()) {
            resetTarget();
            return;
        }
        if (validTargets.size() > 1) sortByPriority(validTargets);

        if (mode.is(Mode.SINGLE)) {
            if (target == null || !validTargets.stream().collect(ArrayList::new,
                    ArrayList::add,
                    ArrayList::addAll
            ).contains(target)) {
                target = validTargets.get(0);
            }
        } else target = validTargets.get(0);

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

    private long lastAttack = 0;
    private int attackCount = 0;
    @EventHandler
    private void onRender2D(EventRender2D event) {
        if (target != null && rotationProvided) {
            int minimumDelay = 1000 / cps.get();
            if (System.currentTimeMillis() > lastAttack + minimumDelay) {
                lastAttack = System.currentTimeMillis();
                attackCount ++;
            }
        }
    }

    @EventHandler
    private void onProcessInteract(EventPreProcessInteract e) {
        stopBlocking();
        if (target != null && rotationProvided) {
            while (attackCount > 0) {
                attack();
            }
            if (blockHit.get() && isHoldingSword()) {
                if (notWhileHurt.get() && EntityLivingBase.getHurtTime(mcWrapper.getPlayer(mc)) > 0) return;
                double distance = Entity.distanceTo(
                        target,
                        Entity.getPositionVector(mcWrapper.getPlayer(mc))
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
        attackCount = 0;
    }

    private List<Object> provideValidTargetList() {
        List<Object> list = new ArrayList<>();
        int fovValue = fov.get();
        Vector3d eyePos = Entity.getPositionEyes(mcWrapper.getPlayer(mc), 1);
        for (Object entity : World.getLoadedEntityList(mcWrapper.getWorld(mc))) {
            if (Entity.isDead(entity)) continue;
            if (!EntityLivingBase.isTarget(entity.getClass())) continue;
            if (entity == mcWrapper.getPlayer(mc)) continue;

            if (EntityLivingBase.getHealth(entity) <= 0) continue;
            if (EntityLivingBase.distanceTo(entity, Entity.getPositionVector(mcWrapper.getPlayer(mc))) > targetRange.getValue()) continue;

            if (teamCheck.get() && Teams.isTeammate(entity)) continue;

            for (EnumEntityTarget targetEnum : targets.getEnabled()) {
                if (!targetEnum.isTarget(entity.getClass())) continue;
                if (!targetInvisible.get() && Entity.isInvisible(entity)) continue;
                if (fovValue != 180) {
                    Rotation rotDelta = RotationUtils.getRotationDeltaAimingPoint(
                            eyePos,
                            FrostCore.getInstance().getRotationManager().getCurrentPlayerRotation(),
                            Entity.getBoundingBox(entity).getCenter()
                    );
                    if (Math.abs(rotDelta.getYaw()) > fovValue || Math.abs(rotDelta.getPitch()) > fovValue / 2f) {
                        break;
                    }
                }

                list.add(entity);
                break;
            }
        }
        return list;
    }
    private void sortByPriority(List<Object> list) {
        EnumEntityTargetPriority value = priority.getValue();
        Comparator<Object> comparator = value.getComparator().apply(mcWrapper.getPlayer(mc));
        for (EnumEntityTargetPriority p : EnumEntityTargetPriority.values()) {
            if (p == value) continue;
            comparator = comparator.thenComparing(p.getComparator().apply(mcWrapper.getPlayer(mc)));
        }
        list.sort(comparator);
    }
    private Rotation getRotation() {
        BoundingBox box = Entity.getBoundingBox(target);
        Vector3d eyePos = Entity.getPositionEyes(mcWrapper.getPlayer(mc), 1);
        Rotation rotationAimingEyePos = RotationUtils.getRotationAimingPoint(
                eyePos,
                Entity.getPositionEyes(target, 1)
        );

        // aim eyePos when the target out of attackRange
        if (eyePos.distance(Entity.getPositionVector(target)) > attackRange.get()) {
            return rotationAimingEyePos;
        }

        // return current rotation if our eyePos inside targetBoundingBox
        if (box.isVecInside(eyePos)) {
            return FrostCore.getInstance().getRotationManager().getCurrentSilentRotation();
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

    private boolean rayTraceTarget(Rotation r) {
        HitResult result = Entity.rayTrace(
                mcWrapper.getPlayer(mc),
                RotationUtils.getVectorForRotation(r.getPitch(), r.getYaw()),
                getAttackRange(), 1
        );
        if (result == null) return false;
        if (result.getType() == HitResult.EnumHitType.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) result;
            return entityHit.getHitEntity() == target;
        }
        return false;
    }

    private void attack() {
        mcWrapper.clickLMB(mc);
        attackCount --;
    }
    private void makeBlocking() {
        if (blockMode.is(InteractMode.LEGIT)) mcWrapper.clickRMB(mc);
        else packetBlock();
    }
    private void stopBlocking() {
        if (blockMode.is(InteractMode.PACKET)) packetUnblock();
    }

    private boolean packetBlockState;
    private int switchedFromSlot;
    private void packetBlock() {
        if (!packetBlockState) {
            EntityPlayer.setItemInUse(getPlayer(), getPlayerHeldItem(), 72000);
            FrostCore.getInstance().getPacketManager().sendPacket(
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
                if (FrostCore.getInstance().getPlayerListener().getTicksSinceHeldItemChange() >= 1) {
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
                FrostCore.getInstance().getPacketManager().sendPacket(PlayerDiggingPacket.build(
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
    private double getAttackRange() {
        if (attackMode.is(InteractMode.LEGIT)) return 3;
        return attackRange.get();
    }
    private Object getPlayer() {
        return mcWrapper.getPlayer(mc);
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
        attackCount = 0;
        lastAttack = 0;
        packetBlockState = false;
        switchedFromSlot = -1;
    }

    @Override
    protected void onDisabled() {
        if (packetBlockState) stopBlocking();
    }

    @TranslationKey("strings.killaura.modes.~")
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

    @TranslationKey("strings.killaura.interactmodes.~")
    @RequiredArgsConstructor
    public enum InteractMode implements Named {
        LEGIT("legit"),
        PACKET("packet"),;

        final String key;
        @Override
        public String toString() {
            return key;
        }
    }
}
