package pub.frost.client.feature.module.impl.visual;

import imgui.ImGui;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.utils.ImTextRenderer;

@Module(
        key = "hud",
        category = ModuleCategory.VISUAL,
        defaultState = true
)
public class HUD extends AbstractModule {
    @EventHandler
    public void onRender(EventRender2D e) {
        ImGui.pushFont(FontManager.INSTANCE.puHui18);
        ImTextRenderer.drawShadowedText(ImGui.getBackgroundDrawList(), "Frost", 4, 4, -1);
        ImGui.popFont();
    }
}
