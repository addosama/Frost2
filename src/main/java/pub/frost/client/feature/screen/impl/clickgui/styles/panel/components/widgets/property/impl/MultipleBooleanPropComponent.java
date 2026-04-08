package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements.SelectorElement;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;
import pub.frost.utils.ImTextRenderer;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class MultipleBooleanPropComponent<T extends Enum<T>> extends PropertyComponent<Map<T, Boolean>> {
    private final MultipleBooleanProperty<T> prop;
    private final SelectorElement<Map<T, Boolean>> selector;

    public MultipleBooleanPropComponent(PanelClickGui gui, PropertyDescriptor descriptor, MultipleBooleanProperty<T> multipleBooleanProperty) {
        super(gui, descriptor);
        this.prop = multipleBooleanProperty;
        this.selector = new SelectorElement<Map<T, Boolean>>(gui) {
            @Override
            protected String providePreviewString(Map<T, Boolean> value) {
                StringBuilder builder = new StringBuilder();
                boolean first = true;
                for (Map.Entry<T, Boolean> entry : value.entrySet().stream().filter(Map.Entry::getValue).collect(Collectors.toList())) {
                    if (!first) builder.append(", ");
                    first = false;
                    T key = entry.getKey();
                    builder.append(key instanceof Named? ((Named) key).getName() : key.toString());
                }
                String str = builder.toString();
                return str.isEmpty()? FrostCore.getLocalizer().get("strings.none") : str;
            }

            @Override
            protected Map<T, Boolean> renderPopup(boolean dummy, float tickDelta, String id, Map<T, Boolean> value) {
                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
                ImGui.beginGroup();
                ImGui.pushFont(FontManager.INSTANCE.puHui12);
                ImVec2 hoveredButtonMin = null;
                float maxButtonWidth = 150;
                final float buttonHeight = ImGui.getTextLineHeight() + 12;
                ImDrawList drawList = ImGui.getWindowDrawList();
                drawList.channelsSplit(2);
                drawList.channelsSetCurrent(1);
                boolean anyActive = value.containsValue(Boolean.TRUE);
                float xOffset = anyActive? 14 : 0;
                for (T mode : value.keySet()) {
                    boolean active = value.get(mode);
                    String buttonText = mode instanceof Named? ((Named) mode).getName() : mode.toString();
                    float buttonWidth = Math.max(ImTextRenderer.getTextWidth(buttonText) + 16 + xOffset, 150);
                    maxButtonWidth = Math.max(maxButtonWidth, buttonWidth);

                    if (ImGui.invisibleButton(
                            id + ".popup.buttons." + mode,
                            buttonWidth, buttonHeight
                    )) value.put(mode, !active);
                    ImVec2 rectMin = ImGui.getItemRectMin();
                    if (ImGui.isItemHovered()) hoveredButtonMin = rectMin;

                    if (!dummy) {
                        if (active) {
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

                return value;
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
    public Map<T, Boolean> renderElement(boolean dummy, float tickDelta, String id, Map<T, Boolean> value) {
        return selector.renderElement(dummy, tickDelta, id, value);
    }
}
