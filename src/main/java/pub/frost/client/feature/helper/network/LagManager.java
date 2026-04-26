package pub.frost.client.feature.helper.network;

import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.types.TickType;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class LagManager {
    private final Deque<DelayedPacket> incomingPacketDeque = new ConcurrentLinkedDeque<>();
    private final Deque<DelayedPacket> outgoingPacketDeque = new ConcurrentLinkedDeque<>();

    private boolean lagIncoming = false;
    private int delay = 0;

    private boolean laggingIncoming, laggingOutgoing;

    public boolean processIncoming(Object packet) {
        if (lagIncoming && shouldLag()) {
            incomingPacketDeque.offerLast(new DelayedPacket(packet));
            return true;
        }
        return false;
    }
    public boolean processOutgoing(Object packet) {
        if (shouldLag()) {
            outgoingPacketDeque.offerLast(new DelayedPacket(packet));
            return true;
        }
        return false;
    }

    @EventHandler
    private void onPostUpdate(EventGameTick event) {
        if (event.getType() == TickType.PRE) return;

    }

    private boolean shouldLag() {
        return delay > 0;
    }

    @RequiredArgsConstructor
    private static class DelayedPacket {
        final Object packet;
        int ticks;
    }
}
