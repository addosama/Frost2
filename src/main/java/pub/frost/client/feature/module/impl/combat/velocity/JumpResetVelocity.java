package pub.frost.client.feature.module.impl.combat.velocity;

import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.Velocity;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.PercentProperty;

import java.util.function.Supplier;

public class JumpResetVelocity extends AbstractSubModule<Velocity> implements Supplier<Boolean> {
    public JumpResetVelocity(Velocity velocity) {
        super(velocity);
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(false);

    @Property("jumpChance")
    public final PercentProperty jumpChance = new PercentProperty(0f, 1f, 1f);

    public void tryJump(EventUpdateMovementInput event) {
        if (Math.random() <= jumpChance.get()) event.setJump(true);
    }

    @Override
    public Boolean get() {
        return !parent.cancel.get();
    }
}
