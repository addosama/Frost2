package pub.frost.utils.recorder.impl.state;

import pub.frost.utils.recorder.Recorder;

public class StateRecorder extends Recorder<Boolean> {
    public StateRecorder() {
        this(false);
    }
    public StateRecorder(Boolean value) {
        super(value);
    }
}
