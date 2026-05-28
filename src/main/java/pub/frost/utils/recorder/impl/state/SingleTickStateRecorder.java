package pub.frost.utils.recorder.impl.state;

public class SingleTickStateRecorder extends TickableStateRecorder {
    public SingleTickStateRecorder() {
        this(false);
    }
    public SingleTickStateRecorder(boolean defaultState) {
        super(defaultState);
    }

    @Override
    public void tick() {
        if (!getValue().equals(getDefaultValue()))
            setValue(getDefaultValue());
        super.tick();
    }
}
