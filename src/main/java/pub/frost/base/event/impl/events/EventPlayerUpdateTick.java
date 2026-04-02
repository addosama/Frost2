package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.impl.CancellableEvent;
import pub.frost.base.event.api.interfaces.Typed;
import pub.frost.base.event.impl.types.TickType;

@RequiredArgsConstructor @Getter
public class EventPlayerUpdateTick extends CancellableEvent implements Typed<TickType> {
    private final TickType type;
}
