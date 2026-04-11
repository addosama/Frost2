package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main;

import pub.frost.client.feature.screen.components.InputListener;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;

public abstract class MainPanel extends PanelComponent implements InputListener {
    public MainPanel(PanelClickGui gui) {
        super(gui);
    }
}
