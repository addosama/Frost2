package pub.frost.client.feature.module.impl.combat;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUpdateTick;
import pub.frost.base.event.impl.events.EventPlayerVelocity;
import pub.frost.base.event.impl.events.EventUpdateMovementInput;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.PercentProperty;

@Module(
        key = "velocity",
        category = ModuleCategory.COMBAT
)
public class Velocity extends AbstractModule {
    @Property("MotionCheck")
    public final BooleanProperty motionCheck = new BooleanProperty(true);

    @Property("editMotion")
    public final BooleanProperty editMotion = new BooleanProperty(false);
    @Property("cancel")
    public final BooleanProperty cancel = new BooleanProperty(false).setVisibilitySupplier(BooleanProperty.class, editMotion::get);
    @Property("motionX")
    public final PercentProperty motionX = new PercentProperty(-1f, 1f, 0f).setVisibilitySupplier(PercentProperty.class, this::shouldEditMotion);
    @Property("motionY")
    public final PercentProperty motionY = new PercentProperty(-1f, 1f, 1f).setVisibilitySupplier(PercentProperty.class, this::shouldEditMotion);
    @Property("motionZ")
    public final PercentProperty motionZ = new PercentProperty(-1f, 1f, 0f).setVisibilitySupplier(PercentProperty.class, this::shouldEditMotion);

    @Property("jumpReset")
    public final BooleanProperty jumpReset = new BooleanProperty(true);
    @Property("jumpChance")
    public final PercentProperty jumpChance = new PercentProperty(0f, 1f, 1f).setVisibilitySupplier(PercentProperty.class, jumpReset::get);

    private boolean hasVelocity;

    @EventHandler
    public void onPostUpdate(EventPlayerUpdateTick event) {
        if (event.getType() == TickType.POST) hasVelocity = false;
    }

    @EventHandler
    public void onVelocity(EventPlayerVelocity event) {
        this.hasVelocity = !motionCheck.get() || hasMotion(event);
        if (!hasVelocity) return;

        if (editMotion.get()) {
            if (cancel.get()) {
                event.cancel();
            } else {
                event.setXMultiplier(motionX.getValue());
                event.setYMultiplier(motionY.getValue());
                event.setZMultiplier(motionZ.getValue());
            }
        }
    }

    @EventHandler
    public void onMovementInput(EventUpdateMovementInput event) {
        if (hasVelocity) {
            if (jumpReset.get()) {
                if (Math.random() <= jumpChance.get()) event.setJump(true);
            }
        }
    }

    private boolean hasMotion(EventPlayerVelocity event) {
        return event.getMotionX() != 0 || event.getMotionZ() != 0;
    }
    private boolean shouldEditMotion() {
        return editMotion.get() && !cancel.get();
    }
}
