package pub.frost.client.feature.module.impl.movement;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPlayerUseItemSlowdown;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.client.property.impl.number.PercentProperty;

@Module(
        key = "NoSlowdown",
        category = ModuleCategory.MOVEMENT
)
public class NoSlowdown extends AbstractModule {
    @Property("Forward")
    public final PercentProperty forward = new PercentProperty(0, 1, 0.2f);
    @Property("Strafe")
    public final PercentProperty strafe = new PercentProperty(0, 1, 0.2f);

    @Property("RequireC09")
    public final BooleanProperty requireC09 = new BooleanProperty(false);
    @Property("MaxC09TickPass")
    public final IntegerProperty maxC09TickPass = new IntegerProperty(0, 10, 1, 1).setVisibilitySupplier(requireC09::get);

    @EventHandler
    private void onUseItemSlowdown(EventPlayerUseItemSlowdown event) {
        if (requireC09.get() && FrostCore.getInstance().getPlayerListener().getTicksSinceHeldItemChange() > maxC09TickPass.get())
            return;

        event.setForward(forward.get());
        event.setStrafe(strafe.get());
    }
}
