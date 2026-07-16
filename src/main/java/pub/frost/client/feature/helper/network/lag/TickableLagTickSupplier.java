package pub.frost.client.feature.helper.network.lag;

import lombok.Setter;
import pub.frost.utils.api.Tickable;

import java.util.function.Supplier;

public class TickableLagTickSupplier implements Tickable, Supplier<Integer> {
    @Setter
    private int remainingTicks;

    public TickableLagTickSupplier(int lagTicks) {
        this.remainingTicks = lagTicks;
    }

    @Override
    public void tick() {
        remainingTicks--;
    }

    @Override
    public Integer get() {
        return remainingTicks;
    }
}
