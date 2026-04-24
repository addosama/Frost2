package pub.frost.client.feature.module.impl.combat.velocity;

import pub.frost.client.feature.module.api.SubModule;
import pub.frost.client.feature.module.impl.combat.Velocity;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;

import java.util.function.Supplier;

public class DelayVelocity extends SubModule<Velocity> implements Supplier<Boolean> {
    public DelayVelocity(Velocity velocity) {
        super(velocity);
    }

    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(false);

    @Override
    public Boolean get() {
        return !parent.cancel.get();
    }
}
