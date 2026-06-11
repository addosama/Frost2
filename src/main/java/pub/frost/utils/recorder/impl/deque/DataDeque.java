package pub.frost.utils.recorder.impl.deque;

import pub.frost.utils.recorder.Recorder;

import java.util.ArrayDeque;
import java.util.Deque;

public class DataDeque<DATA> extends Recorder<Deque<DATA>> {
    private final int length;
    public DataDeque(int length) {
        this(new ArrayDeque<>(), length);
    }

    public DataDeque(Deque<DATA> deque, int length) {
        super(deque);
        this.length = length;
    }

    public Deque<DATA> getValueCopy() {
        return new ArrayDeque<>(getValue());
    }

    public void offerValue(DATA value) {
        Deque<DATA> deque = getValue();
        deque.offerLast(value);
        while (deque.size() > length) {
            deque.pollFirst();
        }
    }
}
