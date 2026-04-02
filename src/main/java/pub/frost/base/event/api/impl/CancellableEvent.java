package pub.frost.base.event.api.impl;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pub.frost.base.event.api.interfaces.Cancellable;
import pub.frost.base.event.api.interfaces.Event;

@NoArgsConstructor @Getter @Setter
public class CancellableEvent implements Event, Cancellable {
    private boolean cancelled = false;
}
