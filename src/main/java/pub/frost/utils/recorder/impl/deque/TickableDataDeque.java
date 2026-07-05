package pub.frost.utils.recorder.impl.deque;

import pub.frost.utils.api.Tickable;

import java.util.Deque;
import java.util.LinkedList;

public class TickableDataDeque<DATA> extends DataDeque<DATA> implements Tickable {
    public TickableDataDeque(int ticksToSave) {
        super(new LinkedList<>(), ticksToSave);
    }
    private boolean tickOffered = false;

    @Override
    public void tick() {
        if (!tickOffered)
            super.offerValue(null);
        tickOffered = false;
    }

    @Override
    public void offerValue(DATA value) {
        tickOffered = true;
        super.offerValue(value);
    }

    @Override
    public Deque<DATA> getValueCopy() {
        return new LinkedList<>(getValue());
    }
}
