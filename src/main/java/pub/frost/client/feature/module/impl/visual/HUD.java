package pub.frost.client.feature.module.impl.visual;

import imgui.*;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventPostRender;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.overlay.impl.WatermarkOverlay;
import pub.frost.utils.ImTextRenderer;

@Module(
        key = "hud",
        category = ModuleCategory.VISUAL,
        defaultState = true
)
public class HUD extends AbstractModule {
    private final WatermarkOverlay watermark = new WatermarkOverlay();

    @EventHandler(priority = 50)
    public void onRender(EventRender2D e) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 2, 2);
        watermark.render(false, isInChatHud(), e.getTickDelta());
        ImGui.popStyleVar();

        ImGui.pushFont(FontManager.INSTANCE.puHui18);
        float yIndex = 44;
        for (
                AbstractModule m :
                FrostCore.getInstance().getModuleManager().getModules(AbstractModule::isEnabled, (m1, m2) -> {
                    float diff = ImTextRenderer.getTextWidth(m2.getName()) - ImTextRenderer.getTextWidth(m1.getName());
                    return diff < 0? -1 : diff == 0? 0 : 1;
                })
        ) {
            ImTextRenderer.drawShadowedText(ImGui.getBackgroundDrawList(), m.getName(), 8, yIndex, -1);
            yIndex += ImTextRenderer.getTextHeight() + 2;
        }
        ImGui.popFont();
    }

    @EventHandler(priority = 50)
    public void onPostRender(EventPostRender e) {
        watermark.render(true, isInChatHud(), e.getTickDelta());
    }

    private boolean isInChatHud() {
        return false;
    }
}
