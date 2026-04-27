package pub.frost.client.feature.helper.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
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

    @Getter
    private boolean lagIncoming = false;
    @Getter
    private int delay = 0;

    public boolean processIncoming(Object packet) {
        if (lagIncoming && shouldLag()) {
            incomingPacketDeque.offerLast(new DelayedPacket(packet));
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
            setDelay(0);
        }

        if (shouldLag()) {
            outgoingPacketDeque.offerLast(new DelayedPacket(packet));
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

                while (!incomingPacketDeque.isEmpty()) {
                    DelayedPacket data = incomingPacketDeque.peek();
                    if (data.ticks <= delay) break;
                    else {
                        Packet.processPacket(
                                data.packet,
                                Minecraft.getNetHandler(mc)
                        );
                        incomingPacketDeque.pop();
                    }
                }

                while (!outgoingPacketDeque.isEmpty()) {
                    DelayedPacket data = outgoingPacketDeque.peek();
                    if (data.ticks <= delay) break;
                    else {
                        FrostCore.getInstance().getPacketManager().sendPacket(
                                data.packet,
                                false
                        );
                        outgoingPacketDeque.pop();
                    }
                }
            }
        } else {
            incomingPacketDeque.forEach(p -> p.ticks++);
            outgoingPacketDeque.forEach(p -> p.ticks++);
        }
    }

    private boolean shouldLag() {
        return delay > 0;
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
            FrostCore.getInstance().getPacketManager().sendPacket(
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
