package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.ElementRenderer;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.utils.ImTextRenderer;

import java.util.Collection;

public abstract class SelectorElement<T, PT> extends PanelComponent implements ElementRenderer<PT> {
    public SelectorElement(PanelClickGui gui) {
        super(gui);
    }

    @Override
    public PT renderElement(boolean dummy, float tickDelta, String id, PT value) {
        boolean mouseClicked = ImGui.invisibleButton(id, 100, 20);
        ImVec2 itemMin = ImGui.getItemRectMin(), itemMax = ImGui.getItemRectMax();
        if (mouseClicked) ImGui.openPopup(id + ".popup");
        boolean popupOpen = ImGui.isPopupOpen(id + ".popup");

        if (!dummy) {
            String valueStr = providePreviewString(value);
            ImGui.getWindowDrawList().addRectFilled(
                    itemMin, itemMax,
                    gui.getTheme().getSelectorBgColor(), 6f
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
            PT newValue = value;
            ImGui.setNextWindowPos(itemMin.x, itemMax.y + 4);
            ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 12);
            ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 1f);
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

    private PT renderPopup(boolean dummy, float tickDelta, String id, PT value) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
        ImGui.beginGroup();
        ImGui.pushFont(FontManager.INSTANCE.puHui12);

        PT ret = doRenderPopup(dummy, tickDelta, id, value);

        ImGui.popFont();
        ImGui.endGroup();
        ImGui.popStyleVar();
        return ret;
    }

    private PT doRenderPopup(boolean dummy, float tickDelta, String id, PT value) {
        ImDrawList drawList = ImGui.getWindowDrawList();

        boolean anyActive = isMultiSelect() && hasAnyActive(value);
        float xOffset = anyActive? 14 : 0;

        ImVec2 hoveredButtonMin = null;
        float maxButtonWidth = 150;
        final float buttonHeight = ImGui.getTextLineHeight() + 12;

        {
            drawList.channelsSplit(2);
            drawList.channelsSetCurrent(1);
        }

        PT valueReturn = value;
        for (T mode : provideValueList(value)) {
            boolean active = isActive(mode, value);
            String buttonText = mode instanceof Named ? ((Named) mode).getName() : mode.toString();
            float buttonWidth = Math.max(ImTextRenderer.getTextWidth(buttonText) + 16 + xOffset, 150);
            maxButtonWidth = Math.max(maxButtonWidth, buttonWidth);

            if (ImGui.invisibleButton(
                    id + ".popup.buttons." + mode,
                    buttonWidth, buttonHeight
            )) valueReturn = valueClicked(mode, value);
            ImVec2 rectMin = ImGui.getItemRectMin();
            if (ImGui.isItemHovered()) hoveredButtonMin = rectMin;

            if (!dummy) {
                if (active && isMultiSelect()) {
                    drawList.addCircleFilled(
                            rectMin.plus(
                                    8 + 4,
                                    buttonHeight / 2
                            ),
                            4f,
                            gui.getTheme().getSecondaryColor()
                    );
                }
                ImTextRenderer.drawText(
                        drawList,
                        buttonText,
                        rectMin.x + 8 + xOffset,
                        rectMin.y + 5,
                        active? gui.getTheme().getTextHighlightColor() : gui.getTheme().getMainColor()
                );
            }
        }

        {
            drawList.channelsSetCurrent(0);
            if (!dummy) {
                if (hoveredButtonMin != null) {
                    int color = ImGui.isMouseDown(0) ? gui.getTheme().getSelectorElementActiveColor() : gui.getTheme().getSelectorElementHoverColor();
                    drawList.addRectFilled(
                            hoveredButtonMin, hoveredButtonMin.plus(maxButtonWidth, buttonHeight),
                            color, 9f
                    );
                }
            }
            drawList.channelsMerge();
        }

        return valueReturn;
    }

    protected abstract String providePreviewString(PT value);
    protected abstract Collection<T> provideValueList(PT propValue);
    protected abstract PT valueClicked(T value, PT propValue);

    protected abstract boolean isActive(T value, PT propValue);
    protected abstract boolean isMultiSelect();
    protected abstract boolean hasAnyActive(PT propValue);

    @Override public final void render(boolean dummy, float tickDelta) {}
    @Override
    public float getElementWidth(PT value) {
        return 100;
    }
}
