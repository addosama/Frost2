package pub.frost.client.feature.helper.player.interact;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.utils.api.Tickable;
import pub.frost.utils.recorder.Recorder;
import pub.frost.utils.recorder.impl.state.SingleTickStateRecorder;
import pub.frost.utils.recorder.impl.state.TickableStateRecorder;

import java.util.Arrays;
import java.util.List;

@Getter
public class PlayerListener implements IPlayerListener {
    private final Minecraft mc = Minecraft.getMinecraft();

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
            if (mc.theWorld == null) resetAllStates();
        }
    }

    @EventHandler(priority = 100)
    public void prePacket(EventPacket e) {
        if (e.isCancelled()) return;
        if (e.getType() == PacketType.OUT) {
            Object packet = e.getPacket();
            if (packet instanceof C03PacketPlayer) {
                tickableList.forEach(Tickable::tick);
            }
            else {
                if (packet instanceof C07PacketPlayerDigging) {
                    C07PacketPlayerDigging c07 =  (C07PacketPlayerDigging) packet;
                    if (c07.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        diggingStateRecorder.setValue(true);
                    }
                    if (c07.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK
                            || c07.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
                    ) {
                        diggingStateRecorder.setValue(false);
                    }
                }
                else if (packet instanceof C09PacketHeldItemChange) {
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
