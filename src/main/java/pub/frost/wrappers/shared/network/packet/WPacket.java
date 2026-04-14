package pub.frost.wrappers.shared.network.packet;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

@SuppressWarnings("unchecked")
public abstract class WPacket extends Wrapper implements FakeInstanceWrapper<Packet> {
    public WPacket(Class<?> clazz) {
        super(clazz);
    }

    public void processPacket(Object instance, Object netHandler) {
        cast(instance).processPacket((INetHandler) netHandler);
    }
}
