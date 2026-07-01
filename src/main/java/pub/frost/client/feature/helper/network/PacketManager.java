package pub.frost.client.feature.helper.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.client.core.FrostCore;

import java.util.HashSet;
import java.util.Set;

public class PacketManager {
    private final Set<Packet> noEventPackets = new HashSet<>();
    private final Minecraft mc = Minecraft.getMinecraft();

    public boolean processIncoming(Packet packet) {
        if (FrostCore.getHelpers().getLagManager().processIncoming(packet))
            return true;
        EventPacket event = new EventPacket(PacketType.IN, packet);
        FrostCore.getEventBus().call(event);
        return event.isCancelled();
    }

    public boolean processOutgoing(Packet packet) {
        if (noEventPackets.contains(packet)) {
            noEventPackets.remove(packet);
            return false;
        }

        if (FrostCore.getHelpers().getLagManager().processOutgoing(packet))
            return true;
        EventPacket event = new EventPacket(PacketType.OUT, packet);
        FrostCore.getEventBus().call(event);
        return event.isCancelled();
    }

    public void sendPacket(Packet packet, boolean event) {
        if (!event) {
            noEventPackets.add(packet);
        }
        mc.getNetHandler().addToSendQueue(packet);
    }
}
