package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.Setter;
import pub.frost.base.event.api.interfaces.Event;

@Getter @Setter
public class EventPlayerAttackSlowdown implements Event {
    private double xMultiplier = 1;
    private double zMultiplier = 1;
    private boolean cancelSprint = true;
}
