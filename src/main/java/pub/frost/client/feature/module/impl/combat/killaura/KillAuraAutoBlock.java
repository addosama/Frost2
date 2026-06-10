package pub.frost.client.feature.module.impl.combat.killaura;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.client.property.impl.number.PercentProperty;
import pub.frost.utils.InputUtils;
import pub.frost.utils.interacting.EnumInteractType;

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
    @Property("BlockRange")
    public final FloatProperty blockRange = new FloatProperty(0, 6, 0.01f, 3f);
    @Property("BlockChance")
    public final PercentProperty blockChance = new PercentProperty(0, 1, 1);
    @Property("DistanceBasedChance")
    public final BooleanProperty distanceBasedChance = new BooleanProperty(true);

    public void tryMakeBlocking(double distance) {
        if (notWhileHurt.get() && mc.thePlayer.hurtTime > 0) return;
        double range = blockRange.get();
        if (range == 0) return;
        if (distance > range) return;
        float chance = blockChance.get();
        if (distanceBasedChance.get()) {
            chance *= (float) ((range - distance) / range);
        }
        if (Math.random() <= chance) makeBlocking();
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
}
