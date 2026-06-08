package pub.frost.base.event.impl.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pub.frost.base.event.api.interfaces.Event;

@Getter @Setter @AllArgsConstructor
public class EventHandleMouseInput implements Event {
    private int deltaX, deltaY;
}
