package pub.frost.wrappers.shared.network.packet.impl.s2c;

import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.world.World;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WEntityPacket extends WPacket {
    public WEntityPacket() {
        super(S14PacketEntity.class);
    }
    public WEntityPacket(Class<?> clazz) {
        super(clazz);
    }

    public Object getEntity(Object instance, Object world) {
        return cast(instance, S14PacketEntity.class).getEntity((World) world);
    }

    public byte getPosX(Object instance) {
        return cast(instance, S14PacketEntity.class).func_149062_c();
    }
    public byte getPosY(Object instance) {
        return cast(instance, S14PacketEntity.class).func_149061_d();
    }
    public byte getPosZ(Object instance) {
        return cast(instance, S14PacketEntity.class).func_149064_e();
    }

    public static class WEntityLookPacket extends WEntityPacket {
        public WEntityLookPacket() {
            super(S14PacketEntity.S16PacketEntityLook.class);
        }
    }

    public static class WEntityRelativeMovePacket extends WEntityPacket {
        public WEntityRelativeMovePacket() {
            super(S14PacketEntity.S15PacketEntityRelMove.class);
        }
    }

    public static class WEntityLookMovePacket extends WEntityPacket {
        public WEntityLookMovePacket() {
            super(S14PacketEntity.S17PacketEntityLookMove.class);
        }
    }
}
