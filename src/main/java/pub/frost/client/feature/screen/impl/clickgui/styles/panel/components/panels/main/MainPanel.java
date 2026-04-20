package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main;

import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import pub.frost.client.feature.screen.components.InputListener;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;

public abstract class MainPanel extends PanelComponent implements InputListener {
    public MainPanel(PanelClickGui gui) {
        super(gui);
    }

    @Override
    public final void render(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8, 8);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 8, 8);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, gui.getTheme().getMainPanelBgColor());
        ImGui.beginChild(
                this.toString(),
                0f, 0f,
                ImGuiChildFlags.AlwaysUseWindowPadding,
                ImGuiWindowFlags.NoScrollbar
        );

        renderPanelContent(dummy, tickDelta);

        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.popStyleVar(3);
    }

    protected abstract void renderPanelContent(boolean dummy, float tickDelta);
}
