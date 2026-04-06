package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl;

import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.BooleanProperty;

public class BooleanPropComponent extends PropertyComponent {
    private final BooleanProperty prop;
    public BooleanPropComponent(PanelClickGui gui, PropertyDescriptor descriptor, BooleanProperty prop) {
        super(gui, descriptor);
        this.prop = prop;
    }

    @Override
    protected void renderWidgets(boolean dummy, float tickDelta) {
        float baseWidth = 30;
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 6);
        ImGui.setCursorPosX(ImGui.getCursorPosX() + ImGui.getContentRegionAvailX() - baseWidth);
        ImVec2 cursor = ImGui.getCursorScreenPos();
        boolean clicked = ImGui.invisibleButton(this + ".switch", 30, 18);
        boolean enabled = prop.getValue();
        if (!dummy) {
            ImGui.getWindowDrawList().addRectFilled(
                    cursor, cursor.plus(30, 18),
                    enabled? gui.getTheme().getSwitchEnabledBgColor() : gui.getTheme().getSwitchDisabledBgColor(),
                    9f
            );
            ImVec2 offset = new ImVec2(1, 1);
            if (enabled) offset = offset.plus(12, 0);
            ImGui.getWindowDrawList().addRectFilled(
                    cursor.plus(offset), cursor.plus(offset).plus(16, 16),
                    gui.getTheme().getSwitchIndicatorColor(), 8f
            );
        }
        if (clicked) {
            prop.set(!enabled);
        }
    }
}
