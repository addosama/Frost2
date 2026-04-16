package pub.frost.wrappers.shared.network.packet.impl.c2s;

import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.world.World;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WUseEntityPacket extends WPacket {
    public WUseEntityPacket() {
        super(C02PacketUseEntity.class);
    }

    public Object getEntityFromWorld(Object instance, Object world) {
        return cast(instance, C02PacketUseEntity.class).getEntityFromWorld((World) world);
    }
}
