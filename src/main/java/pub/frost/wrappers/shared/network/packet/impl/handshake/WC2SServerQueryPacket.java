package pub.frost.wrappers.shared.network.packet.impl.handshake;

import net.minecraft.network.status.client.C00PacketServerQuery;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WC2SServerQueryPacket extends WPacket {
    public WC2SServerQueryPacket() {
        super(C00PacketServerQuery.class);
    }
}
