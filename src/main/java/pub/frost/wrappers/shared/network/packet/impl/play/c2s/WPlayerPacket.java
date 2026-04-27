package pub.frost.wrappers.shared.network.packet.impl.play.c2s;

import net.minecraft.network.play.client.C03PacketPlayer;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WPlayerPacket extends WPacket {
    public WPlayerPacket() {
        super(C03PacketPlayer.class);
    }
    public WPlayerPacket(Class<?> clazz) {
        super(clazz);
    }

    public float getYaw(Object instance) {
        return cast(instance, C03PacketPlayer.class).getYaw();
    }
    public float getPitch(Object instance) {
        return cast(instance, C03PacketPlayer.class).getPitch();
    }

    public static class WPlayerLookPacket extends WPlayerPacket {
        public WPlayerLookPacket() {
            super(C03PacketPlayer.C05PacketPlayerLook.class);
        }

        public Object build(float yaw, float pitch, boolean onGround) {
            return new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, onGround);
        }
    }
    public static class WPlayerPositionPacket extends WPlayerPacket {
        public WPlayerPositionPacket() {
            super(C03PacketPlayer.C04PacketPlayerPosition.class);
        }
    }
    public static class WPlayerPosLookPacket extends WPlayerPacket {
        public WPlayerPosLookPacket() {
            super(C03PacketPlayer.C06PacketPlayerPosLook.class);
        }
    }
}
