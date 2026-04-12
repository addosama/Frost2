package pub.frost.base.event.impl.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pub.frost.base.event.api.interfaces.Event;

@Getter @Setter @AllArgsConstructor
public class EventRotation implements Event {
    private float yaw, pitch, speed;
    private boolean lockView;
}
