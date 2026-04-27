package pub.frost.wrappers.shared.network.packet.impl.login;

import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WC2SEncryptionResponsePacket extends WPacket {
    public WC2SEncryptionResponsePacket() {
        super(C01PacketEncryptionResponse.class);
    }
}
