package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.impl;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.*;
import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.MainPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.child.PropertyPanel;
import pub.frost.utils.InputUtils;
import pub.frost.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModuleListPanel extends MainPanel {
    private final List<PropertyPanel> leftPanels, rightPanels;
    private float panelWidth = 0;
    public ModuleListPanel(PanelClickGui gui, ModuleCategory category) {
        super(gui);
        leftPanels = new ArrayList<>();
        rightPanels = new ArrayList<>();
        int leftSize = 0, rightSize = 0;
        final Supplier<Float> widthSupplier = () -> panelWidth;
        for (AbstractModule module : FrostCore.getInstance().getModuleManager().getModulesByCategory(category)) {
            boolean left = leftSize <= rightSize;
            int propSize = module.getPropertyList().size();
            PropertyPanel panel = new PropertyPanel(gui, module, widthSupplier);
            if (left) {
                leftPanels.add(panel);
                leftSize += propSize;
            } else {
                rightPanels.add(panel);
                rightSize += propSize;
            }
        }
    }

    @Override
    public void renderPanelContent(boolean dummy, float tickDelta) {
        renderModules(dummy, tickDelta);
        renderScrollBar(dummy, tickDelta, ImGui.getItemRectSize());
    }

    private void renderModules(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 0);

        ImGui.beginChild(
                this + ".modules",
                0f, 0f,
                ImGuiChildFlags.AlwaysUseWindowPadding | ImGuiChildFlags.AutoResizeY,
                ImGuiWindowFlags.NoScrollbar
        );
        {
            ImVec2 size = ImGui.getContentRegionAvail();
            panelWidth = (size.x - 8) / 2;

            ImGui.beginGroup();
            for (PropertyPanel panel : leftPanels) panel.render(dummy, tickDelta);
            ImGui.endGroup();

            ImGui.sameLine();

            ImGui.beginGroup();
            for (PropertyPanel panel : rightPanels) panel.render(dummy, tickDelta);
            ImGui.endGroup();
        }
        ImGui.endChild();

        ImGui.popStyleVar(2);
    }
    private void renderScrollBar(boolean dummy, float tickDelta, ImVec2 contentSize) {
        float scrollMaxY = ImGui.getScrollMaxY();
        if (scrollMaxY > 0f) {
            float scrollY = ImGui.getScrollY();
            ImVec2 windowPos = ImGui.getWindowPos();
            ImVec2 windowSize = ImGui.getWindowSize();

            float trackWidth = 4f;
            float trackPadding = 4f;
            float rounding = trackWidth / 2f;

            float trackMinX = windowPos.x + windowSize.x - trackPadding - trackWidth;
            float trackMaxX = windowPos.x + windowSize.x - trackPadding;
            float trackMinY = windowPos.y + 8f;
            float trackMaxY = windowPos.y + windowSize.y - 8f;
            float trackHeight = trackMaxY - trackMinY;

            float visibleRatio = Math.min(1f, contentSize.y / (contentSize.y + scrollMaxY));
            float thumbHeight = Math.max(24f, trackHeight * visibleRatio);
            float thumbTravel = Math.max(0f, trackHeight - thumbHeight);
            float thumbOffset = thumbTravel * (scrollY / scrollMaxY);

            float thumbMinY = trackMinY + thumbOffset;
            float thumbMaxY = thumbMinY + thumbHeight;

            ImVec2 cursorPos = ImGui.getCursorPos();
            ImGui.setCursorScreenPos(trackMinX, trackMinY);
            ImGui.invisibleButton(this + "##scrollbar", trackWidth, trackHeight);

            boolean hovered = ImGui.isItemHovered();
            boolean held = ImGui.isItemActive();

            float scrollOffset = 0;
            if (held) {
                float mouseDeltaY = ImGui.getIO().getMouseDeltaY();
                scrollOffset += thumbTravel <= 0f ? 0f : mouseDeltaY / thumbTravel * scrollMaxY;

                scrollY = ImGui.getScrollY();
                thumbOffset = thumbTravel * (scrollY / scrollMaxY);
                thumbMinY = trackMinY + thumbOffset;
                thumbMaxY = thumbMinY + thumbHeight;
            }
            if (scrollOffset != 0) {
                ImGui.setScrollY(MathUtils.clamp(scrollY + scrollOffset, 0f, ImGui.getScrollMaxY()));
            }

            ImGui.setCursorPos(cursorPos);

            int trackColor = gui.getTheme().getScrollbarTrackColor();
            int thumbColor = held?
                    gui.getTheme().getScrollbarThumbActiveColor()
                    : hovered? gui.getTheme().getScrollbarThumbHoverColor()
                      : gui.getTheme().getScrollbarThumbColor();


            if (!dummy) {
                ImDrawList drawList = ImGui.getWindowDrawList();
                drawList.addRectFilled(
                        trackMinX, trackMinY,
                        trackMaxX, trackMaxY,
                        trackColor, rounding
                );
                drawList.addRectFilled(
                        trackMinX, thumbMinY,
                        trackMaxX, thumbMaxY,
                        thumbColor, rounding
                );
            }
        }
    }
}
