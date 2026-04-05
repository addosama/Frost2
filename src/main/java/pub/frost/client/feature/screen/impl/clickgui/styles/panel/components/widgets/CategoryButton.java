package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets;

import imgui.ImGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.category.CategoryPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.MainPanel;

import java.util.function.Supplier;

public class CategoryButton extends PanelComponent {
    private final CategoryPanel catePanel;
    private final MainPanel boundPanel;
    private final String icon;
    private final Supplier<String> textSupplier;

    public CategoryButton(PanelClickGui gui, CategoryPanel catePanel, MainPanel boundPanel, String icon, Supplier<String> textSupplier) {
        super(gui);
        this.catePanel = catePanel;
        this.boundPanel = boundPanel;
        this.icon = icon;
        this.textSupplier = textSupplier;
    }

    @Override
    public void render(boolean dummy, float tickDelta) {
        ImGui.button(icon + " " + textSupplier.get(), 0, 32);
    }
}
