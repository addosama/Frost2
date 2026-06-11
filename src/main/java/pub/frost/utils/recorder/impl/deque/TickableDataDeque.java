package pub.frost.utils.recorder.impl.deque;

import pub.frost.utils.api.Tickable;

import java.util.Deque;
import java.util.LinkedList;

public class TickableDataDeque<DATA> extends DataDeque<DATA> implements Tickable {
    public TickableDataDeque(int ticksToSave) {
        super(new LinkedList<>(), ticksToSave);
    }

    @Override
    public void tick() {
        getValue().offerLast(null);
    }

    @Override
    public Deque<DATA> getValueCopy() {
        return new LinkedList<>(getValue());
    }
}
