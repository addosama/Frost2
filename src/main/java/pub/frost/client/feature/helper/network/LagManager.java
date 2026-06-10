package pub.frost.client.feature.helper.network;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventProactiveLag;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import net.minecraft.client.Minecraft;
import pub.frost.client.core.FrostCore;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Setter
public class LagManager {
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Deque<DelayedPacket> incomingPacketDeque = new ConcurrentLinkedDeque<>();
    private final Deque<DelayedPacket> outgoingPacketDeque = new ConcurrentLinkedDeque<>();

    private int incomingLaggedTicks = 0;
    private int outgoingLaggedTicks = 0;

    private boolean laggingInc = false, laggingOut = false;
    private int releaseIncTick = -1, releaseOutTick = -1;

    public boolean processIncoming(Packet<?> packet) {
        EventProactiveLag lagEvent = new EventProactiveLag(
                packet, PacketType.IN,
                incomingLaggedTicks, outgoingLaggedTicks
        );
        FrostCore.getEventBus().call(lagEvent);
        laggingInc = lagEvent.isLagIncoming();
        laggingOut = lagEvent.isLagOutgoing();
        releaseIncTick = lagEvent.getReleaseIncomingReachTick();
        releaseOutTick = lagEvent.getReleaseOutgoingReachTick();

        if (laggingInc || lagEvent.isLagCurrentPacket()) {
            if (laggingInc) incomingPacketDeque.offerLast(new DelayedPacket(packet));
            return true;
        }
        return false;
    }
    public boolean processOutgoing(Packet<?> packet) {
        if (
                packet instanceof C00Handshake
                || packet instanceof C01PacketPing
                || packet instanceof C00PacketServerQuery
                || packet instanceof C01PacketEncryptionResponse
                || packet instanceof C00PacketLoginStart
        ) {
            return false;
        }

        EventProactiveLag lagEvent = new EventProactiveLag(
                packet, PacketType.OUT,
                incomingLaggedTicks, outgoingLaggedTicks
        );
        FrostCore.getEventBus().call(lagEvent);
        laggingInc = lagEvent.isLagIncoming();
        laggingOut = lagEvent.isLagOutgoing();
        releaseIncTick = lagEvent.getReleaseIncomingReachTick();
        releaseOutTick = lagEvent.getReleaseOutgoingReachTick();

        if (laggingOut || lagEvent.isLagCurrentPacket()) {
            if (laggingOut) outgoingPacketDeque.offerLast(new DelayedPacket(packet));
            return true;
        }
        return false;
    }

    @EventHandler
    private void onUpdate(EventGameTick event) {
        if (event.getType() == TickType.PRE) {
            EntityPlayerSP player = mc.thePlayer;
            if (player == null || mc.theWorld == null) {
                clearIncoming();
                clearOutgoing();
            }
            else {
                if (player.isDead) {
                    flushIncoming();
                    clearOutgoing();
                    return;
                }

                if (laggingInc) {
                    if (releaseIncTick >= 0) {
                        while (!incomingPacketDeque.isEmpty()) {
                            DelayedPacket data = incomingPacketDeque.peekFirst();
                            if (data.ticks >= releaseIncTick) {
                                data.packet.processPacket(mc.getNetHandler());
                                incomingPacketDeque.pollFirst();
                            } else break;
                        }
                    }
                }
                else flushIncoming();

                if (laggingOut) {
                    if (releaseOutTick >= 0) {
                        while (!outgoingPacketDeque.isEmpty()) {
                            DelayedPacket data = outgoingPacketDeque.peekFirst();
                            if (data.ticks >= releaseOutTick) {
                                FrostCore.getHelpers().getPacketManager().sendPacket(
                                        data.packet,
                                        false
                                );
                                outgoingPacketDeque.pollFirst();
                            } else break;
                        }
                    }
                }
                else flushOutgoing();
            }
        }
        else {
            laggingInc = !incomingPacketDeque.isEmpty();
            if (laggingInc) {
                incomingLaggedTicks++;
                incomingPacketDeque.forEach(p -> p.ticks++);
            }
            else incomingLaggedTicks = 0;

            laggingOut = !outgoingPacketDeque.isEmpty();
            if (laggingOut) {
                outgoingLaggedTicks++;
                outgoingPacketDeque.forEach(p -> p.ticks++);
            }
            else outgoingLaggedTicks = 0;
        }
    }

    private void flushIncoming() {
        while (!incomingPacketDeque.isEmpty()) {
            DelayedPacket data = incomingPacketDeque.poll();
            data.packet.processPacket(mc.getNetHandler());
        }
    }
    private void flushOutgoing() {
        while (!outgoingPacketDeque.isEmpty()) {
            DelayedPacket data = outgoingPacketDeque.poll();
            FrostCore.getHelpers().getPacketManager().sendPacket(
                    data.packet,
                    false
            );
        }
    }

    private void clearIncoming() {
        incomingPacketDeque.clear();
    }
    private void clearOutgoing() {
        outgoingPacketDeque.clear();
    }

    @RequiredArgsConstructor
    private static class DelayedPacket {
        final Packet packet;
        int ticks;
    }
}
