package pub.frost.client.feature.module.impl.combat;

import lombok.RequiredArgsConstructor;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerAttackSlowdown;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.mode.ModeProperty;

@Module(
        key = "KeepSprint",
        category = ModuleCategory.COMBAT
)
public class KeepSprint extends AbstractModule {
    @Property("mode")
    public final ModeProperty<Mode> mode = new ModeProperty<>(Mode.VANILLA);

    @EventHandler
    private void onAttackSlowdown(EventPlayerAttackSlowdown event) {
        event.setCancelSprint(false);
        if (!mode.is(Mode.CLIENT)) {
            event.setXMultiplier(event.getXMultiplier() / 0.6);
            event.setZMultiplier(event.getZMultiplier() / 0.6);
        }
    }

    @TranslationKey("strings.enum.keepsprint.modes.~")
    @RequiredArgsConstructor
    public enum Mode implements Named {
        VANILLA("vanilla"),
        CLIENT("ClientOnly");
        final String key;
        @Override
        public String toString() {
            return key;
        }
    }
}
