package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerAttackEntity;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventPrePlayerMotionUpdate;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.IntegerProperty;

@Module(
        key = "SprintReset",
        category = ModuleCategory.COMBAT
)
public class SprintReset extends AbstractModule {
    @Property("mode")
    public final ModeProperty<Mode> mode = new ModeProperty<>(Mode.NO_STOP);
    @Property("duration")
    public final IntegerProperty duration = new IntegerProperty(1, 10, 1, 1);

    private boolean reset;
    private int ticksSinceReset;

    @EventHandler
    private void onAttack(EventPlayerAttackEntity e) {
        resetRecorder(true);
        if (reset && ticksSinceReset <= duration.getValue()) {
            if (mode.is(Mode.NO_STOP)) Entity.setSprinting(mcWrapper.getPlayer(mc), false);
        }
    }

    @EventHandler
    private void onUpdate(EventPlayerUpdateTick event) {
        if (event.getType() == TickType.POST) {
            ticksSinceReset ++;
        } else {
            if (ticksSinceReset >= duration.getValue()) {
                resetRecorder(false);
            }
        }
    }

    @EventHandler
    private void preMotion(EventPrePlayerMotionUpdate e) {
    }

    @EventHandler(priority = 5)
    private void onMoveInput(EventUpdateMovementInput e) {
        if (reset && ticksSinceReset <= duration.getValue()) {
            if (mode.is(Mode.SNEAK)) e.setSneak(true);
        }
    }

    private void resetRecorder(boolean attacked) {
        this.reset = attacked;
        ticksSinceReset = 0;
    }

    @TranslationKey("strings.enum.sprintreset.modes.~")
    @RequiredArgsConstructor
    public enum Mode implements Named {
        NO_STOP("nostop"),
        SNEAK("sneak"),;
        final String key;

        @Override
        public String toString() {
            return key;
        }
    }
}
