package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.impl.CancellableEvent;

@RequiredArgsConstructor @Getter
public class EventPlayerAttackEntity extends CancellableEvent {
    private final Object targetEntity;
}
