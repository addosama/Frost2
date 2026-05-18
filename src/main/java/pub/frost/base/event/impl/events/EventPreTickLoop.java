package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.interfaces.Event;

@Getter @RequiredArgsConstructor
public class EventPreTickLoop implements Event {
    private final int elapsedTicks;
    private final float tickDelta;
}
