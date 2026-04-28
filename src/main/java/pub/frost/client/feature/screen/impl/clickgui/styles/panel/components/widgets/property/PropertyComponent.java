package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl.*;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.override.OverridePopupComponent;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.NumberProperty;
import pub.frost.utils.ImTextRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PropertyComponent<T> extends PanelComponent implements ElementRenderer<T> {
    protected final PropertyDescriptor descriptor;
    protected final OverridePopupComponent<T> overridePopup;
    protected final List<PropertyComponent<?>> children;

    public PropertyComponent(PanelClickGui gui, PropertyDescriptor descriptor) {
        super(gui);
        this.descriptor = descriptor;

        AbstractProperty prop = descriptor.getProperty();
        if (prop != null && prop.isOverridingEnabled()) {
            overridePopup = new OverridePopupComponent<>(
                    gui,
                    prop,
                    this
            );
        } else overridePopup = null;

        if (descriptor.isGroup()) {
            children = descriptor.getChildProperties().stream().collect(
                    ArrayList::new,
                    (list, child) -> {
                        AbstractProperty childProp = child.getProperty();
                        if (childProp != null && childProp == prop) return;
                        list.add(buildForDescriptor(gui, child));
                    },
                    ArrayList::addAll
            );
        } else children = null;
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
        ImVec2 cursor = ImGui.getCursorPos();
        renderWidgets(dummy, tickDelta);

        if (overridePopup != null) {
            renderOverride(dummy, tickDelta, cursor);
        }

        if (descriptor.isGroup()) {
            ImGui.sameLine();
            AbstractProperty prop = descriptor.getProperty();
            float elementWidth = prop == null? 0 : getElementWidth((T) prop.getValue());
            setupGroupPopupButtonPosition(new ImVec2(ImGui.getCursorPosX(), cursor.y), elementWidth);
            renderGroupPopupButton(dummy, tickDelta);
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
    protected abstract void renderWidgets(boolean dummy, float tickDelta);
    protected void renderOverride(boolean dummy, float tickDelta, ImVec2 pos) {
        boolean overrideClicked = false;
        boolean overrideActive = descriptor.getProperty().isOverrideActive();
        overrideClicked = renderOverrideButton(dummy, setupOverrideButtonPosition(pos, getElementWidth((T) descriptor.getProperty().getValue())), overrideActive);
        if (overrideClicked) ImGui.openPopup(overridePopup.toString());
        overridePopup.render(dummy, tickDelta);
    }

    protected ImVec2 setupOverrideButtonPosition(ImVec2 cursor, float elementWidth) {
        ImGui.setCursorPosX(cursor.x + ImGui.getContentRegionAvailX() - elementWidth - 20);
        ImGui.setCursorPosY(cursor.y + 8);
        return ImGui.getCursorScreenPos();
    }
    protected boolean renderOverrideButton(boolean dummy, ImVec2 pos, boolean highlight) {
        boolean clicked = ImGui.invisibleButton(this + ".override", 14, 14);
        if (!dummy) {
            ImGui.pushFont(FontManager.INSTANCE.icon14);
            String icon = "\uedaf";
            ImVec2 iconSize = ImGui.calcTextSize(icon);
            ImVec2 iconPos = pos.plus(
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
        return clicked;
    }

    protected ImVec2 setupGroupPopupButtonPosition(ImVec2 cursor, float elementWidth) {
        ImGui.setCursorPosX(cursor.x - elementWidth - 26);
        ImGui.setCursorPosY(cursor.y + 5);
        return ImGui.getCursorScreenPos();
    }
    protected void renderGroupPopupButton(boolean dummy, float tickDelta) {
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

    private static PropertyComponent<?> getPropertyComponent(PanelClickGui gui, PropertyDescriptor descriptor) {
        AbstractProperty abstractProp = descriptor.getProperty();
        PropertyComponent<?> component = null;
        if (abstractProp != null) {
            if (abstractProp instanceof BooleanProperty) {
                component = new BooleanPropComponent(gui, descriptor, (BooleanProperty) abstractProp);
            } else if (abstractProp instanceof NumberProperty) {
                component = new NumberPropComponent(gui, descriptor, (NumberProperty) abstractProp);
            } else if (abstractProp instanceof ModeProperty) {
                component = new ModePropComponent(gui, descriptor, (ModeProperty) abstractProp);
            } else if (abstractProp instanceof MultipleBooleanProperty) {
                component = new MultipleBooleanPropComponent(gui, descriptor, (MultipleBooleanProperty) abstractProp);
            }
        } else {
            component = new PropertyComponent<Object>(gui, descriptor) {
                @Override
                protected void renderWidgets(boolean dummy, float tickDelta) {

                }

                @Override
                public Object renderElement(boolean dummy, float tickDelta, String id, Object value) {
                    return value;
                }

                @Override
                public float getElementWidth(Object value) {
                    return 0;
                }
            };
        }
        return component;
    }
    public static PropertyComponent<?> buildForDescriptor(PanelClickGui gui, PropertyDescriptor descriptor) {
        return getPropertyComponent(
                gui, descriptor
        );
    }
}
