package pub.frost.utils.recorder;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Recorder<DATA> {
    private final DATA defaultValue;
    private DATA value;

    public Recorder(DATA defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public void reset() {
        setValue(defaultValue);
    }
}
