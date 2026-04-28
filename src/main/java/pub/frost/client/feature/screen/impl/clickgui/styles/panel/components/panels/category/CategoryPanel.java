package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.category;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiStyleVar;
import lombok.Getter;
import lombok.Setter;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category.CategoryButton;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category.CategoryButtonGroup;
import pub.frost.utils.ImTextRenderer;

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

        renderClientInfo(dummy, tickDelta);

        splitLine(dummy, tickDelta);

        for (CategoryButtonGroup buttonGroup : buttonGroupList) {
            buttonGroup.render(dummy, tickDelta);
        }

        ImGui.endChild();
        ImGui.popStyleVar(3);
    }

    private void renderClientInfo(boolean dummy, float tickDelta) {
        ImGui.beginGroup();

        // icon
        {
            ImGui.dummy(36, 36);
            if (!dummy) {
                ImVec2 min = ImGui.getItemRectMin(), max = ImGui.getItemRectMax();
                ImGui.getWindowDrawList().addRectFilled(
                        min, max,
                        0xFF663329, 12f
                );
                ImGui.pushFont(FontManager.INSTANCE.icon16);
                ImGui.getWindowDrawList().addText(
                        min.plus(10, 10),
                        0xFFFFFFFF,
                        "\ue601"
                );
                ImGui.popFont();
            }
        }

        ImGui.sameLine(0, 8);

        // text
        {
            float centerY = ImGui.getCursorPosY() + 18;

            ImGui.beginGroup();

            // client name
            ImGui.pushFont(FontManager.INSTANCE.puhui14);
            ImGui.setCursorPosY(centerY - 1 - ImGui.getFont().getAscent());
            ImGui.textColored(0xFF331A15, "Frost");
            ImGui.popFont();

            // client version
            ImGui.pushFont(FontManager.INSTANCE.puHui10);
            ImGui.setCursorPosY(centerY + 1);
            ImGui.textColored(0xFF665552, FrostCore.getVersionString());
            ImGui.popFont();

            ImGui.endGroup();
        }

        ImGui.endGroup();
    }

    private void splitLine(boolean dummy, float tickDelta) {
        ImGui.dummy(ImGui.getContentRegionAvailX(), 1);
        if (!dummy) {
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getItemRectMin(), ImGui.getItemRectMax(),
                    gui.getTheme().getSplitColor()
            );
        }
    }

    public void addGroup(CategoryButtonGroup buttonGroup) {
        buttonGroupList.add(buttonGroup);
    }

    public CategoryButton getActiveButton() {
        checkActiveButton();
        return activeButton;
    }
}
