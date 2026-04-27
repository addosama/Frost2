package pub.frost.wrappers.shared.network.packet.impl.play.c2s;

import net.minecraft.network.play.client.C09PacketHeldItemChange;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WHeldItemChangePacket extends WPacket {
    public WHeldItemChangePacket() {
        super(C09PacketHeldItemChange.class);
    }
}
