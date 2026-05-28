package pub.frost.client.feature.helper.player.interact;

import lombok.Getter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.utils.api.Tickable;
import pub.frost.utils.recorder.Recorder;
import pub.frost.utils.recorder.impl.state.SingleTickStateRecorder;
import pub.frost.utils.recorder.impl.state.TickableStateRecorder;
import pub.frost.wrappers.shared.network.packet.impl.play.c2s.WPlayerDiggingPacket;

import java.util.Arrays;
import java.util.List;

@Getter
public class PlayerListener implements Wrappers, IPlayerListener {
    private final Object mc = Minecraft.getInstance();

    private final TickableStateRecorder diggingStateRecorder = new TickableStateRecorder();
    private final SingleTickStateRecorder heldItemChangeRecorder = new SingleTickStateRecorder();

    private final List<Tickable> tickableList = Arrays.asList(
            diggingStateRecorder,
            heldItemChangeRecorder
    );
    private final List<Recorder<?>> recorderList = Arrays.asList(
            diggingStateRecorder,
            heldItemChangeRecorder
    );

    @EventHandler(priority = -1)
    public void preGameTick(EventGameTick event) {
        if (event.getType() == TickType.PRE) {
            if (Minecraft.getWorld(mc) == null) resetAllStates();
        }
    }

    @EventHandler(priority = 100)
    public void prePacket(EventPacket e) {
        if (e.isCancelled()) return;
        if (e.getType() == PacketType.OUT) {
            Object packet = e.getPacket();
            if (PlayerPacket.isTarget(packet)) {
                tickableList.forEach(Tickable::tick);
            }
            else {
                if (PlayerDiggingPacket.isTarget(packet)) {
                    if (PlayerDiggingPacket.getStatus(packet) == WPlayerDiggingPacket.START_DESTROY_BLOCK) {
                        diggingStateRecorder.setValue(true);
                    }
                    if (PlayerDiggingPacket.getStatus(packet) == WPlayerDiggingPacket.ABORT_DESTROY_BLOCK
                            || PlayerDiggingPacket.getStatus(packet) == WPlayerDiggingPacket.STOP_DESTROY_BLOCK
                    ) {
                        diggingStateRecorder.setValue(false);
                    }
                }
                else if (HeldItemChangePacket.isTarget(packet)) {
                    heldItemChangeRecorder.setValue(true);
                }
            }
        }
    }

    private void resetAllStates() {
        recorderList.forEach(Recorder::reset);
    }

    @Override
    public boolean isDigging() {
        return diggingStateRecorder.getValue();
    }
    @Override
    public boolean isStartDiggingTick() {
        return diggingStateRecorder.getTicksSinceTrue() == 0;
    }
    @Override
    public boolean isStopDiggingTick() {
        return diggingStateRecorder.getTicksSinceFalse() == 0;
    }

    @Override
    public boolean isHeldItemChangeTick() {
        return heldItemChangeRecorder.getValue();
    }
    @Override
    public int getTicksSinceHeldItemChange() {
        return heldItemChangeRecorder.getTicksSinceTrue();
    }
}
