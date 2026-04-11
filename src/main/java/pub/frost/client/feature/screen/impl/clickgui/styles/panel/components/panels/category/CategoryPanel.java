package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.category;

import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiStyleVar;
import lombok.Getter;
import lombok.Setter;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category.CategoryButton;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category.CategoryButtonGroup;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel extends PanelComponent {
    private final List<CategoryButtonGroup> buttonGroupList;
    @Setter
    private CategoryButton activeButton;

    public CategoryPanel(PanelClickGui gui) {
        super(gui);
        this.buttonGroupList = new ArrayList<>();
    }

    private void checkActiveButton() {
        if (activeButton == null) setActiveButton(buttonGroupList.get(0).getButtonList().get(0));
    }

    @Override
    public void render(boolean dummy, float tickDelta) {
        checkActiveButton();

        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8, 8);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 8, 8);
        ImGui.beginChild(
                "CategoryPanel",
                170f, 0,
                ImGuiChildFlags.AlwaysUseWindowPadding
        );
        for (CategoryButtonGroup buttonGroup : buttonGroupList) {
            buttonGroup.render(dummy, tickDelta);
        }
        ImGui.endChild();
        ImGui.popStyleVar(3);
    }

    public void addGroup(CategoryButtonGroup buttonGroup) {
        buttonGroupList.add(buttonGroup);
    }

    public CategoryButton getActiveButton() {
        checkActiveButton();
        return activeButton;
    }
}
