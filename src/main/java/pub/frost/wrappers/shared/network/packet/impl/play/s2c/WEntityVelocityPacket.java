package pub.frost.wrappers.shared.network.packet.impl.play.s2c;

import net.minecraft.network.play.server.S12PacketEntityVelocity;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WEntityVelocityPacket extends WPacket {
    public WEntityVelocityPacket() {
        super(S12PacketEntityVelocity.class);
    }

    public int getEntityId(Object instance) {
        return cast(instance, S12PacketEntityVelocity.class).getEntityID();
    }
}
