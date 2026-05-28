package pub.frost.utils.recorder.impl.state;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import pub.frost.utils.api.Tickable;

public class TickableStateRecorder extends StateRecorder implements Tickable {
    @Accessors(chain = true) @Setter
    private int maxTick = 72000;
    @Getter
    private int ticksSinceUpdate, ticksSinceTrue, ticksSinceFalse;

    public TickableStateRecorder() {
        this(false);
    }
    public TickableStateRecorder(Boolean value) {
        super(value);
    }

    @Override
    public void setValue(Boolean value) {
        super.setValue(value);

        ticksSinceUpdate = 0;
        if (value) ticksSinceTrue = 0;
        else ticksSinceFalse = 0;
    }

    @Override
    public void tick() {
        if (ticksSinceUpdate < maxTick)  {
            ticksSinceUpdate++;
        }
        if (ticksSinceTrue < maxTick) {
            ticksSinceTrue++;
        }
        if (ticksSinceFalse < maxTick) {
            ticksSinceFalse++;
        }
    }
}
