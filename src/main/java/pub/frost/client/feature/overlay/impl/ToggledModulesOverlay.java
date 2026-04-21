package pub.frost.client.feature.overlay.impl;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.overlay.ClientOverlay;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.utils.ImTextRenderer;
import pub.frost.utils.data.EnumTextFormatting;

public class ToggledModulesOverlay extends ClientOverlay {
    public ToggledModulesOverlay() {
        super("overlays.toggledmodules");
    }

    @Property("Sidebar")
    public final BooleanProperty sidebar = new BooleanProperty(true);
    @Property("SidebarGradient")
    public final BooleanProperty sidebarGradient = new BooleanProperty(true);

    @Override
    protected void doRender(boolean dummy, boolean input, float tickDelta) {
        int windowFlags = DEFAULT_WINDOW_FLAGS;
        if (!input) windowFlags |= ImGuiWindowFlags.NoInputs;
        ImGui.begin(this.toString(), windowFlags);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
        ImGui.pushFont(FontManager.INSTANCE.puhui14);

        for (
                AbstractModule module :
                FrostCore.getInstance().getModuleManager().getModules(AbstractModule::isEnabled, (m1, m2) -> {
                    float diff = ImTextRenderer.getTextWidth(m2.getName()) - ImTextRenderer.getTextWidth(m1.getName());
                    return diff < 0? -1 : diff == 0? 0 : 1;
                })
        ) {
            renderModule(
                    dummy, input, tickDelta,
                    module
            );
        }

        ImGui.popFont();
        ImGui.popStyleVar();
        ImGui.end();
    }

    private void renderModule(boolean dummy, boolean input, float tickDelta, AbstractModule module) {
        ImGui.beginGroup();
        ImVec2 dummyPosMin;
        ImVec2 textPos;
        ImVec2 panelPosMin, panelPosMax;
        String moduleName = module.getName();

        if (sidebar.get()) {
            ImGui.dummy(2, 0);
            dummyPosMin = ImGui.getItemRectMin();
            ImGui.sameLine();
        } else dummyPosMin = ImGui.getCursorScreenPos();

        {
            ImGui.beginGroup();
            ImGui.dummy(0, 4);
            ImGui.dummy(10, 0);
            ImGui.sameLine();
            ImGui.textColored(0, EnumTextFormatting.removeFormat(moduleName));
            textPos = ImGui.getItemRectMin();
            ImGui.sameLine();
            ImGui.dummy(10, 0);
            ImGui.dummy(0, 4);
            ImGui.endGroup();
            panelPosMin = ImGui.getItemRectMin();
            panelPosMax = ImGui.getItemRectMax();
        }

        if (!dummy) {
            ImDrawList draws = ImGui.getWindowDrawList();
            if (sidebar.get()) {
                float panelHeight = panelPosMax.y - panelPosMin.y;
                draws.addRectFilled(
                        dummyPosMin, dummyPosMin.plus(2, panelHeight),
                        ImColor.rgba("#E5EAFFFF")
                );
                if (sidebarGradient.get()) {
                    draws.addRectFilledMultiColor(
                            dummyPosMin, dummyPosMin.plus(8, panelHeight),
                            ImColor.rgba("#E5EAFFB2"),
                            ImColor.rgba("#E5EAFF00"),
                            ImColor.rgba("#E5EAFF00"),
                            ImColor.rgba("#E5EAFFB2")
                    );
                }
            }
            draws.addRectFilled(
                    panelPosMin, panelPosMax,
                    ImColor.rgba("#141933CC")
            );
            ImTextRenderer.drawText(
                    draws,
                    moduleName,
                    textPos.x, textPos.y,
                    ImColor.rgba("#E5EAFFFF")
            );
        }

        ImGui.endGroup();
    }
}
