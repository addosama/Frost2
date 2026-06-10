package pub.frost.base.event.impl.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;
import pub.frost.base.event.api.impl.CancellableEvent;
import pub.frost.base.event.api.interfaces.Typed;
import pub.frost.base.event.impl.types.PacketType;

@AllArgsConstructor @Getter @Setter
public class EventPacket extends CancellableEvent implements Typed<PacketType> {
    private final PacketType type;
    private final Packet packet;
}
