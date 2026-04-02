package pub.frost.base.event.impl.events;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pub.frost.base.event.api.interfaces.Event;

@NoArgsConstructor(access = AccessLevel.PRIVATE) @Getter
public class EventUpdateMovementInput implements Event {
    public static final EventUpdateMovementInput INSTANCE = new EventUpdateMovementInput();
}
