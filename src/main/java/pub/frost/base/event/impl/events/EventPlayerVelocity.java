package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pub.frost.base.event.api.impl.CancellableEvent;

@Getter @Setter @RequiredArgsConstructor
public class EventPlayerVelocity extends CancellableEvent {
    private final double motionX, motionY, motionZ;
    private float xMultiplier = 1, yMultiplier = 1, zMultiplier = 1;
}
