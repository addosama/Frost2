package pub.frost.client.feature.module.impl.combat;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPacket;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventPlayerVelocity;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.base.event.impl.types.PacketType;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.module.impl.combat.velocity.AttackReduceVelocity;
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
    @InsertProperty("Delay")
    public final DelayVelocity delay = new DelayVelocity(this);
    @InsertProperty("AttackReduce")
    public final AttackReduceVelocity attackReduce = new AttackReduceVelocity(this);

    private boolean hasVelocity;

    @EventHandler
    public void onUpdate(EventPlayerUpdateTick event) {
        if (event.getType() == TickType.POST) {
            hasVelocity = false;
            if (delay.enabled.get()) delay.update();
        }
        else {
            if (attackReduce.enabled.get()) attackReduce.update();
        }
    }

    @EventHandler
    public void onPacket(EventPacket event) {
        if (event.getType() == PacketType.IN) {
            if (delay.enabled.get()) delay.processIncomingPacket(event);
        } else if (event.getType() == PacketType.OUT) {
            if (attackReduce.enabled.get()) attackReduce.processOutgoingPacket(event);
        }
    }

    @EventHandler
    public void onVelocity(EventPlayerVelocity event) {
        if (cancel.get()) event.cancel();
        else {
            if (attackReduce.enabled.get()) attackReduce.calculateReduceTicks(event.getMotionX(), event.getMotionZ());
            this.hasVelocity = !motionCheck.get() || hasMotion(event);
            if (!hasVelocity) return;

            if (basic.enabled.get()) basic.processVelocity(event);
        }
    }

    @EventHandler
    public void onMovementInput(EventUpdateMovementInput event) {
        if (hasVelocity) {
            if (jumpReset.enabled.get()) jumpReset.tryJump(event);
        }
    }

    @Override
    protected void onDisabled() {
        if (delay.enabled.get()) delay.flush();
    }

    private boolean hasMotion(EventPlayerVelocity event) {
        return event.getMotionX() != 0 || event.getMotionZ() != 0;
    }
}
