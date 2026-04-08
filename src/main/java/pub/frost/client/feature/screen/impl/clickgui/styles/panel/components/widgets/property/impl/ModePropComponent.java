package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements.SelectorElement;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.utils.ImTextRenderer;

public class ModePropComponent<T extends Enum<T>> extends PropertyComponent<T> {
    private final ModeProperty<T> prop;
    private final SelectorElement<T> selector;
    public ModePropComponent(PanelClickGui gui, PropertyDescriptor descriptor, ModeProperty<T> modeProperty) {
        super(gui, descriptor);
        this.prop = modeProperty;
        this.selector = new SelectorElement<T>(gui) {
            @Override
            protected String providePreviewString(T value) {
                return value instanceof Named ? ((Named) value).getName() : value.toString();
            }

            @Override
            protected T renderPopup(boolean dummy, float tickDelta, String id, T value) {
                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
                ImGui.beginGroup();
                T newValue = value;
                ImGui.pushFont(FontManager.INSTANCE.puHui12);
                ImVec2 hoveredButtonMin = null;
                float maxButtonWidth = 150;
                final float buttonHeight = ImGui.getTextLineHeight() + 12;
                ImDrawList drawList = ImGui.getWindowDrawList();
                drawList.channelsSplit(2);
                drawList.channelsSetCurrent(1);
                for (T mode : value.getDeclaringClass().getEnumConstants()) {
                    String buttonText = providePreviewString(mode);
                    float buttonWidth = Math.max(ImTextRenderer.getTextWidth(buttonText) + 16, 150);
                    maxButtonWidth = Math.max(maxButtonWidth, buttonWidth);

                    if (ImGui.invisibleButton(
                            id + ".popup.buttons." + mode,
                            buttonWidth, buttonHeight
                    )) newValue = mode;
                    ImVec2 rectMin = ImGui.getItemRectMin();
                    if (ImGui.isItemHovered()) hoveredButtonMin = rectMin;

                    if (!dummy) {
                        ImTextRenderer.drawText(
                                drawList,
                                buttonText,
                                rectMin.x + 8,
                                rectMin.y + 6,
                                mode == value? gui.getTheme().getTextHighlightColor() : gui.getTheme().getMainColor()
                        );
                    }
                }

                drawList.channelsSetCurrent(0);
                if (!dummy) {
                    if (hoveredButtonMin != null) {
                        int color = ImGui.isMouseDown(0)? 0x50999999 : 0x50CCCCCC;
                        drawList.addRectFilled(
                                hoveredButtonMin, hoveredButtonMin.plus(maxButtonWidth, buttonHeight),
                                color, 9f
                        );
                    }
                }
                drawList.channelsMerge();

                ImGui.popFont();
                ImGui.endGroup();
                ImGui.popStyleVar();
                return newValue;
            }
        };
    }

    @Override
    protected void renderWidgets(boolean dummy, float tickDelta) {
        ImVec2 cursor = ImGui.getCursorPos();
        ImGui.setCursorPosX(cursor.x + ImGui.getContentRegionAvailX() - 100);
        ImGui.setCursorPosY(cursor.y + 5);
        prop.set(renderElement(dummy, tickDelta, selector.toString(), prop.getValue()));
    }

    @Override
    public T renderElement(boolean dummy, float tickDelta, String id, T value) {
        return selector.renderElement(dummy, tickDelta, id, value);
    }
}
