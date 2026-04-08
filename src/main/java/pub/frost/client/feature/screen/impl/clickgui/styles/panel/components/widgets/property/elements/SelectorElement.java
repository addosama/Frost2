package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.ElementRenderer;
import pub.frost.utils.ImTextRenderer;

public abstract class SelectorElement<T> extends PanelComponent implements ElementRenderer<T> {
    public SelectorElement(PanelClickGui gui) {
        super(gui);
    }

    @Override
    public T renderElement(boolean dummy, float tickDelta, String id, T value) {
        boolean mouseClicked = ImGui.invisibleButton(id, 100, 20);
        ImVec2 itemMin = ImGui.getItemRectMin(), itemMax = ImGui.getItemRectMax();
        if (mouseClicked) ImGui.openPopup(id + ".popup");
        boolean popupOpen = ImGui.isPopupOpen(id + ".popup");

        if (!dummy) {
            String valueStr = providePreviewString(value);
            ImGui.getWindowDrawList().addRectFilled(
                    itemMin, itemMax,
                    0xFFE5E5E5, 6f
            );

            // arrow
            ImGui.pushFont(FontManager.INSTANCE.puHui12);
            String arrow = popupOpen ? "-" : "+";
            float arrowWidth = ImGui.calcTextSizeX(arrow);
            ImTextRenderer.drawText(
                    ImGui.getWindowDrawList(),
                    arrow,
                    itemMax.x - 6 - arrowWidth,
                    itemMin.y + (20 - ImGui.getTextLineHeight()) / 2f,
                    gui.getTheme().getMainColor()
            );
            ImGui.popFont();

            // value
            {
                ImGui.pushClipRect(
                        itemMin.plus(6, 0),
                        itemMax.minus(12 + arrowWidth, 0),
                        true
                );
                ImGui.pushFont(FontManager.INSTANCE.puHui12);
                ImTextRenderer.drawText(
                        ImGui.getWindowDrawList(),
                        valueStr,
                        itemMin.x + 6,
                        itemMin.y + (20 - ImGui.getTextLineHeight()) / 2f,
                        gui.getTheme().getMainColor()
                );
                ImGui.popFont();
                ImGui.popClipRect();
            }
        }

        if (popupOpen) {
            T newValue = value;
            ImGui.setNextWindowPos(itemMin.x, itemMax.y + 4);
            ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 12);
            ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 1.5f);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 4);
            ImGui.pushStyleColor(ImGuiCol.PopupBg, gui.getTheme().getModulePanelBgColor());
            ImGui.pushStyleColor(ImGuiCol.Border, gui.getTheme().getWindowBorderColor());

            if (ImGui.beginPopup(id + ".popup")) {
                newValue = renderPopup(dummy, tickDelta, id, value);
                ImGui.endPopup();
            }

            ImGui.popStyleColor(2);
            ImGui.popStyleVar(3);

            return newValue;
        }

        return value;
    }

    protected abstract String providePreviewString(T value);
    protected abstract T renderPopup(boolean dummy, float tickDelta, String id, T value);

    @Override public final void render(boolean dummy, float tickDelta) {}
}
