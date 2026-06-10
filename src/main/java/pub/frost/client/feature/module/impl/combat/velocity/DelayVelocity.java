package pub.frost.client.feature.module.impl.combat.velocity;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.Velocity;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;

import java.util.*;
import java.util.function.Supplier;

public class DelayVelocity extends AbstractSubModule<Velocity> implements Supplier<Boolean> {
    public DelayVelocity(Velocity velocity) {
        super(velocity);
    }

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

    private final Deque<CachedPacket> cachedPackets = new ArrayDeque<>();

    public void processIncomingPacket(EventPacket event) {
        Packet packet = event.getPacket();
        boolean cancel = false;
        if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) packet;
            EntityPlayerSP player = mc.thePlayer;
            if (s12.getEntityID() != player.getEntityId()) return;
            if (!airOnly.get() || !player.onGround) {
                cancel = true;
            }
        } else if (!cachedPackets.isEmpty()) {
            cancel = true;
        }
        if (cancel) {
            event.cancel();
            cachedPackets.offerLast(new CachedPacket(packet));
        }
    }

    public void update() {
        for (CachedPacket cachedPacket : cachedPackets) {
            boolean maxTickReached = cachedPacket.ticks > delayTicks.get();
            boolean groundForceReleaseReached = untilGround.get() && mc.thePlayer.onGround;

            if (maxTickReached || groundForceReleaseReached) {
                cachedPacket.packet.processPacket(mc.getNetHandler());
                cachedPackets.remove(cachedPacket);
            } else cachedPacket.ticks++;
        }
    }

    public void flush() {
        while (!cachedPackets.isEmpty()) {
            cachedPackets.pollFirst().packet.processPacket(mc.getNetHandler());
        }
    }

    @Override
    public Boolean get() {
        return !parent.cancel.get();
    }

    @RequiredArgsConstructor
    private static class CachedPacket {
        final Packet<INetHandlerPlayClient> packet;
        int ticks = 0;
    }
}
