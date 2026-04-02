package pub.frost.client.feature.module.impl.movement;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventSprint;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;

@Module(
        key = "sprint",
        category = ModuleCategory.MOVEMENT,
        defaultState = true
)
public class Sprint extends AbstractModule {
    @EventHandler
    private void preMovementTick(EventSprint event) {
        event.setKeyDown(true);
    }
}
