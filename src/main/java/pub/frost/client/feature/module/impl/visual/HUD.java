package pub.frost.client.feature.module.impl.visual;

import imgui.*;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
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

        drawWatermark();

        float yIndex = 44;
        for (
                AbstractModule m
                : FrostCore.getInstance().getModuleManager().getModules(AbstractModule::isEnabled).stream()
                .sorted((m1, m2) -> {
                    float diff = ImTextRenderer.getTextWidth(m2.getName()) - ImTextRenderer.getTextWidth(m1.getName());
                    return diff < 0? -1 : diff == 0? 0 : 1;
                }).collect(Collectors.toList())
        ) {
            ImTextRenderer.drawShadowedText(ImGui.getBackgroundDrawList(), m.getName(), 8, yIndex, -1);
            yIndex += ImTextRenderer.getTextHeight() + 2;
        }
        ImGui.popFont();
    }

    private void drawWatermark() {
        float x = 8, y = 8;
        int bgColor = ImColor.rgba(20, 25, 51, 204);
        int textColor = ImColor.rgba("#E5EAFFFF");
        ImDrawList draws = ImGui.getBackgroundDrawList();
        {
            ImGui.pushFont(FontManager.INSTANCE.icon14);
            draws.addRectFilled(
                    x, y, x + 30, y + 30,
                    bgColor, 12f
            );
            draws.addText(
                    x + 8, y + 7,
                    textColor, "\ue601"
            );
            ImGui.popFont();
            x += 30;
        }
        x += 5;
        {
            String text = "Frost";
            float width = ImGui.calcTextSizeX(text) + 18f;
            draws.addRectFilled(
                    x, y, x + width, y + 30,
                    bgColor, 12f
            );
            draws.addText(
                    x + 9, y + 2,
                    textColor, text
            );
        }
    }
}
