package pub.frost.client.feature.module.impl.combat;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventPlayerVelocity;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.module.impl.combat.velocity.BasicVelocity;
import pub.frost.client.feature.module.impl.combat.velocity.DelayVelocity;
import pub.frost.client.feature.module.impl.combat.velocity.JumpResetVelocity;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;

@Module(
        key = "velocity",
        category = ModuleCategory.COMBAT
)
public class Velocity extends AbstractModule {
    @Property("MotionCheck")
    public final BooleanProperty motionCheck = new BooleanProperty(true);
    @Property("Cancel")
    public final BooleanProperty cancel = new BooleanProperty(false);

    @InsertProperty("Basic")
    public final BasicVelocity basic = new BasicVelocity(this);
    @InsertProperty("JumpReset")
    public final JumpResetVelocity jumpReset = new JumpResetVelocity(this);
//    @InsertProperty("Delay")
    public final DelayVelocity delay = new DelayVelocity(this);

    private boolean hasVelocity;

    @EventHandler
    public void onPostUpdate(EventPlayerUpdateTick event) {
        if (event.getType() == TickType.POST) hasVelocity = false;
    }

    @EventHandler
    public void onVelocity(EventPlayerVelocity event) {
        if (cancel.get()) event.cancel();
        else {
            this.hasVelocity = !motionCheck.get() || hasMotion(event);
            if (!hasVelocity) return;

            basic.processVelocity(event);
        }
    }

    @EventHandler
    public void onMovementInput(EventUpdateMovementInput event) {
        if (hasVelocity) {
            jumpReset.tryJump(event);
        }
    }

    private boolean hasMotion(EventPlayerVelocity event) {
        return event.getMotionX() != 0 || event.getMotionZ() != 0;
    }
}
