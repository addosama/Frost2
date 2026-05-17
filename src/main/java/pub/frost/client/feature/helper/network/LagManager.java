package pub.frost.client.feature.helper.network;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventProactiveLag;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Setter
public class LagManager implements Wrappers {
    private final Object mc = Minecraft.getInstance();
    private final Deque<DelayedPacket> incomingPacketDeque = new ConcurrentLinkedDeque<>();
    private final Deque<DelayedPacket> outgoingPacketDeque = new ConcurrentLinkedDeque<>();

    private int incomingLaggedTicks = 0;
    private int outgoingLaggedTicks = 0;

    private boolean laggingInc = false, laggingOut = false;
    private int releaseIncTick = -1, releaseOutTick = -1;

    public boolean processIncoming(Object packet) {
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
    public boolean processOutgoing(Object packet) {
        if (
                C2SHandShakePacket.isTarget(packet)
                || C2SPingPacket.isTarget(packet)
                || C2SServerQueryPacket.isTarget(packet)
                || C2SEncryptionResponsePacket.isTarget(packet)
                || C2SLoginStartPacket.isTarget(packet)
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
            Object player = Minecraft.getPlayer(mc);
            if (player == null) {
                clearIncoming();
                clearOutgoing();
            } else {
                if (Entity.isDead(player)) {
                    flushIncoming();
                    clearOutgoing();
                    return;
                }

                if (laggingInc) {
                    if (releaseIncTick >= 0) {
                        while (!incomingPacketDeque.isEmpty()) {
                            DelayedPacket data = incomingPacketDeque.peekFirst();
                            if (data.ticks >= releaseIncTick) {
                                Packet.processPacket(
                                        data.packet,
                                        Minecraft.getNetHandler(mc)
                                );
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
            Packet.processPacket(
                    data.packet,
                    Minecraft.getNetHandler(mc)
            );
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
        final Object packet;
        int ticks;
    }
}
