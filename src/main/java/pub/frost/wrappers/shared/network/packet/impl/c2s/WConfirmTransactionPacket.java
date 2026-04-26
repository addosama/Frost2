package pub.frost.wrappers.shared.network.packet.impl.c2s;

import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WConfirmTransactionPacket extends WPacket {
    public WConfirmTransactionPacket() {
        super(C0FPacketConfirmTransaction.class);
    }
}
