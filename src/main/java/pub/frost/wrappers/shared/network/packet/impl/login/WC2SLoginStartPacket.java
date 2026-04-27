package pub.frost.wrappers.shared.network.packet.impl.login;

import net.minecraft.network.login.client.C00PacketLoginStart;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WC2SLoginStartPacket extends WPacket {
    public WC2SLoginStartPacket() {
        super(C00PacketLoginStart.class);
    }
}
