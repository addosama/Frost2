package pub.frost.wrappers.shared.network.packet.impl.handshake;

import net.minecraft.network.status.client.C01PacketPing;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WC2SPingPacket extends WPacket {
    public WC2SPingPacket() {
        super(C01PacketPing.class);
    }
}
