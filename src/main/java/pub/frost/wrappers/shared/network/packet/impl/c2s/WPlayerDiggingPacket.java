package pub.frost.wrappers.shared.network.packet.impl.c2s;

import net.minecraft.network.play.client.C07PacketPlayerDigging;
import pub.frost.wrappers.shared.network.packet.WPacket;

public class WPlayerDiggingPacket extends WPacket {
    public WPlayerDiggingPacket() {
        super(C07PacketPlayerDigging.class);
    }

    public int getStatus(Object instance) {
        switch (cast(instance, C07PacketPlayerDigging.class).getStatus()) {
            case START_DESTROY_BLOCK: return START_DESTROY_BLOCK;
            case ABORT_DESTROY_BLOCK: return ABORT_DESTROY_BLOCK;
            case STOP_DESTROY_BLOCK: return STOP_DESTROY_BLOCK;
            case DROP_ALL_ITEMS: return DROP_ALL_ITEMS;
            case DROP_ITEM: return DROP_ITEM;
            case RELEASE_USE_ITEM: return RELEASE_USE_ITEM;
        }
        return -1;
    }

    public static final int
            START_DESTROY_BLOCK = 0,
            ABORT_DESTROY_BLOCK = 1,
            STOP_DESTROY_BLOCK = 2,
            DROP_ALL_ITEMS = 3,
            DROP_ITEM = 4,
            RELEASE_USE_ITEM = 5;
}
