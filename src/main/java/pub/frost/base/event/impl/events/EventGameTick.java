package pub.frost.base.event.impl.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.interfaces.Event;
import pub.frost.base.event.api.interfaces.Typed;
import pub.frost.base.event.impl.types.TickType;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE) @Getter
public class EventGameTick implements Event, Typed<TickType> {
    private final TickType type;

    public static final EventGameTick PRE = new  EventGameTick(TickType.PRE);
    public static final EventGameTick POST = new  EventGameTick(TickType.POST);
}
