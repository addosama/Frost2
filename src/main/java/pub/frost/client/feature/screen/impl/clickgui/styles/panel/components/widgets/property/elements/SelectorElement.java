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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SelectorElement<ELEMENT, VAL> extends PanelComponent implements ElementRenderer<VAL> {
    private final Function<VAL, String> valueStringProvider;
    private final Supplier<Collection<ELEMENT>> valueListProvider;
    
    private final BiFunction<ELEMENT, VAL, VAL> valueClickAcceptor;
    
    private final BiFunction<ELEMENT, VAL, Boolean> valueStateProvider;
    private final Function<VAL, Boolean> activeStateProvider;
    
    private final boolean allowMultiSelect;
    
    public SelectorElement(
            PanelClickGui gui,
            Function<VAL, String> valueStringProvider, Supplier<Collection<ELEMENT>> valueListProvider,
            BiFunction<ELEMENT, VAL, VAL> valueClickAcceptor,
            BiFunction<ELEMENT, VAL, Boolean> valueStateProvider, Function<VAL, Boolean> activeStateProvider,
            boolean allowMultiSelect
    ) {
        super(gui);
        this.valueStringProvider = valueStringProvider;
        this.valueListProvider = valueListProvider;
        this.valueClickAcceptor = valueClickAcceptor;
        this.valueStateProvider = valueStateProvider;
        this.activeStateProvider = activeStateProvider;
        this.allowMultiSelect = allowMultiSelect;
    }

    @Override
    public VAL renderElement(boolean dummy, float tickDelta, String id, VAL value) {
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
            VAL newValue = value;
            ImGui.setNextWindowPos(itemMin.x, itemMax.y + 4);
            ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 12);
            ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 1f);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 4);
            ImGui.pushStyleColor(ImGuiCol.PopupBg, gui.getTheme().getModulePanelBgColor());
            ImGui.pushStyleColor(ImGuiCol.Border, gui.getTheme().getWindowBorderColor());

            if (ImGui.beginPopup(id + ".popup")) {
                newValue = renderPopupContent(dummy, tickDelta, id, value);
                ImGui.endPopup();
            }

            ImGui.popStyleColor(2);
            ImGui.popStyleVar(3);

            return newValue;
        }

        return value;
    }

    private VAL renderPopupContent(boolean dummy, float tickDelta, String id, VAL value) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
        ImGui.beginGroup();
        ImGui.pushFont(FontManager.INSTANCE.puHui12);

        VAL ret = renderPopupChildren(dummy, tickDelta, id, value);

        ImGui.popFont();
        ImGui.endGroup();
        ImGui.popStyleVar();
        return ret;
    }

    private VAL renderPopupChildren(boolean dummy, float tickDelta, String id, VAL value) {
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

        VAL valueReturn = value;
        for (ELEMENT mode : provideValueList(value)) {
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

    @Deprecated
    protected String providePreviewString(VAL value) {
        return valueStringProvider.apply(value);
    }
    @Deprecated
    protected Collection<ELEMENT> provideValueList(VAL propValue) {
        return valueListProvider.get();
    }
    @Deprecated
    protected VAL valueClicked(ELEMENT value, VAL propValue) {
        return valueClickAcceptor.apply(value, propValue);
    }

    @Deprecated
    protected boolean isActive(ELEMENT value, VAL propValue) {
        return valueStateProvider.apply(value, propValue);
    }
    @Deprecated
    protected boolean isMultiSelect() {
        return allowMultiSelect;
    }
    @Deprecated
    protected boolean hasAnyActive(VAL propValue) {
        return activeStateProvider.apply(propValue);
    }

    @Override @Deprecated
    public void render(boolean dummy, float tickDelta) {}

    @Override
    public float getElementWidth(VAL value) {
        return 100;
    }
    @Override
    public float getElementHeight() {
        return 20;
    }
}
