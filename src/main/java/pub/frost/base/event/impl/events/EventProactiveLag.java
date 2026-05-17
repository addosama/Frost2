package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pub.frost.base.event.api.interfaces.Event;
import pub.frost.base.event.impl.types.PacketType;

@RequiredArgsConstructor
@Getter @Setter
public class EventProactiveLag implements Event {
    private final Object eventPacket;
    private final PacketType packetType;
    private final int incomingLaggedTicks, outgoingLaggedTicks;

    private boolean lagIncoming = false, lagOutgoing = false, lagCurrentPacket;
    private int releaseIncomingReachTick = -1, releaseOutgoingReachTick = -1;
}
