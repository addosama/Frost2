package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl;

import imgui.ImGui;
import imgui.ImVec2;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements.SelectorElement;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class MultipleBooleanPropComponent<T extends Enum<T>> extends PropertyComponent<Map<T, Boolean>> {
    private final MultipleBooleanProperty<T> prop;
    private final SelectorElement<T, Map<T, Boolean>> selector;

    public MultipleBooleanPropComponent(PanelClickGui gui, PropertyDescriptor descriptor, MultipleBooleanProperty<T> multipleBooleanProperty) {
        super(gui, descriptor);
        this.prop = multipleBooleanProperty;
        this.selector = new SelectorElement<T, Map<T, Boolean>>(gui) {
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
            protected Collection<T> provideValueList(Map<T, Boolean> propValue) {
                return propValue.keySet();
            }

            @Override
            protected Map<T, Boolean> valueClicked(T value, Map<T, Boolean> propValue) {
                propValue.put(value, !propValue.getOrDefault(value, false));
                return propValue;
            }

            @Override
            protected boolean isActive(T value, Map<T, Boolean> propValue) {
                return propValue.getOrDefault(value, false);
            }

            @Override
            protected boolean isMultiSelect() {
                return true;
            }

            @Override
            protected boolean hasAnyActive(Map<T, Boolean> propValue) {
                return propValue.containsValue(Boolean.TRUE);
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
