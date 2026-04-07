package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.number.NumberProperty;

import java.math.BigDecimal;

public class NumberPropComponent<T extends Number & Comparable<T>> extends PropertyComponent<T> {
    private final NumberProperty<T> prop;
    private boolean dragging = false;

    public NumberPropComponent(PanelClickGui gui, PropertyDescriptor descriptor, NumberProperty<T> prop) {
        super(gui, descriptor);
        this.prop = prop;
    }

    @Override
    protected void renderWidgets(boolean dummy, float tickDelta) {
        ImVec2 cursor = ImGui.getCursorPos();
        ImGui.setCursorPosX(cursor.x + ImGui.getContentRegionAvailX() - 100);
        ImGui.setCursorPosY(cursor.y + 6);
        prop.set(renderElement(dummy, tickDelta, this + ".slider", prop.getValue()));
    }

    @Override
    public T renderElement(boolean dummy, float tickDelta, String id, T value) {
        float valueMin = prop.getMinValue().floatValue();
        float valueMax = prop.getMaxValue().floatValue();
        float valueInterval = valueMax - valueMin;
        T valReturn = value;

        ImGui.invisibleButton(id, 100, 18);
        ImVec2 rectMin = ImGui.getItemRectMin(), rectMax = ImGui.getItemRectMax(), size = ImGui.getItemRectSize();
        if (ImGui.isMouseDown(0) && ImGui.isMouseHoveringRect(rectMin, rectMax)) dragging = true;
        if (ImGui.isMouseReleased(0)) dragging = false;
        if (dragging) {
            float relativeMouseX = ImGui.getMousePosX() - rectMin.x;
            float mousePercent = relativeMouseX / size.x;
            valReturn = prop.castValue(prop.getProcessedValue(
                    prop.castValue(BigDecimal.valueOf(valueMin + (mousePercent * valueInterval)))
            ));
        }

        // value
        {
            ImGui.pushFont(FontManager.INSTANCE.puHui10);
            String valText = prop.getValueAsString(valReturn);
            ImGui.setCursorScreenPos(
                    rectMin.x - 14 - ImGui.calcTextSizeX(valText),
                    rectMin.y
            );
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 6);
            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 4, 2);
            ImGui.pushStyleColor(ImGuiCol.Button, gui.getTheme().getButtonBgColor());
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, gui.getTheme().getButtonBgColor());
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, gui.getTheme().getButtonBgColor());
            ImGui.pushStyleColor(ImGuiCol.Text, gui.getTheme().getSecondaryColor());
            ImGui.button(valText + "###" + this + ".value");
            ImGui.popStyleColor(4);
            ImGui.popStyleVar(2);
            ImGui.popFont();
        }

        // draw slider
        if (!dummy) {
            ImDrawList list = ImGui.getWindowDrawList();
            ImVec2 sliderMin = rectMin.plus(0, 7);
            ImVec2 sliderMax = rectMax.minus(0, 7);
            ImVec2 sliderSize = sliderMax.minus(sliderMin);
            float valPercent = (valReturn.floatValue() - valueMin) / valueInterval;
            list.addRectFilled(
                    sliderMin, sliderMax,
                    gui.getTheme().getSliderBgColor(), 2f
            );
            if (valPercent > 0) {
                list.addRectFilled(
                        sliderMin, sliderMin.plus(sliderSize.x * valPercent, sliderSize.y),
                        gui.getTheme().getSliderHighlightBgColor(), 2f
                );
            }
            ImVec2 indicatorMin = sliderMin.plus(valPercent * (sliderSize.x - 6), -4);
            list.addRectFilled(
                    indicatorMin, indicatorMin.plus(6, 12),
                    gui.getTheme().getSliderIndicatorColor(), 3f
            );
        }

        return valReturn;
    }
}
