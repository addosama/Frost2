package pub.frost.client.feature.helper.player.interact;

import lombok.Getter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.wrappers.shared.network.packet.impl.play.c2s.WPlayerDiggingPacket;

@Getter
public class PlayerListener implements Wrappers {
    private final Object mc = Minecraft.getInstance();
    private boolean digging;

    private boolean startDiggingTick;
    private boolean stopDiggingTick;

    private int ticksSinceHeldItemChange;

    @EventHandler(priority = -1)
    public void preGameTick(EventGameTick event) {
        if (event.getType() == TickType.PRE) {
            if (Minecraft.getWorld(mc) == null) resetAllStates();
            else {
                ticksSinceHeldItemChange++;
            }
        }
    }

    @EventHandler(priority = 100)
    public void prePacket(EventPacket e) {
        if (e.isCancelled()) return;
        if (e.getType() == PacketType.OUT) {
            Object packet = e.getPacket();
            if (PlayerPacket.isTarget(packet)) {
                resetTickStates();
            } else {
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
                else if (HeldItemChangePacket.isTarget(packet)) {
                    ticksSinceHeldItemChange = 0;
                }
            }
        }
    }

    private void resetTickStates() {
        startDiggingTick = false;
        stopDiggingTick = false;
    }

    private void resetAllStates() {
        digging = false;
        resetTickStates();
        ticksSinceHeldItemChange = 0;
    }
}
