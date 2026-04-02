package pub.frost.base.event.impl.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pub.frost.base.event.api.interfaces.Event;

@AllArgsConstructor @Getter
public class EventRender2D implements Event {
    private final float tickDelta;
}
