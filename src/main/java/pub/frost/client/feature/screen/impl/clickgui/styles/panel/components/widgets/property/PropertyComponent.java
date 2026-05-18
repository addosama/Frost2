package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements.ColorSelectorElement;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements.SelectorElement;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements.SliderElement;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements.SwitchElement;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.override.OverridePopupComponent;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;
import pub.frost.client.property.impl.color.ColorProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.NumberProperty;
import pub.frost.utils.ImTextRenderer;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class PropertyComponent<T> extends PanelComponent {
    protected final PropertyDescriptor descriptor;
    protected final AbstractProperty<T, ?> prop;
    protected final List<PropertyComponent<?>> children;
    protected final OverridePopupComponent<T> overridePopup;
    protected final ElementRenderer<T> elementRenderer;

    public PropertyComponent(PanelClickGui gui, PropertyDescriptor descriptor, ElementRenderer<T> elementRenderer) {
        super(gui);
        this.descriptor = descriptor;

        prop = descriptor.getProperty();
        if (prop != null && prop.isOverridingEnabled()) {
            overridePopup = new OverridePopupComponent<>(
                    gui,
                    prop,
                    elementRenderer
            );
        } else overridePopup = null;

        if (descriptor.isGroup()) {
            List<PropertyComponent<?>> childList;
            childList = descriptor.getChildProperties().stream().collect(
                    ArrayList::new,
                    (list, child) -> {
                        AbstractProperty<?, ?> childProp = child.getProperty();
                        if (childProp != null && childProp == prop) return;
                        list.add(buildForDescriptor(gui, child));
                    },
                    ArrayList::addAll
            );
            if (childList.isEmpty()) children = null;
            else children = childList;
        } else children = null;

        if (elementRenderer == null) {
            elementRenderer = new ElementRenderer<T>() {
                @Override
                public T renderElement(boolean dummy, float tickDelta, String id, T value) {
                    return value;
                }

                @Override
                public float getElementWidth(T value) {
                    return 0;
                }
            };
        }
        this.elementRenderer = elementRenderer;
    }

    @Override
    public final void render(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 0);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0);
        ImGui.beginChild(
                this.toString(),
                0f, 30f,
                0
        );

        renderText(dummy, tickDelta);

        float xOffset = ImGui.getContentRegionAvailX();
        if (prop != null) {
            T val = prop.getValue();
            xOffset -= elementRenderer.getElementWidth(val);
            ImGui.sameLine(xOffset);
            renderWidgets(dummy, tickDelta, val);
            xOffset -= 6;
        }
        if (overridePopup != null) {
            xOffset -= 14;
            ImGui.sameLine(xOffset);
            renderOverride(dummy, tickDelta);
            xOffset -= 2;
        }
        if (children != null) {
            for (PropertyComponent<?> child : children) {
                if (child.isVisible()) {
                    xOffset -= 20;
                    ImGui.sameLine(xOffset);
                    renderGroupPopupButton(dummy, tickDelta);
                    break;
                }
            }
        }

        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }

    protected void renderText(boolean dummy, float tickDelta) {
        ImGui.pushFont(FontManager.INSTANCE.puHui12);
        float lineHeight = ImGui.getTextLineHeight();
        float y = ImGui.getCursorPosY();
        ImGui.setCursorPosY(y + (30 - lineHeight) / 2);
        ImTextRenderer.drawText(
                ImGui.getWindowDrawList(),
                descriptor.getName(),
                ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(),
                gui.getTheme().getMainColor()
        );
        ImGui.textColored(0, descriptor.getName());
        ImGui.setCursorPosY(y);
        ImGui.popFont();
    }
    protected void renderWidgets(boolean dummy, float tickDelta, T val) {
        ImGui.setCursorPosY((ImGui.getCursorPosY() + ImGui.getContentRegionAvailY() - elementRenderer.getElementHeight()) / 2);
        prop.set(elementRenderer.renderElement(dummy, tickDelta, this + ".element", val));
    }
    protected void renderOverride(boolean dummy, float tickDelta) {
        boolean overrideActive = descriptor.getProperty().isOverrideActive();
        if (renderOverrideButton(dummy, overrideActive)) ImGui.openPopup(overridePopup.toString());
        overridePopup.render(dummy, tickDelta);
    }

    protected boolean renderOverrideButton(boolean dummy, boolean highlight) {
        float y = ImGui.getCursorPosY();
        ImGui.setCursorPosY((y + ImGui.getContentRegionAvailY() - 14) / 2);
        boolean clicked = ImGui.invisibleButton(this + ".override", 14, 14);
        if (!dummy) {
            ImGui.pushFont(FontManager.INSTANCE.icon14);
            String icon = "\uedaf";
            ImVec2 iconSize = ImGui.calcTextSize(icon);
            ImVec2 iconPos = ImGui.getItemRectMin().plus(
                    (14 - iconSize.x) / 2,
                    (14 - iconSize.y) / 2
            );
            ImTextRenderer.drawText(
                    ImGui.getWindowDrawList(),
                    icon,
                    iconPos.x, iconPos.y - 0.5f,
                    highlight? gui.getTheme().getIconHighlightColor() : gui.getTheme().getMainColor()
            );
            ImGui.popFont();
        }
        ImGui.setCursorPosY(y);
        return clicked;
    }
    protected void renderGroupPopupButton(boolean dummy, float tickDelta) {
        float y = ImGui.getCursorPosY();
        ImGui.setCursorPosY((y + ImGui.getContentRegionAvailY() - 20) / 2);
        if (ImGui.invisibleButton(this + ".button", 20, 20)) {
            ImGui.setNextWindowPos(ImGui.getWindowPos().plus(-12, ImGui.getWindowHeight() + 4));
            ImGui.openPopup(this + ".popup");
        }
        boolean popup = ImGui.isPopupOpen(this + ".popup");
        if (!dummy) {
            ImGui.pushFont(FontManager.INSTANCE.icon14);
            ImVec2 buttonPos = ImGui.getItemRectMin();
            ImGui.getWindowDrawList().addText(
                    buttonPos.plus(2, 3),
                    popup? gui.getTheme().getIconHighlightColor() : gui.getTheme().getMainColor(), "\ue678"
            );
            ImGui.popFont();
        }
        if (popup) {
            renderGroupPopup(dummy, tickDelta);
        }
        ImGui.setCursorPosY(y);
    }

    protected void renderGroupPopup(boolean dummy, float tickDelta) {
        float width = ImGui.getWindowWidth();

        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 1f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12, 4);
        ImGui.pushStyleColor(ImGuiCol.PopupBg, gui.getTheme().getModulePanelBgColor());
        ImGui.pushStyleColor(ImGuiCol.Border, gui.getTheme().getWindowBorderColor());

        if (ImGui.beginPopup(this + ".popup")) {
            ImGui.beginChild(
                    this + ".popup.panel",
                    width, 0f,
                    ImGuiChildFlags.AutoResizeY
            );
            for (PanelComponent component : children) {
                if (component.isVisible()) component.render(dummy, tickDelta);
            }
            ImGui.endChild();
            ImGui.endPopup();
        }

        ImGui.popStyleColor(2);
        ImGui.popStyleVar(3);
    }

    @Override
    public boolean isVisible() {
        return descriptor.isVisible();
    }

    private static <NUM extends Number & Comparable<NUM>, MODE extends Enum<MODE>> PropertyComponent<?> getPropertyComponent(PanelClickGui gui, PropertyDescriptor descriptor) {
        final AbstractProperty<?, ?> abstractProp = descriptor.getProperty();
        ElementRenderer<?> er = null;

        if (abstractProp != null) {
            if (abstractProp instanceof BooleanProperty) {
                er = new SwitchElement(gui);
            }
            else if (abstractProp instanceof NumberProperty) {
                NumberProperty<NUM, ?> numProp = (NumberProperty<NUM, ?>) abstractProp;
                er = new SliderElement<>(
                        gui,
                        () -> numProp.getMinValue().floatValue(),
                        () -> numProp.getMaxValue().floatValue(),
                        numProp::getValueAsString,
                        numProp::castValue,
                        numProp::getProcessedValue
                );
            }
            else if (abstractProp instanceof ModeProperty) {
                ModeProperty<MODE> modeProp = (ModeProperty<MODE>) abstractProp;
                er = new SelectorElement<MODE, MODE>(
                        gui,
                        value -> value instanceof Named ? ((Named) value).getName() : value.toString(),
                        () -> Arrays.asList(modeProp.getTypeClass().getEnumConstants()),
                        (element, value) -> element,
                        (element, value) -> modeProp.is(element),
                        value -> true,
                        false
                );
            }
            else if (abstractProp instanceof MultipleBooleanProperty) {
                MultipleBooleanProperty<MODE> multipleProp = (MultipleBooleanProperty<MODE>) abstractProp;
                er = new SelectorElement<MODE, Map<MODE, Boolean>>(
                        gui,
                        value -> {
                            StringBuilder builder = new StringBuilder();
                            boolean first = true;
                            for (Map.Entry<MODE, Boolean> entry : value.entrySet().stream().filter(Map.Entry::getValue).collect(Collectors.toList())) {
                                if (!first) builder.append(", ");
                                first = false;
                                MODE key = entry.getKey();
                                builder.append(key instanceof Named? ((Named) key).getName() : key.toString());
                            }
                            String str = builder.toString();
                            return str.isEmpty()? FrostCore.getLocalizer().get("strings.none") : str;
                        },
                        () -> multipleProp.get().keySet(),
                        (element, value) -> {
                            value.put(element, !value.getOrDefault(element, false));
                            return value;
                        },
                        (element, value) -> value.getOrDefault(element, false),
                        value -> value.containsValue(Boolean.TRUE),
                        true
                );
            }
            else if (abstractProp instanceof ColorProperty) {
                ColorProperty colorProp = (ColorProperty) abstractProp;
                er = new ColorSelectorElement(gui, colorProp.isAlphaEnabled());
            }
        }

        return new PropertyComponent<>(
                gui, descriptor, er
        );
    }
    public static PropertyComponent<?> buildForDescriptor(PanelClickGui gui, PropertyDescriptor descriptor) {
        return getPropertyComponent(
                gui, descriptor
        );
    }
}
