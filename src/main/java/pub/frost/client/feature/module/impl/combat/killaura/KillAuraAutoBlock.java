package pub.frost.client.feature.module.impl.combat.killaura;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import pub.frost.base.event.impl.events.EventProactiveLag;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.helper.network.DelayedPacket;
import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.client.property.impl.number.PercentProperty;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.InputUtils;
import pub.frost.utils.interacting.EnumInteractType;
import pub.frost.utils.raycast.RayCastUtils;

import java.util.Deque;
import java.util.List;
import java.util.Map;

@SubModule(KillAura.class)
public class KillAuraAutoBlock extends AbstractSubModule<KillAura> {
    @PropertyGroupMain
    @Property("Enabled")
    public final BooleanProperty enabled = new BooleanProperty(false);
    // @Property("BlockMode")
    public final ModeProperty<EnumInteractType> blockMode = new ModeProperty<>(EnumInteractType.LEGIT).setValueChangeListener(
            (o, n) -> {
                if (o == EnumInteractType.PACKET) packetUnblock();
            }
    );
    // @Property("SwitchItemUnblock")
    public final BooleanProperty switchItemUnblock = new BooleanProperty(false).setVisibilitySupplier(() -> blockMode.is(EnumInteractType.PACKET));
    @Property("NotWhileHurt")
    public final BooleanProperty notWhileHurt = new BooleanProperty(true);
    @InsertProperty("Predict")
    public final Prediction predict = new Prediction();
    @Property("ForceIfInDanger")
    public final BooleanProperty forceIfInDanger = new BooleanProperty(true)
            .setVisibilitySupplier(predict.enabled::get);
    @Property("BlockRange")
    public final FloatProperty blockRange = new FloatProperty(0, 6, 0.01f, 3f);

    @Property("LimiterMode")
    public final ModeProperty<LimiterMode> limiterMode = new ModeProperty<>(LimiterMode.CHANCE);

    @Property("BlockChance")
    public final PercentProperty blockChance = new PercentProperty(0, 1, 1)
            .setVisibilitySupplier(() -> limiterMode.is(LimiterMode.CHANCE));
    @Property("DistanceBasedChance")
    public final BooleanProperty distanceBasedChance = new BooleanProperty(true)
            .setVisibilitySupplier(() -> limiterMode.is(LimiterMode.CHANCE));

    @Property("BPS")
    public final IntegerProperty bps = new IntegerProperty(1, 10, 1, 1)
            .setVisibilitySupplier(() -> limiterMode.is(LimiterMode.STATIC));

    @Property("Lag")
    public final BooleanProperty lag = new BooleanProperty(false);
    @Property("MaxLagTicks")
    public final IntegerProperty maxLagTicks = new IntegerProperty(1, 40, 1, 2)
            .setVisibilitySupplier(lag::get);

    private long lastBlock = 0;
    private int blockCount = 0;

    public void tryMakeBlocking(double distance) {
        if (notWhileHurt.get() && mc.thePlayer.hurtTime > 0) return;
        boolean block = false;

        if (predict.enabled.get()) {
            boolean inDanger = runDangerPrediction();
            if (!inDanger) return;
            if (forceIfInDanger.get()) block = true;
        }

        if (!block) {
            double range = blockRange.get();
            if (range == 0) return;
            if (distance > range) return;

            if (limiterMode.is(LimiterMode.CHANCE)) {
                float chance = blockChance.get();
                if (distanceBasedChance.get()) {
                    chance *= (float) ((range - distance) / range);
                }
                if (Math.random() <= chance) block = true;
            }
            else {
                int div = 1000 / bps.get();
                int countAdd = (int)(System.currentTimeMillis() - lastBlock) / div;
                if (countAdd > 0) {
                    if (countAdd < 3) {
                        blockCount += countAdd;
                        lastBlock += div * countAdd;
                    }
                    else {
                        blockCount += 1;
                        lastBlock = System.currentTimeMillis();
                    }
                }

                if (blockCount > 0) {
                    block = true;
                    blockCount--;
                }
            }
        }

        if (block) makeBlocking();
    }

    private boolean runDangerPrediction() {
        List<Entity> entities = getParent().searching.searchTargets(
                predict.customSearchRange.get() ?
                        predict.searchRange.get()
                        : blockRange.get()
        );

        boolean inDanger = false;
        for (Entity entity : entities) {
            if (inDanger) break;
            if (!(entity instanceof EntityPlayer)) continue;

            if (
                    RayCastUtils.getSimpleHitResult(
                            entity.getPositionEyes(1),
                            entity.rotationYaw, entity.rotationPitch,
                            mc.thePlayer.getEntityBoundingBox()
                    ).getKey()
            ) {
                inDanger = true;
                break;
            }

            if (predict.predictPastPos.get()) {
                Deque<Vec3> pastPosDeque = FrostCore.getHelpers().getPlayerListener().getPositionDeque().getValueCopy();
                for (int ticks = 0; ticks <= predict.pastTicks.get(); ticks++) {
                    if (pastPosDeque.isEmpty()) break;
                    Vec3 pastPos = pastPosDeque.pollFirst();
                    AxisAlignedBB pastBB = EntityUtils.getBoundingBoxAtPosition(
                            mc.thePlayer, pastPos
                    );

                    if (
                            RayCastUtils.getSimpleHitResult(
                                    entity.getPositionEyes(1),
                                    entity.rotationYaw, entity.rotationPitch,
                                    pastBB
                            ).getKey()
                    ) {
                        inDanger = true;
                        break;
                    }
                }
            }
            if (predict.testVelocityPosition.get()) {
                Deque<Map.Entry<Vec3, Vec3>> velocityDeque = FrostCore.getHelpers().getPlayerListener().getVelocityDeque().getValueCopy();
                for (int ticks = 0; ticks <= predict.velocityInTicks.get(); ticks++) {
                    if (velocityDeque.isEmpty()) break;

                    if (velocityDeque.peekFirst() == null) {
                        velocityDeque.pollFirst();
                        continue;
                    }

                    Vec3 pastPos = velocityDeque.pollFirst().getKey();
                    AxisAlignedBB pastBB = EntityUtils.getBoundingBoxAtPosition(
                            mc.thePlayer, pastPos
                    );

                    if (
                            RayCastUtils.getSimpleHitResult(
                                    entity.getPositionEyes(1),
                                    entity.rotationYaw, entity.rotationPitch,
                                    pastBB
                            ).getKey()
                    ) {
                        inDanger = true;
                        break;
                    }
                }
            }
        }

        return inDanger;
    }

    public void makeBlocking() {
        if (blockMode.is(EnumInteractType.LEGIT)) InputUtils.clickRMB();
        else packetBlock();
    }
    public void stopBlocking() {
        if (blockMode.is(EnumInteractType.PACKET)) packetUnblock();
    }

    private boolean packetBlockState;
    private int switchedFromSlot;
    private void packetBlock() {
        if (!packetBlockState) {
            ItemStack itemInUse = getParent().getPlayerHeldItem();
            mc.thePlayer.setItemInUse(itemInUse, 72000);
            FrostCore.getHelpers().getPacketManager().sendPacket(
                    new C08PacketPlayerBlockPlacement(
                            itemInUse
                    ),
                    true
            );
            packetBlockState = true;
        }
    }
    private void packetUnblock() {
        if (packetBlockState) {
            if (switchItemUnblock.get()) {
                if (FrostCore.getHelpers().getPlayerListener().getTicksSinceHeldItemChange() >= 1) {
                    if (switchedFromSlot != -1) {
                        mc.thePlayer.inventory.currentItem = switchedFromSlot;
                        switchedFromSlot = -1;
                    } else {
                        switchedFromSlot = getParent().getCurrentItemIndex();
                        int switchSlot = switchedFromSlot + 1;
                        mc.thePlayer.inventory.currentItem = switchSlot > 8? 0 : switchSlot;
                    }
                }
            } else {
                FrostCore.getHelpers().getPacketManager().sendPacket(
                        new C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                                new BlockPos(-1, -1, -1),
                                EnumFacing.DOWN
                        ),
                        true
                );
            }
            mc.thePlayer.stopUsingItem();
            packetBlockState = false;
        }
    }

    public void resetStates() {
        packetBlockState = false;
        switchedFromSlot = -1;
    }

    private DelayedPacket delayed = null;
    public void processLag(EventProactiveLag event) {
        if (event.getPacketType() == PacketType.IN) return;

        Packet packet = event.getEventPacket();
        boolean lag = false;
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            if (delayed != null) {
                delayed.setForceFlush(true);
                delayed = null;
            }
        }
        else if (packet instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging c07 =  (C07PacketPlayerDigging) packet;
            if (c07.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                lag = true;
            }
        }
        else if (packet instanceof C09PacketHeldItemChange) {
            lag = true;
        }

        if (lag && delayed == null) {
            event.setLagTicks(maxLagTicks.get());
            event.setDelayedPacketConsumer(p -> delayed = p);
        }
    }

    public static class Prediction {
        @TranslationKey("strings.enabled")
        @PropertyGroupMain
        @Property("Enabled")
        public final BooleanProperty enabled = new BooleanProperty(false);

        @Property("CustomSearchRange")
        public final BooleanProperty customSearchRange = new BooleanProperty(false);
        @Property("SearchRange")
        public final FloatProperty searchRange = new FloatProperty(0, 8, 0.01f, 3f)
                .setVisibilitySupplier(customSearchRange::get);

        @Property("TestPastPosition")
        public final BooleanProperty predictPastPos = new BooleanProperty(true);
        @Property("PastTicks")
        public final IntegerProperty pastTicks = new IntegerProperty(1, 20, 1, 5);

        @Property("TestVelocityPosition")
        public final BooleanProperty testVelocityPosition = new BooleanProperty(true);
        @Property("VelocityInTicks")
        public final IntegerProperty velocityInTicks = new IntegerProperty(1, 20, 1, 5);
    }

    @RequiredArgsConstructor
    @TranslationKey("strings.enum.killaura.autoblock.limitermode.~")
    public enum LimiterMode implements Named {
        CHANCE("Chance"),
        STATIC("Static");
        final String key;
        @Override
        public String toString() {
            return key;
        }
    }
}
