package pub.frost.client.feature.module.impl.visual;

import imgui.ImGui;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.utils.ImTextRenderer;

import java.util.stream.Collectors;

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

        float yIndex = 40;
        for (
                AbstractModule m
                : FrostCore.getInstance().getModuleManager().getModules(AbstractModule::isEnabled).stream()
                .sorted((m1, m2) -> {
                    float diff = ImTextRenderer.getTextWidth(m2.getName()) - ImTextRenderer.getTextWidth(m1.getName());
                    return diff < 0? -1 : diff == 0? 0 : 1;
                }).collect(Collectors.toList())
        ) {
            ImTextRenderer.drawShadowedText(ImGui.getBackgroundDrawList(), m.getName(), 4, yIndex, -1);
            yIndex += ImTextRenderer.getTextHeight() + 2;
        }
        ImGui.popFont();
    }
}
