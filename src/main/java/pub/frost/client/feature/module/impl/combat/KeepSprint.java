package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.events.EventPlayerAttackSlowdown;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.mode.ModeProperty;

import java.util.ArrayDeque;
import java.util.Deque;

@Module(
        key = "KeepSprint",
        category = ModuleCategory.COMBAT
)
public class KeepSprint extends AbstractModule {
    @Property("mode")
    public final ModeProperty<Mode> mode = new ModeProperty<>(Mode.VANILLA);

    private boolean lagging = false;
    private int laggedTick = -1;
    private final Deque<Object> laggedPacketDeque = new ArrayDeque<>();

    @EventHandler
    private void onAttackSlowdown(EventPlayerAttackSlowdown event) {
        event.setCancelSprint(false);
        event.setXMultiplier(event.getXMultiplier() / 0.6);
        event.setZMultiplier(event.getZMultiplier() / 0.6);
    }

    @EventHandler
    private void onGameTick(EventGameTick event) {
        if (event.getType() == TickType.POST && lagging) {
            laggedTick++;
        }
    }

    @EventHandler
    private void onPlayerUpdate(EventPlayerUpdateTick e) {
        if (e.getType() == TickType.PRE) {
            if (mode.is(Mode.LAG)) {
                if (laggedTick >= 3) flushLaggedPackets();
            }
        }
    }

    @EventHandler
    private void onPacket(EventPacket event) {
        if (mcWrapper.getWorld(mc) == null) return;
        if (mode.is(Mode.LAG)) {
            if (event.getType() == PacketType.OUT) {
                Object packet = event.getPacket();
                if (lagging) {
                    laggedPacketDeque.offerLast(packet);
                    event.cancel();
                } else if (UseEntityPacket.isTarget(packet)) {
                    laggedPacketDeque.offerLast(packet);
                    event.cancel();
                    startLagging();
                }
            } else if (lagging) event.cancel();
        }
    }

    private void startLagging() {
        laggedTick = 0;
        lagging = true;
    }
    private void flushLaggedPackets() {
        while (!laggedPacketDeque.isEmpty()) {
            FrostCore.getInstance().getPacketManager().sendPacket(laggedPacketDeque.pollFirst(), false);
        }
        laggedTick = -1;
        lagging = false;
    }

    @RequiredArgsConstructor
    public enum Mode implements Named {
        VANILLA("vanilla"),
        LAG("lag");
        final String key;
        @Override
        public String toString() {
            return key;
        }
        @Override
        public String getName() {
            return FrostCore.getLocalizer().getName("strings.keepsprint.modes." + this);
        }
    }
}
