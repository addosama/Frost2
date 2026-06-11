package pub.frost.client.feature.helper.player.interact;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.utils.api.Tickable;
import pub.frost.utils.recorder.Recorder;
import pub.frost.utils.recorder.impl.deque.DataDeque;
import pub.frost.utils.recorder.impl.deque.TickableDataDeque;
import pub.frost.utils.recorder.impl.state.SingleTickStateRecorder;
import pub.frost.utils.recorder.impl.state.TickableStateRecorder;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public class PlayerListener {
    private final Minecraft mc = Minecraft.getMinecraft();

    private final TickableStateRecorder diggingStateRecorder = new TickableStateRecorder();
    private final TickableStateRecorder usingStateRecorder = new TickableStateRecorder();

    private final SingleTickStateRecorder swingRecorder = new SingleTickStateRecorder();
    private final SingleTickStateRecorder attackRecorder = new SingleTickStateRecorder();
    private final SingleTickStateRecorder placeRecorder = new SingleTickStateRecorder();
    private final SingleTickStateRecorder heldItemChangeRecorder = new SingleTickStateRecorder();

    private final DataDeque<Vec3> positionDeque = new DataDeque<>(20);
    private final TickableDataDeque<Map.Entry<Vec3, Vec3>> velocityDeque = new TickableDataDeque<>(20);

    private final List<Tickable> tickableList = Arrays.asList(
            diggingStateRecorder,
            usingStateRecorder,

            swingRecorder,
            attackRecorder,
            placeRecorder,
            heldItemChangeRecorder,
            
            velocityDeque
    );
    private final List<Recorder<?>> recorderList = Arrays.asList(
            diggingStateRecorder,
            usingStateRecorder,

            swingRecorder,
            attackRecorder,
            placeRecorder,
            heldItemChangeRecorder,

            positionDeque
    );

    @EventHandler(priority = -1)
    public void preGameTick(EventGameTick event) {
        if (event.getType() == TickType.PRE) {
            if (mc.theWorld == null) resetAllStates();
        }
    }

    @EventHandler(priority = -1)
    public void prePacket(EventPacket e) {
        if (e.isCancelled()) return;
        Object packet = e.getPacket();
        if (e.getType() == PacketType.OUT) {
            if (packet instanceof C03PacketPlayer) {
                C03PacketPlayer c03 = (C03PacketPlayer) packet;
                if (
                        packet instanceof C03PacketPlayer.C04PacketPlayerPosition
                                || packet instanceof C03PacketPlayer.C06PacketPlayerPosLook
                ) {
                    positionDeque.offerValue(new Vec3(c03.getPositionX(), c03.getPositionY(), c03.getPositionZ()));
                }
                tickableList.forEach(Tickable::tick);
            }
            else {
                if (packet instanceof C02PacketUseEntity) {
                    attackRecorder.updateValue(true);
                }
                else if (packet instanceof C07PacketPlayerDigging) {
                    C07PacketPlayerDigging c07 =  (C07PacketPlayerDigging) packet;
                    switch (c07.getStatus()) {
                        case START_DESTROY_BLOCK: {
                            diggingStateRecorder.updateValue(true);
                            break;
                        }
                        case ABORT_DESTROY_BLOCK:
                        case STOP_DESTROY_BLOCK: {
                            diggingStateRecorder.updateValue(false);
                            break;
                        }

                        default: {
                            usingStateRecorder.updateValue(false);
                        }
                    }
                }
                else if (packet instanceof C08PacketPlayerBlockPlacement) {
                    C08PacketPlayerBlockPlacement c08 =  (C08PacketPlayerBlockPlacement) packet;
                    ItemStack stack = c08.getStack();
                    if (stack != null) {
                        if (stack.getItemUseAction() != EnumAction.NONE) {
                            usingStateRecorder.updateValue(true);
                        } else placeRecorder.updateValue(true);
                    }
                }
                else if (packet instanceof C09PacketHeldItemChange) {
                    heldItemChangeRecorder.updateValue(true);
                    usingStateRecorder.updateValue(false);
                }
                else if (packet instanceof C0APacketAnimation) {
                    swingRecorder.updateValue(true);
                }
            }
        }
        else {
            if (packet instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) packet;
                velocityDeque.offerValue(new AbstractMap.SimpleEntry<>(
                        mc.thePlayer.getPositionVector(),
                        new Vec3(
                                s12.getMotionX() / 8000d,
                                s12.getMotionY() / 8000d,
                                s12.getMotionZ() / 8000d
                        )
                ));
            }
        }
    }

    private void resetAllStates() {
        recorderList.forEach(Recorder::reset);
    }

    public boolean isDigging() {
        return diggingStateRecorder.getValue();
    }
    public boolean isStartDiggingTick() {
        return diggingStateRecorder.getTicksSinceTrue() == 0;
    }
    public boolean isStopDiggingTick() {
        return diggingStateRecorder.getTicksSinceFalse() == 0;
    }

    public boolean isHeldItemChangeTick() {
        return heldItemChangeRecorder.getValue();
    }
    public int getTicksSinceHeldItemChange() {
        return heldItemChangeRecorder.getTicksSinceTrue();
    }
}
