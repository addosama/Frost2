package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl;

import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;

public class GroupedPropElement extends PropertyComponent<Object> {
    public GroupedPropElement(PanelClickGui gui, PropertyDescriptor descriptor) {
        super(gui, descriptor);
    }

    @Override
    protected void renderWidgets(boolean dummy, float tickDelta) {

    }

    @Override
    public Object renderElement(boolean dummy, float tickDelta, String id, Object value) {
        return null;
    }

    @Override
    public boolean isVisible() {
        return super.isVisible();
    }
}
