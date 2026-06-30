package pub.frost.client.feature.module.impl.combat.velocity;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import pub.frost.base.event.impl.events.EventProactiveLag;
import pub.frost.client.feature.helper.network.DelayedPacket;
import pub.frost.client.feature.helper.network.TickableLagTickSupplier;
import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.Velocity;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;

import java.util.*;
import java.util.function.Supplier;

@SubModule(Velocity.class)
public class DelayVelocity extends AbstractSubModule<Velocity> implements Supplier<Boolean> {
    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(false).setValueChangeListener(
            (o, state) -> {
                if (!state) flush();
            }
    );

    @Property("AirOnly")
    public final BooleanProperty airOnly = new BooleanProperty(true);
    @Property("UntilGround")
    public final BooleanProperty untilGround = new BooleanProperty(false).setVisibilitySupplier(airOnly::get);
    @Property("DelayTicks")
    public final IntegerProperty delayTicks = new IntegerProperty(1, 10, 1, 1).setVisibilitySupplier(() -> !untilGround.get());

    private boolean flag = false;
    private DelayedPacket delayed = null;

    public void processIncomingPacket(EventProactiveLag event) {
        if (delayed != null) {
            if (delayed.isFlushed()) delayed = null;
            else return;
        }

        Packet packet = event.getEventPacket();
        if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) packet;
            EntityPlayerSP player = mc.thePlayer;
            if (s12.getEntityID() != player.getEntityId()) return;
            boolean airOnly = this.airOnly.get();
            if (!airOnly || !player.onGround) {
                flag = airOnly && untilGround.get();
                event.setLagTicksSupplier(
                        flag?
                                () -> 1
                                : new TickableLagTickSupplier(delayTicks.get())
                );
                event.setDelayedPacketConsumer(d -> delayed = d);
            }
        }
    }

    public void update() {
        if (delayed == null || !flag) return;
        if (mc.thePlayer.onGround) {
            flush();
        }
    }

    public void flush() {
        if (delayed != null) {
            delayed.setForceFlush(true);
            delayed = null;
        }
    }

    @Override
    public Boolean get() {
        return !getParent().cancel.get();
    }
}
