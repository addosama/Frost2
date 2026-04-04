package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.interfaces.Event;

@RequiredArgsConstructor @Getter
public class EventPostRender implements Event {
    private final float tickDelta;
}
