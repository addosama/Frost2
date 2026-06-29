package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import lombok.Getter;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.category.CategoryPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.MainPanel;
import pub.frost.utils.ImTextRenderer;

import java.util.function.Supplier;

public class CategoryButton extends PanelComponent {
    private final CategoryPanel catePanel;
    @Getter
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
        boolean highlight = catePanel.getActiveButton() == this;
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.channelsSplit(2);
        drawList.channelsSetCurrent(1);
        ImFont iconFont = FontManager.INSTANCE.icon14, textFont = FontManager.INSTANCE.puHui12;

        ImGui.beginGroup();
        ImVec2 groupPos = ImGui.getCursorScreenPos();
        ImGui.invisibleButton(this.toString(), ImGui.getContentRegionAvailX(), 32);

        if (!dummy) {
            FontManager.pushFont(iconFont);
            ImVec2 iconPos = groupPos.plus(ImTextRenderer.centerText(icon, 9, 16, false, true));
            float iconSize = ImTextRenderer.getTextWidth(icon);
            ImTextRenderer.drawText(
                    drawList,
                    icon,
                    iconPos.x, iconPos.y,
                    highlight? gui.getTheme().getIconHighlightColor() : gui.getTheme().getMainColor()
            );
            ImGui.popFont();

            FontManager.pushFont(textFont);
            ImVec2 textPos = groupPos.plus(ImTextRenderer.centerText(textSupplier.get(), 14 + iconSize, 16, false, true));
            ImTextRenderer.drawText(
                    drawList,
                    textSupplier.get(),
                    textPos.x, textPos.y,
                    highlight? gui.getTheme().getTextHighlightColor() : gui.getTheme().getMainColor()
            );
            ImGui.popFont();
        }

        ImGui.endGroup();
        ImVec2 buttonMin = ImGui.getItemRectMin(), buttonMax = ImGui.getItemRectMax();
        boolean clicked = ImGui.isItemClicked(0);
        drawList.channelsSetCurrent(0);
        if (!dummy && highlight) {
            drawList.addRectFilled(
                    buttonMin, buttonMax,
                    gui.getTheme().getCategoryHighlightColor(), 8f
            );
        }
        drawList.channelsMerge();

        if (clicked) {
            catePanel.setActiveButton(this);
        }
    }
}
