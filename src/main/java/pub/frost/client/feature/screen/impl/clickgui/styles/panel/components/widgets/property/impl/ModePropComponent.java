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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ModePropComponent<T extends Enum<T>> extends PropertyComponent<T> {
    private final ModeProperty<T> prop;
    private final SelectorElement<T, T> selector;
    public ModePropComponent(PanelClickGui gui, PropertyDescriptor descriptor, ModeProperty<T> modeProperty) {
        super(gui, descriptor);
        this.prop = modeProperty;
        this.selector = new SelectorElement<T, T>(gui) {
            @Override
            protected String providePreviewString(T value) {
                return value instanceof Named ? ((Named) value).getName() : value.toString();
            }

            @Override
            protected Collection<T> provideValueList(T propValue) {
                return Arrays.asList(propValue.getDeclaringClass().getEnumConstants());
            }

            @Override
            protected T valueClicked(T value, T propValue) {
                return value;
            }

            @Override
            protected boolean isActive(T value, T propValue) {
                return prop.is(value);
            }

            @Override
            protected boolean isMultiSelect() {
                return false;
            }

            @Override
            protected boolean hasAnyActive(T propValue) {
                return true;
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
