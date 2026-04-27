package pub.frost.wrappers.shared.network.packet.impl.handshake;

import net.minecraft.network.handshake.client.C00Handshake;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WC2SHandshakePacket extends WPacket {
    public WC2SHandshakePacket() {
        super(C00Handshake.class);
    }
}
