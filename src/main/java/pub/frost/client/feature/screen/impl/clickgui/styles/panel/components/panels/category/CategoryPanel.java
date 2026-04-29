package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.category;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiStyleVar;
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

        ImGui.setCursorPosY(ImGui.getCursorPosY() + ImGui.getContentRegionAvailY() - 45);
        splitLine(dummy, tickDelta);
        renderUserInfo(dummy, tickDelta);

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
                        0xFFFFEAE5,
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

    private void renderUserInfo(boolean dummy, float tickDelta) {
        ImGui.invisibleButton(this + ".userinfo", ImGui.getContentRegionAvailX(), 36);
        if (!dummy) {
            ImVec2 minVec = ImGui.getItemRectMin(), maxVec = ImGui.getItemRectMax();
            ImDrawList draws = ImGui.getWindowDrawList();

            // draw user avatar
            {
                ImVec2 avatarMaxVec = minVec.plus(36, 36);
                draws.addRectFilled(
                        minVec, avatarMaxVec,
                        0xFFBF9060, 12f
                );
                ImGui.pushFont(FontManager.INSTANCE.puHui12);
                draws.addText(
                        ImTextRenderer.centerText(
                                "OvO",
                                minVec.x + 18,
                                minVec.y + 18,
                                true, true
                        ),
                        0xFFFFFFFF,
                        "OvO"
                );
                ImGui.popFont();
            }
            // draw user info
            {
                ImVec2 centerVec = minVec.plus(46, 18);

                // username
                ImGui.pushFont(FontManager.INSTANCE.puHui12);
                draws.addText(
                        centerVec.minus(0, ImGui.getFont().getAscent() + 1),
                        0xFF331A15,
                        "Beta User"
                );
                ImGui.popFont();

                // license info
                ImGui.pushFont(FontManager.INSTANCE.puHui10);
                draws.addText(
                        centerVec.plus(0, 1),
                        0xFF665552,
                        "Nightly Beta License"
                );
                ImGui.popFont();
            }
        }
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
