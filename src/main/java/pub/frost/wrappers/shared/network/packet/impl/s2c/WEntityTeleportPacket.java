package pub.frost.wrappers.shared.network.packet.impl.s2c;

import net.minecraft.network.play.server.S18PacketEntityTeleport;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WEntityTeleportPacket extends WPacket {
    public WEntityTeleportPacket() {
        super(S18PacketEntityTeleport.class);
    }

    public int getEntityId(Object instance, Object world) {
        return cast(instance, S18PacketEntityTeleport.class).getEntityId();
    }

    public int getX(Object instance) {
        return cast(instance, S18PacketEntityTeleport.class).getX();
    }
    public int getY(Object instance) {
        return cast(instance, S18PacketEntityTeleport.class).getY();
    }
    public int getZ(Object instance) {
        return cast(instance, S18PacketEntityTeleport.class).getZ();
    }
}
