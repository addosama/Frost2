package pub.frost.client.feature.module.impl.visual;

import imgui.*;
import net.minecraft.client.gui.GuiScreen;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventPostRender;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.overlay.ClientOverlay;
import pub.frost.client.feature.overlay.impl.ToggledModulesOverlay;
import pub.frost.client.feature.overlay.impl.WatermarkOverlay;
import pub.frost.client.property.annotations.InsertProperty;

import java.util.Arrays;
import java.util.List;

@Module(
        key = "hud",
        category = ModuleCategory.VISUAL,
        defaultState = true
)
public class HUD extends AbstractModule {
    @InsertProperty("Watermark")
    private final WatermarkOverlay watermark = new WatermarkOverlay();
    @InsertProperty("ToggledModules")
    private final ToggledModulesOverlay toggledModules = new ToggledModulesOverlay();

    private final List<ClientOverlay> overlayList = Arrays.asList(
            watermark, toggledModules
    );

    @EventHandler(priority = 120)
    public void onPreTick(EventGameTick event) {
        if (event.getType() == TickType.PRE) overlayList.forEach(ClientOverlay::tick);
    }

    @EventHandler(priority = -50)
    public void onRender(EventRender2D e) {
        overlayList.forEach(o -> o.render(false, isInChatHud(), e.getTickDelta()));
    }

    @EventHandler(priority = -50)
    public void onPostRender(EventPostRender e) {
        overlayList.forEach(o -> o.render(true, isInChatHud(), e.getTickDelta()));
    }

    private boolean isInChatHud() {
        Object currentScreen = mc.currentScreen;
        return currentScreen instanceof GuiScreen;
    }
}
