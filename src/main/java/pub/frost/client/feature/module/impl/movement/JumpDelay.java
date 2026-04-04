package pub.frost.client.feature.module.impl.movement;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventSetJumpDelay;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.number.IntegerProperty;

@Module(
        key = "JumpDelay",
        category = ModuleCategory.MOVEMENT
)
public class JumpDelay extends AbstractModule {
    @Property("delay")
    public final IntegerProperty delay = new IntegerProperty(0, 10, 1, 1);

    @EventHandler
    private void onSetJumpDelay(EventSetJumpDelay event) {
        event.setDelay(delay.get());
    }
}
