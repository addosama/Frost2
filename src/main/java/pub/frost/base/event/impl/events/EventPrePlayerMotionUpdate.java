package pub.frost.base.event.impl.events;

import pub.frost.base.event.api.interfaces.Event;

public class EventPrePlayerMotionUpdate implements Event {
    public static final EventPrePlayerMotionUpdate INSTANCE = new EventPrePlayerMotionUpdate();
    private EventPrePlayerMotionUpdate() {}
}
