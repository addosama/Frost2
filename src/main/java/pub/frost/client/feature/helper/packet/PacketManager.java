package pub.frost.client.feature.helper.packet;

import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.core.FrostCore;

import java.util.HashSet;
import java.util.Set;

public class PacketManager implements Wrappers {
    private final Set<Object> noEventPackets = new HashSet<>();

    public boolean processIncoming(Object packet) {
        EventPacket event = new EventPacket(PacketType.IN, packet);
        FrostCore.getInstance().getEventBus().call(event);
        return event.isCancelled();
    }

    public boolean processOutgoing(Object packet) {
        if (noEventPackets.contains(packet)) {
            noEventPackets.remove(packet);
            return false;
        }
        EventPacket event = new EventPacket(PacketType.OUT, packet);
        FrostCore.getInstance().getEventBus().call(event);
        return event.isCancelled();
    }

    public void sendPacket(Object packet, boolean event) {
        if (!event) {
            noEventPackets.add(packet);
        }
        NetHandlerPlayClient.addToSendQueue(Minecraft.getNetHandler(Minecraft.getInstance()), packet);
    }
}
