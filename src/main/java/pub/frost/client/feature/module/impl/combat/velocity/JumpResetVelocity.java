package pub.frost.client.feature.module.impl.combat.velocity;

import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.Velocity;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.client.property.impl.number.PercentProperty;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

@SubModule(Velocity.class)
public class JumpResetVelocity extends AbstractSubModule<Velocity> implements Supplier<Boolean> {
    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(false);

    @Property("jumpChance")
    public final PercentProperty jumpChance = new PercentProperty(0f, 1f, 1f);
    @Property("JumpDelay")
    public final IntegerProperty jumpDelay = new IntegerProperty(0, 3, 1, 1);

    private final Deque<Boolean> scheduledJumpDeque = new ArrayDeque<>();

    public void tickMovement(EventUpdateMovementInput event) {
        if (getParent().isHasVelocity()) {
            for (int i = 0; i < jumpDelay.getValue(); i++) {
                scheduledJumpDeque.offerLast(false);
            }
            scheduledJumpDeque.offerLast(true);
        }
        tryJump(event);
    }
    public void tryJump(EventUpdateMovementInput event) {
        if (scheduledJumpDeque.isEmpty()) return;
        if (!scheduledJumpDeque.pollFirst()) return;
        if (Math.random() <= jumpChance.get()) event.setJump(true);
    }

    @Override
    public Boolean get() {
        return !getParent().cancel.get();
    }
}
