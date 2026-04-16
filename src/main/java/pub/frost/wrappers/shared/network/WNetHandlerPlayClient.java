package pub.frost.wrappers.shared.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import pub.frost.base.wrapping.Wrapper;
import pub.frost.wrappers.FakeInstanceWrapper;

public class WNetHandlerPlayClient extends Wrapper implements FakeInstanceWrapper<NetHandlerPlayClient> {
    public WNetHandlerPlayClient() {
        super(NetHandlerPlayClient.class);
    }

    public void addToSendQueue(Object instance, Object packet) {
        cast(instance).addToSendQueue((Packet) packet);
    }
}
