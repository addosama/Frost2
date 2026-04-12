package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.interfaces.Event;

@Getter @RequiredArgsConstructor
public class EventPreRender implements Event {
    private final float tickDelta;
}
