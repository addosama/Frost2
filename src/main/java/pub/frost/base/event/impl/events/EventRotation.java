package pub.frost.base.event.impl.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pub.frost.base.event.api.interfaces.Event;

@Getter @Setter @AllArgsConstructor
public class EventRotation implements Event {
    private float yaw, pitch, speed;
    private boolean lockView;
    private int processors;

    public void addProcessors(int processors) {
        this.processors |= processors;
    }
    public void removeProcessors(int processors) {
        this.processors &= ~processors;
    }

    public boolean hasProcessor(int processor) {
        return (this.processors & processor) != 0;
    }

    public static final int GCD_FIX = 0x01, PITCH_DELTA_FIX = 0x10;
}
