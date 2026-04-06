package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.impl;

import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.MainPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.child.ModulePanel;

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

    @Override
    public void render(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8, 8);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 8, 8);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, gui.getTheme().getMainPanelBgColor());
        ImGui.beginChild(
                this.toString(),
                0f, 0f,
                ImGuiChildFlags.AlwaysUseWindowPadding
        );

        panelWidth = (ImGui.getContentRegionAvailX() - 8) / 2;

        ImGui.beginGroup();
        for (ModulePanel panel : leftPanels) panel.render(dummy, tickDelta);
        ImGui.endGroup();

        ImGui.sameLine();

        ImGui.beginGroup();
        for (ModulePanel panel : rightPanels) panel.render(dummy, tickDelta);
        ImGui.endGroup();

        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.popStyleVar(3);
    }
}
