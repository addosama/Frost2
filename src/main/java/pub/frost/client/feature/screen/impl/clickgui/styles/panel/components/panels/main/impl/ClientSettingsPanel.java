package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.impl;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.MainPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.child.PropertyPanel;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.descriptor.PropertyDescriptorFactory;

import java.util.ArrayList;
import java.util.List;

public class ClientSettingsPanel extends MainPanel {
    private final List<PropertyPanel> panels = new ArrayList<>();
    private float panelWidth = 0;

    public ClientSettingsPanel(PanelClickGui gui) {
        super(gui);
        for (PropertyDescriptor group : PropertyDescriptorFactory.createForObject(FrostCore.getInstance().getClientSettings()).build().getDescriptorMap().values()) {
            panels.add(new PropertyPanel(
                    gui,
                    group::getName,
                    group.getChildProperties(),
                    () -> panelWidth
            ));
        }
    }

    @Override
    protected void renderPanelContent(boolean dummy, float tickDelta) {
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
            panelWidth = size.x;

            ImGui.beginGroup();
            for (PropertyPanel panel : panels) panel.render(dummy, tickDelta);
            ImGui.endGroup();
        }
        ImGui.endChild();

        ImGui.popStyleVar(2);
    }
}
