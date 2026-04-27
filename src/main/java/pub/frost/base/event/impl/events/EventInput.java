package pub.frost.base.event.impl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.impl.CancellableEvent;
import pub.frost.base.event.api.interfaces.Typed;
import pub.frost.base.event.impl.types.InputDevice;

@Getter @RequiredArgsConstructor
public class EventInput extends CancellableEvent implements Typed<InputDevice> {
    private final InputDevice type;
    private final int key, action;

    public int getKeyCodeOnDevice() {
        if (key >= 0) return key;
        return -1 - key;
    }
}
