package pub.frost.client.feature.module.impl.combat.velocity;

import lombok.RequiredArgsConstructor;
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
        Object packet = event.getPacket();
        boolean cancel = false;
        if (EntityVelocityPacket.isTarget(packet)) {
            Object player = Minecraft.getPlayer(mc);
            if (EntityVelocityPacket.getEntityId(packet) != Entity.getEntityId(player)) return;
            if (!airOnly.get() || !Entity.isOnGround(player)) {
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
            boolean groundForceReleaseReached = untilGround.get() && Entity.isOnGround(Minecraft.getPlayer(mc));

            if (maxTickReached || groundForceReleaseReached) {
                Packet.processPacket(
                        cachedPacket.packet,
                        Minecraft.getNetHandler(mc)
                );
                cachedPackets.remove(cachedPacket);
            } else cachedPacket.ticks++;
        }
    }

    public void flush() {
        while (!cachedPackets.isEmpty()) {
            Packet.processPacket(
                    cachedPackets.pollFirst().packet,
                    Minecraft.getNetHandler(mc)
            );
        }
    }

    @Override
    public Boolean get() {
        return !parent.cancel.get();
    }

    @RequiredArgsConstructor
    private static class CachedPacket {
        final Object packet;
        int ticks = 0;
    }
}
