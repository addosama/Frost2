package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements;

import imgui.ImGui;
import imgui.ImVec2;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.ElementRenderer;

public class SwitchElement extends PanelComponent implements ElementRenderer<Boolean> {
    public SwitchElement(PanelClickGui gui) {
        super(gui);
    }

    @Override
    public Boolean renderElement(boolean dummy,float tickDelta, String id, Boolean value) {
        boolean clicked = ImGui.invisibleButton(id, 30, 18);
        if (!dummy) {
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getItemRectMin(), ImGui.getItemRectMax(),
                    value ? gui.getTheme().getSwitchEnabledBgColor() : gui.getTheme().getSwitchDisabledBgColor(),
                    9f
            );
            ImVec2 indicator = ImGui.getItemRectMin().plus(1, 1);
            if (value) indicator = indicator.plus(12, 0);
            ImGui.getWindowDrawList().addRectFilled(
                    indicator, indicator.plus(16, 16),
                    gui.getTheme().getSwitchIndicatorColor(), 8f
            );
        }
        return clicked != value;
    }

    @Override
    public float getElementWidth(Boolean value) {
        return 30;
    }

    @Override @Deprecated
    public void render(boolean dummy, float tickDelta) {}
}
