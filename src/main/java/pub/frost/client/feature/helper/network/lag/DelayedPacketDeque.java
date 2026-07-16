package pub.frost.client.feature.helper.network.lag;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.network.Packet;
import pub.frost.utils.api.Tickable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public class DelayedPacketDeque implements Tickable {
    final Deque<Packet> packetList = new ArrayDeque<>();
    final Supplier<Integer> lagTickSupplier;
    @Setter
    boolean forceFlush = false;
    boolean flushed;

    public void tick() {
        if (lagTickSupplier instanceof Tickable) ((Tickable) lagTickSupplier).tick();
    }
    public int getRemainingTicks() {
        return lagTickSupplier.get();
    }
    void setFlushed() {
        flushed = true;
    }
}
