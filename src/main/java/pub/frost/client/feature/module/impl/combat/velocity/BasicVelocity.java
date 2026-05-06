package pub.frost.client.feature.module.impl.combat.velocity;

import pub.frost.base.event.impl.events.EventPlayerVelocity;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.Velocity;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.PercentProperty;

import java.util.function.Supplier;

public class BasicVelocity extends AbstractSubModule<Velocity> implements Supplier<Boolean> {
    public BasicVelocity(Velocity velocity) {
        super(velocity);
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(false);

    @Property("motionX")
    public final PercentProperty motionX = new PercentProperty(-1f, 1f, 0f);
    @Property("motionY")
    public final PercentProperty motionY = new PercentProperty(-1f, 1f, 1f);
    @Property("motionZ")
    public final PercentProperty motionZ = new PercentProperty(-1f, 1f, 0f);


    public void processVelocity(EventPlayerVelocity event) {
        event.setXMultiplier(motionX.getValue());
        event.setYMultiplier(motionY.getValue());
        event.setZMultiplier(motionZ.getValue());
    }

    @Override
    public Boolean get() {
        return !parent.cancel.get();
    }
}
