package pub.frost.client.feature.helper.network.lag;

import lombok.Getter;
import lombok.Setter;
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
import pub.frost.client.core.FrostCore;
import pub.frost.utils.wrappers.MinecraftWrapper;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

@Setter
public class LagManager implements MinecraftWrapper {
    private final Deque<DelayedPacketDeque> incomingPacketDeque = new ConcurrentLinkedDeque<>();
    private final Deque<DelayedPacketDeque> outgoingPacketDeque = new ConcurrentLinkedDeque<>();

    @Setter @Getter
    private int sendThreshold = 0;

    public boolean processIncoming(Packet<?> packet) {
        while (!incomingPacketDeque.isEmpty()) {
            DelayedPacketDeque data = incomingPacketDeque.peekFirst();
            if (data.forceFlush || data.getRemainingTicks() <= getSendThreshold()) {
                data.packetList.forEach(p -> p.processPacket(mc.getNetHandler()));
                data.setFlushed();
                incomingPacketDeque.pollFirst();
            } else break;
        }

        EventProactiveLag lagEvent = new EventProactiveLag(packet, PacketType.IN);
        FrostCore.getEventBus().call(lagEvent);

        Supplier<Integer> lagTicksSupplier = lagEvent.getLagTicksSupplier();
        if (lagTicksSupplier != null) {
            DelayedPacketDeque delayed = new DelayedPacketDeque(lagTicksSupplier);
            if (incomingPacketDeque.offerLast(delayed) && lagEvent.getDelayedPacketConsumer() != null) {
                lagEvent.getDelayedPacketConsumer().accept(delayed);
            }
        }
        if (!incomingPacketDeque.isEmpty()) {
            incomingPacketDeque.peekLast().packetList.add(packet);
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

        while (!outgoingPacketDeque.isEmpty()) {
            DelayedPacketDeque data = outgoingPacketDeque.peekFirst();
            if (data.forceFlush || data.getRemainingTicks() <= getSendThreshold()) {
                data.packetList.forEach(
                        p -> FrostCore.getHelpers().getPacketManager().sendPacket(p, false)
                );
                data.setFlushed();
                outgoingPacketDeque.pollFirst();
            } else break;
        }

        EventProactiveLag lagEvent = new EventProactiveLag(packet, PacketType.OUT);
        FrostCore.getEventBus().call(lagEvent);

        Supplier<Integer> lagTicksSupplier = lagEvent.getLagTicksSupplier();
        if (lagTicksSupplier != null) {
            DelayedPacketDeque delayed = new DelayedPacketDeque(lagTicksSupplier);
            if (outgoingPacketDeque.offerLast(delayed) && lagEvent.getDelayedPacketConsumer() != null) {
                lagEvent.getDelayedPacketConsumer().accept(delayed);
            }
        }
        if (!outgoingPacketDeque.isEmpty()) {
            outgoingPacketDeque.peekLast().packetList.add(packet);
            return true;
        }

        return false;
    }

    @EventHandler
    private void onUpdate(EventGameTick event) {
        if (event.getType() == TickType.PRE) {
            if (mc.thePlayer == null || mc.theWorld == null) {
                clearIncoming();
                clearOutgoing();
            }
            else {
                if (mc.thePlayer.isDead) {
                    flushIncoming();
                    clearOutgoing();
                }
            }
        }
        else {
            if (!incomingPacketDeque.isEmpty()) {
                incomingPacketDeque.peekFirst().tick();
            }
            if (!outgoingPacketDeque.isEmpty()) {
                outgoingPacketDeque.peekFirst().tick();
            }
        }
    }

    private void flushIncoming() {
        while (!incomingPacketDeque.isEmpty()) {
            DelayedPacketDeque data = incomingPacketDeque.poll();
            data.packetList.forEach(p -> p.processPacket(mc.getNetHandler()));
        }
    }
    private void flushOutgoing() {
        while (!outgoingPacketDeque.isEmpty()) {
            DelayedPacketDeque data = outgoingPacketDeque.poll();
            data.packetList.forEach(
                    p -> FrostCore.getHelpers().getPacketManager().sendPacket(p, false)
            );
        }
    }

    private void clearIncoming() {
        incomingPacketDeque.clear();
    }
    private void clearOutgoing() {
        outgoingPacketDeque.clear();
    }
}

