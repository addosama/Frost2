package pub.frost.client.feature.module.impl.movement;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.events.EventPlayerUseItemSlowdown;
import pub.frost.base.event.impl.events.EventPrePlayerMotionUpdate;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.utils.MoveUtils;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author jiuxian_baka
 * @Date 2026/2/3 02:55
 */
@Module(
        key = "BlinkNoslow",
        category = ModuleCategory.MOVEMENT
)
public class BlinkNoslow extends AbstractModule {

    private final ConcurrentLinkedQueue<Packet> packets = new ConcurrentLinkedQueue<>();

    @EventHandler
    private void onPacket(EventPacket event) {
        if (event.getType() != PacketType.OUT) return;

        Packet packet = event.getPacket();
        if (packet instanceof C09PacketHeldItemChange || packet instanceof C07PacketPlayerDigging) {
            flush(false);
            return;
        }

        if (MoveUtils.getForwardValue() != 0 || MoveUtils.getStrafeValue() != 0
                && net.minecraft.client.Minecraft.getMinecraft().thePlayer.isUsingItem() && canNoslow()) {
            event.cancel();
            packets.add(packet);
        } else {
            flush();
        }
    }

    @EventHandler
    private void onMotion(EventPrePlayerMotionUpdate event) {
        if (net.minecraft.client.Minecraft.getMinecraft().thePlayer == null) return;
        if (net.minecraft.client.Minecraft.getMinecraft().thePlayer.isUsingItem() && canNoslow()) {
            if (MoveUtils.getForwardValue() != 0 || MoveUtils.getStrafeValue() != 0) {
                packets.add(new C08PacketPlayerBlockPlacement(
                        net.minecraft.client.Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem()));
                flush();
                packets.add(new C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                        BlockPos.ORIGIN,
                        EnumFacing.DOWN
                ));
            } else {
                FrostCore.getHelpers().getPacketManager().sendPacket(
                        new C08PacketPlayerBlockPlacement(
                                net.minecraft.client.Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem()), false);
            }
        } else {
            flush();
        }
    }

    @EventHandler
    private void onUseItemSlowdown(EventPlayerUseItemSlowdown event) {
        if (MoveUtils.getForwardValue() != 0 || MoveUtils.getStrafeValue() != 0
                && net.minecraft.client.Minecraft.getMinecraft().thePlayer.isUsingItem() && canNoslow()) {
            event.setForward(1.0f);
            event.setStrafe(1.0f);
        } else {
            flush();
        }
    }

    @Override
    protected void onDisabled() {
        flush();
    }

    private void flush() {
        flush(true);
    }

    private void flush(boolean includeC08) {
        if (packets.isEmpty()) return;

        if (!includeC08) {
            packets.removeIf(p -> p instanceof C08PacketPlayerBlockPlacement);
        }

        for (Packet packet : packets) {
            FrostCore.getHelpers().getPacketManager().sendPacket(packet, false);
        }
        packets.clear();
    }

    private boolean canNoslow() {
        if (checkFood()) return false;
        if (checkItem(Items.bow)) return false;
        return true;
    }

    private boolean checkFood() {
        ItemStack item = net.minecraft.client.Minecraft.getMinecraft().thePlayer.getHeldItem();
        if (item == null) return false;
        return item.getItem() == Items.golden_apple || item.getItem() == Items.potionitem;
    }

    private boolean checkItem(Item item) {
        ItemStack stack = net.minecraft.client.Minecraft.getMinecraft().thePlayer.getHeldItem();
        return stack != null && stack.getItem() == item;
    }
}
