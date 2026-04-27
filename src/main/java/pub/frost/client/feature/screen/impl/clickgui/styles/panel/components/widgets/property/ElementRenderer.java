package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property;

public interface ElementRenderer<T> {
    T renderElement(boolean dummy, float tickDelta, String id, T value);
    float getElementWidth(T value);
}
