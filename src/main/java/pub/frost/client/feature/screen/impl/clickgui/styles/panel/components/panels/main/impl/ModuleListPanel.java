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
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.child.ModulePanel;
import pub.frost.utils.InputUtils;
import pub.frost.utils.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModuleListPanel extends MainPanel {
    private final List<ModulePanel> leftPanels, rightPanels;
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
            ModulePanel panel = new ModulePanel(gui, module, widthSupplier);
            if (left) {
                leftPanels.add(panel);
                leftSize += propSize;
            } else {
                rightPanels.add(panel);
                rightSize += propSize;
            }
        }
    }

    private float wheelInput = 0;
    private float targetScroll = 0;

    @Override
    public void render(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12, 8);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 8, 8);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, gui.getTheme().getMainPanelBgColor());
        ImGui.beginChild(
                this.toString(),
                0f, 0f,
                ImGuiChildFlags.AlwaysUseWindowPadding,
                ImGuiWindowFlags.NoScrollbar
        );
        ImVec2 size = ImGui.getContentRegionAvail();

        panelWidth = (size.x - 8) / 2;
        float leftHeight, rightHeight;

        ImGui.beginGroup();
        for (ModulePanel panel : leftPanels) panel.render(dummy, tickDelta);
        ImGui.endGroup();
        leftHeight = ImGui.getCursorPosY();

        ImGui.sameLine();

        ImGui.beginGroup();
        for (ModulePanel panel : rightPanels) panel.render(dummy, tickDelta);
        ImGui.endGroup();
        rightHeight = ImGui.getCursorPosY();

        float scrollMaxY = ImGui.getScrollMaxY();
        if (scrollMaxY > 0f) {
            if (ImGui.isWindowHovered(
                    ImGuiHoveredFlags.ChildWindows
            )) {
                if (wheelInput != 0) {
                    float tempTarget = ImGui.getScrollY() - wheelInput * 24f;
                    tempTarget = Math.max(0f, Math.min(tempTarget, ImGui.getScrollMaxY()));
                    this.targetScroll = tempTarget;
                }
            }

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

            float visibleRatio = Math.min(1f, size.y / (size.y + scrollMaxY));
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

            if (held) {
                float mouseDeltaY = ImGui.getIO().getMouseDeltaY();
                float scrollDelta = thumbTravel <= 0f ? 0f : mouseDeltaY / thumbTravel * scrollMaxY;
                targetScroll = Math.max(0f, Math.min(scrollY + scrollDelta, scrollMaxY));

                scrollY = ImGui.getScrollY();
                thumbOffset = thumbTravel * (scrollY / scrollMaxY);
                thumbMinY = trackMinY + thumbOffset;
                thumbMaxY = thumbMinY + thumbHeight;
            }

            ImGui.setCursorPos(cursorPos);

            int trackColor = 0;
            int thumbColor = ImGui.getColorU32(0, 0, 0, held ? 0.55f : hovered ? 0.42f : 0.28f);

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
//        float scrollSize = Math.max(leftHeight, rightHeight) - size.y;
//        if (scrollSize < scrollMaxY) {
//            scrollMaxY += scrollSize;
//        }
        targetScroll = MathUtils.clamp(targetScroll, 0f, scrollMaxY);
        ImGui.setScrollY((float) MathUtils.lerp(
                ImGui.getScrollY(), targetScroll,
                tickDelta
        ));
        wheelInput = 0;

        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.popStyleVar(3);
    }

    @Override
    public void onInput(InputDevice device, int code, int action) {
        if (device == InputDevice.MOUSE) {
            float wheel = InputUtils.getMouseEventWheel();
            if (wheel != 0) {
                wheelInput += wheel;
            }
        }
    }
}
