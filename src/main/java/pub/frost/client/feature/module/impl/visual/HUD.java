package pub.frost.client.feature.module.impl.visual;

import imgui.*;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPostRender;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.overlay.ClientOverlay;
import pub.frost.client.feature.overlay.impl.ToggledModulesOverlay;
import pub.frost.client.feature.overlay.impl.WatermarkOverlay;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.color.ColorProperty;

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
    @Property("colorTest")
    private final ColorProperty colorTest = new ColorProperty(0x33000000);
    @Property("colorTestNoAlpha")
    private final ColorProperty colorNoAlpha = new ColorProperty(0x33FFFFFF, false);

    private final List<ClientOverlay> overlayList = Arrays.asList(
            watermark, toggledModules
    );

    @EventHandler(priority = -50)
    public void onRender(EventRender2D e) {
        overlayList.forEach(o -> o.render(false, isInChatHud(), e.getTickDelta()));
        ImGui.getForegroundDrawList().addRectFilled(
                0f, 0f, 100f, 100f,
                colorTest.getValue()
        );
        ImGui.getForegroundDrawList().addRectFilled(
                100f, 0f, 200f, 100f,
                colorNoAlpha.getValue()
        );
    }

    @EventHandler(priority = -50)
    public void onPostRender(EventPostRender e) {
        overlayList.forEach(o -> o.render(true, isInChatHud(), e.getTickDelta()));
    }

    private boolean isInChatHud() {
        Object currentScreen = mcWrapper.getCurrentScreen(mc);
        return currentScreen != null && GuiChat.isTarget(currentScreen);
    }
}
