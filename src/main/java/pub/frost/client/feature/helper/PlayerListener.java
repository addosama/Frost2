package pub.frost.client.feature.helper;

import lombok.Getter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;
import pub.frost.wrappers.shared.network.packet.impl.c2s.WPlayerDiggingPacket;

@Getter
public class PlayerListener {
    private boolean digging;

    private boolean startDiggingTick;
    private boolean stopDiggingTick;
    
    private WPlayerDiggingPacket c07Wrapper = FrostCore.getWrapper(WPlayerDiggingPacket.class);

    @EventHandler(priority = 0)
    public void preGameTick(EventGameTick event) {
        if (event.getType() == TickType.PRE) {
            startDiggingTick = false;
            stopDiggingTick = false;
        }
    }

    @EventHandler(priority = 100)
    public void prePacket(EventPacket e) {
        if (e.isCancelled()) return;
        Object packet = e.getPacket();
        if (c07Wrapper.isTarget(packet)) {
            if (c07Wrapper.getStatus(packet) == WPlayerDiggingPacket.START_DESTROY_BLOCK) {
                startDiggingTick = true;
                digging = true;
            }
            if (c07Wrapper.getStatus(packet) == WPlayerDiggingPacket.ABORT_DESTROY_BLOCK
                    || c07Wrapper.getStatus(packet) == WPlayerDiggingPacket.STOP_DESTROY_BLOCK
            ) {
                stopDiggingTick = true;
                digging = false;
            }
        }
    }
}
