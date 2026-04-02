package pub.frost.base.event.impl.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pub.frost.base.event.api.interfaces.Event;

@AllArgsConstructor @Getter @Setter
public class EventSprint implements Event {
    private boolean keyDown;
}
