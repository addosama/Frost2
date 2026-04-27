package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl;

import imgui.ImGui;
import imgui.ImVec2;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.bool.BooleanProperty;

public class BooleanPropComponent extends PropertyComponent<Boolean> {
    private final BooleanProperty prop;
    public BooleanPropComponent(PanelClickGui gui, PropertyDescriptor descriptor, BooleanProperty prop) {
        super(gui, descriptor);
        this.prop = prop;
    }

    @Override
    protected void renderWidgets(boolean dummy, float tickDelta) {
        ImVec2 cursor = ImGui.getCursorPos();
        ImGui.setCursorPosX(cursor.x + ImGui.getContentRegionAvailX() - 30);
        ImGui.setCursorPosY(cursor.y + 6);
        prop.set(renderElement(dummy, tickDelta, this + ".switch", prop.getValue()));
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
}
