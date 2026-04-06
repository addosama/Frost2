package pub.frost.client.feature.screen.impl.clickgui.styles.panel;

import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.screen.components.InputListener;
import pub.frost.client.feature.screen.components.RenderableComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.category.CategoryPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.impl.ModuleListPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category.CategoryButton;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category.CategoryButtonGroup;

import java.util.ArrayList;
import java.util.List;

public class PanelClickGui implements RenderableComponent, InputListener {
    @Getter
    private final ColorTheme theme = new ColorTheme(
            0x99FFFFFF,
            0x33000000,
            0,
            0xFF4D4D4D,
            0xFF808080,
            0xFF1A1A1A,
            ImColor.rgba("#667DFFFF"),
            0x50999999,
            0xFFEBEBEB,
            0xFFF2F2F2,
            0x20000000,
            0xFFE5E5E5,
            ImColor.rgba("#6699FFFF"),
            0xFFFFFFFF
    );

    private final CategoryPanel categoryPanel;
    @Getter @Setter
    private InputListener activeListener;

    public PanelClickGui() {
        this.categoryPanel = new CategoryPanel(this);
        categoryPanel.addGroup(new CategoryButtonGroup(
                this,
                () -> FrostCore.getLocalizer().get("strings.features").toUpperCase(),
                getModuleCategoryButtons(categoryPanel)
        ));
    }

    @Override
    public void render(boolean dummy, float tickDelta) {
        ImGui.setNextWindowSize(800, 614);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 16);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 4);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 4, 4);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1.9f);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, theme.getWindowBgColor());
        ImGui.pushStyleColor(ImGuiCol.Border, theme.getWindowBorderColor());

        ImGui.begin("PanelClickGui", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize);
        categoryPanel.render(dummy, tickDelta);
        ImGui.sameLine();
        categoryPanel.getActiveButton().getBoundPanel().render(dummy, tickDelta);
        ImGui.end();

        ImGui.popStyleColor(2);
        ImGui.popStyleVar(4);
    }

    private List<CategoryButton> getModuleCategoryButtons(CategoryPanel panel) {
        List<CategoryButton> list = new ArrayList<>();
        for (ModuleCategory category : ModuleCategory.values()) {
            list.add(new CategoryButton(
                    this,
                    panel,
                    new ModuleListPanel(this, category),
                    category.getIcon(),
                    category::getName
            ));
        }
        return list;
    }

    @Override
    public void onInput(InputDevice device, int code, int action) {
        if (activeListener != null) {
            activeListener.onInput(device, code, action);
        }
    }

    @RequiredArgsConstructor @Getter
    public static class ColorTheme {
        private final int
        WindowBgColor,
        WindowBorderColor,
        CategoryPanelBgColor,
        MainColor,
        SecondaryColor,
        TextHighlightColor,
        IconHighlightColor,
        CategoryHighlightColor,
        MainPanelBgColor,
        ModulePanelBgColor,
        SplitColor,
        SwitchDisabledBgColor,
        SwitchEnabledBgColor,
        SwitchIndicatorColor;
    }
}
