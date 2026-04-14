package pub.frost.client.feature.helper;

import lombok.Getter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.wrappers.shared.network.packet.impl.c2s.WPlayerDiggingPacket;

@Getter
public class PlayerListener implements Wrappers {
    private boolean digging;

    private boolean startDiggingTick;
    private boolean stopDiggingTick;

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
        if (PlayerDiggingPacket.isTarget(packet)) {
            if (PlayerDiggingPacket.getStatus(packet) == WPlayerDiggingPacket.START_DESTROY_BLOCK) {
                startDiggingTick = true;
                digging = true;
            }
            if (PlayerDiggingPacket.getStatus(packet) == WPlayerDiggingPacket.ABORT_DESTROY_BLOCK
                    || PlayerDiggingPacket.getStatus(packet) == WPlayerDiggingPacket.STOP_DESTROY_BLOCK
            ) {
                stopDiggingTick = true;
                digging = false;
            }
        }
    }
}
