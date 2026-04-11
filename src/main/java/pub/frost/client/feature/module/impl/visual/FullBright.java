package pub.frost.client.feature.module.impl.visual;

import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventUpdateLightMap;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;

@Module(
        key = "FullBright",
        category = ModuleCategory.VISUAL
)
public class FullBright extends AbstractModule {
    @EventHandler
    private void onUpdateLightMap(EventUpdateLightMap event) {
        event.setGamma(10000);
    }
}

