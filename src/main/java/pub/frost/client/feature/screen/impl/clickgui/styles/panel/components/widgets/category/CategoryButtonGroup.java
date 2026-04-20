package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category;

import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import lombok.Getter;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.utils.ImTextRenderer;

import java.util.List;
import java.util.function.Supplier;

public class CategoryButtonGroup extends PanelComponent {
    private final Supplier<String> titleSupplier;
    @Getter
    private final List<CategoryButton> buttonList;
    public CategoryButtonGroup(PanelClickGui gui, Supplier<String> titleSupplier, List<CategoryButton> buttonList) {
        super(gui);
        this.titleSupplier = titleSupplier;
        this.buttonList = buttonList;
    }

    @Override
    public void render(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 2);
        ImGui.beginGroup();
        ImGui.pushFont(FontManager.INSTANCE.puHui10);
        ImGui.dummy(9, 0);
        ImGui.sameLine();
        ImTextRenderer.drawText(
                ImGui.getWindowDrawList(),
                titleSupplier.get(),
                ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(),
                gui.getTheme().getSecondaryColor()
        );
        ImGui.textColored(0, titleSupplier.get());
        ImGui.popFont();

        for (CategoryButton button : buttonList) button.render(dummy, tickDelta);

        ImGui.endGroup();
        ImGui.popStyleVar();
    }
}
