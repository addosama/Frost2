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
    private final NumberProperty<T, ? extends NumberProperty> prop;

    public NumberPropComponent(PanelClickGui gui, PropertyDescriptor descriptor, NumberProperty<T, ? extends NumberProperty> prop) {
        super(gui, descriptor);
        this.prop = prop;
    }

    @Override
    public T renderElement(boolean dummy, float tickDelta, String id, T value) {
        float valueMin = prop.getMinValue().floatValue();
        float valueMax = prop.getMaxValue().floatValue();
        float valueInterval = valueMax - valueMin;
        T valReturn = value;

        ImGui.beginGroup();

        // value
        {
            ImGui.pushFont(FontManager.INSTANCE.puHui10);
            String valText = prop.getValueAsString(valReturn);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 6);
            ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 4, 2);
            ImGui.pushStyleColor(ImGuiCol.Button, gui.getTheme().getButtonBgColor());
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, gui.getTheme().getButtonBgColor());
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, gui.getTheme().getButtonBgColor());
            ImGui.pushStyleColor(ImGuiCol.Text, gui.getTheme().getSecondaryColor());
            ImGui.button(valText + "###" + id + ".value");
            ImGui.popStyleColor(4);
            ImGui.popStyleVar(2);
            ImGui.popFont();
        }

        ImGui.sameLine(0, 6);

        // slider
        {
            ImGui.invisibleButton(id + ".slider", 100, 18);
            ImVec2 rectMin = ImGui.getItemRectMin(), rectMax = ImGui.getItemRectMax(), size = ImGui.getItemRectSize();

            boolean drag = ImGui.isMouseDragging(0) && ImGui.isItemActive();

            if (drag) {
                float relativeMouseX = ImGui.getMousePosX() - rectMin.x;
                float mousePercent = relativeMouseX / size.x;
                valReturn = prop.castValue(prop.getProcessedValue(
                        prop.castValue(BigDecimal.valueOf(valueMin + (mousePercent * valueInterval)))
                ));
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
        }

        ImGui.endGroup();

        return valReturn;
    }

    @Override
    public float getElementWidth(T value) {
        return 100 + 14 + FontManager.INSTANCE.puHui10.calcTextSizeAX(14f, Float.MAX_VALUE, 0f, prop.getValueAsString(value));
    }
}
