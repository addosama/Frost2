package pub.frost.client.feature.helper.network;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.network.Packet;
import pub.frost.utils.api.Tickable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DelayedPacket implements Tickable {
    final List<Packet> packetList = new ArrayList<>();
    final Supplier<Integer> lagTickSupplier;
    @Setter
    boolean forceFlush = false;

    public void tick() {
        if (lagTickSupplier instanceof Tickable) ((Tickable) lagTickSupplier).tick();
    }
    public int getRemainingTicks() {
        return lagTickSupplier.get();
    }

    DelayedPacket(int ticks) {
        lagTickSupplier = new TickableLagTickSupplier(ticks);
    }
}
