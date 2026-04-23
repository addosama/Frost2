package pub.frost.wrappers.shared.network.packet.impl.c2s;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WPlayerBlockPlacementPacket extends WPacket {
    public WPlayerBlockPlacementPacket() {
        super(C08PacketPlayerBlockPlacement.class);
    }

    public Object build(Object itemStack) {
        return new C08PacketPlayerBlockPlacement((ItemStack) itemStack);
    }
}
