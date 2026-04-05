package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components;

import lombok.RequiredArgsConstructor;
import pub.frost.client.feature.screen.components.RenderableComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;

@RequiredArgsConstructor
public abstract class PanelComponent implements RenderableComponent {
    protected final PanelClickGui gui;
}
