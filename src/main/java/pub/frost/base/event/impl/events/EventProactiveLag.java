package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.network.Packet;
import pub.frost.base.event.api.interfaces.Event;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.client.feature.helper.network.DelayedPacket;
import pub.frost.client.feature.helper.network.TickableLagTickSupplier;

import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Getter @Setter
public class EventProactiveLag implements Event {
    private final Packet eventPacket;
    private final PacketType packetType;

    private Supplier<Integer> lagTicksSupplier = null;
    private Consumer<DelayedPacket> delayedPacketConsumer = null;

    public void setLagTicks(int lagTicks) {
        setLagTicksSupplier(new TickableLagTickSupplier(lagTicks));
    }
}
