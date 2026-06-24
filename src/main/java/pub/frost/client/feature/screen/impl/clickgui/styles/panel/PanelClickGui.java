package pub.frost.client.feature.screen.impl.clickgui.styles.panel;

import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.*;
import imgui.type.ImString;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.client.feature.screen.components.InputListener;
import pub.frost.client.feature.screen.components.RenderableComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.category.CategoryPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.MainPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.impl.ClientSettingsPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.impl.ConfigManagementPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.impl.ModuleListPanel;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category.CategoryButton;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.category.CategoryButtonGroup;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class PanelClickGui implements RenderableComponent, InputListener {
    @Getter @Setter
    private ColorTheme theme = EnumTheme.LIGHT.getTheme();

    private final CategoryPanel categoryPanel;
    @Getter @Setter
    private InputListener activeListener;

    public PanelClickGui() {
        CategoryPanel catePanel = new CategoryPanel(this);
        catePanel.addGroup(new CategoryButtonGroup(
                this,
                () -> FrostCore.getLocalizer().get("strings.features").toUpperCase(),
                getModuleCategoryButtons(catePanel)
        ));
        catePanel.addGroup(new CategoryButtonGroup(
                this,
                () -> FrostCore.getLocalizer().get("strings.client").toUpperCase(),
                Arrays.asList(
                        createCategoryButton(
                                catePanel,
                                new ConfigManagementPanel(this),
                                "\ue868",
                                () -> FrostCore.getLocalizer().get("strings.config")
                        ),
                        createCategoryButton(
                                catePanel,
                                new ClientSettingsPanel(this),
                                "\ue8ba",
                                () -> FrostCore.getLocalizer().get("strings.Settings")
                        )
                )
        ));

        this.categoryPanel = catePanel;
    }

    private ImString str = new ImString();
    @Override
    public void render(boolean dummy, float tickDelta) {
        ImGui.setNextWindowSize(800, 614);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 16);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 4);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 4, 4);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1f);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, theme.getWindowBgColor());
        ImGui.pushStyleColor(ImGuiCol.Border, theme.getWindowBorderColor());

        ImGui.begin("PanelClickGui", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize);
        categoryPanel.render(dummy, tickDelta);
        ImGui.sameLine();
        categoryPanel.getActiveButton().getBoundPanel().render(dummy, tickDelta);
        ImGui.end();

        ImGui.popStyleColor(2);
        ImGui.popStyleVar(4);

        ImGui.begin("Debug123");
        ImGui.text("Ctrl Down: " + ImGui.getIO().getKeyCtrl());
        ImGui.inputText("TestInput", str);
        ImGui.end();
    }

    private CategoryButton createCategoryButton(CategoryPanel panel, MainPanel boundPanel, String icon, Supplier<String> nameSupplier) {
        return new CategoryButton(
                this,
                panel,
                boundPanel,
                icon,
                nameSupplier
        );
    }
    private List<CategoryButton> getModuleCategoryButtons(CategoryPanel panel) {
        List<CategoryButton> list = new ArrayList<>();
        for (ModuleCategory category : ModuleCategory.values()) {
            list.add(createCategoryButton(
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
    }

    @RequiredArgsConstructor @Getter
    public static class ColorTheme {
        private final int
        ClientIconBgColor,
        ClientIconColor,
        ClientNameColor,
        ClientVersionColor,
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
        SwitchIndicatorColor,
        ButtonBgColor,
        ButtonHoveredBgColor,
        ButtonActiveBgColor,
        HighlightButtonBgColor,
        HighlightButtonHoveredBgColor,
        HighlightButtonActiveBgColor,
        HighlightButtonTextColor,
        SliderBgColor,
        SliderHighlightBgColor,
        SliderIndicatorColor,
        SelectorBgColor,
        SelectorElementHoverColor,
        SelectorElementActiveColor,
        ScrollbarTrackColor,
        ScrollbarThumbColor,
        ScrollbarThumbHoverColor,
        ScrollbarThumbActiveColor
        ;
    }

    @TranslationKey("strings.enum.guitheme.~")
    @RequiredArgsConstructor @Getter
    public enum EnumTheme implements Named {
        LIGHT(
                "Light",
                new ColorTheme(
                        0xFF663329,
                        0xFFFFEAE5,
                        0xFF331A15,
                        0xFF665552,
                        0x99FFFFFF,
                        0x33000000,
                        0,
                        0xFF4D4D4D,
                        0xFF808080,
                        0xFF1A1A1A,
                        0xFFFF7D66,
                        0x50999999,
                        0xFFEBEBEB,
                        0xFFF2F2F2,
                        0x20000000,
                        0xFFE5E5E5,
                        0xFFFF9966,
                        0xFFFFFFFF,
                        0xFFE5E5E5,
                        0xFFE5E5E5,
                        0xFFE5E5E5,
                        0xFFFF9966,
                        0xFFFF9966,
                        0xFFFF9966,
                        0xFFFFFFFF,
                        0xFFE5E5E5,
                        0xFFFF9966,
                        0xFFFF9966,
                        0xFFE5E5E5,
                        0x50CCCCCC,
                        0x50999999,
                        0,
                        ImColor.rgba(0, 0, 0, 0.28f),
                        ImColor.rgba(0, 0, 0, 0.42f),
                        ImColor.rgba(0, 0, 0, 0.55f)
                )
        ),
        DARK(
                "Dark",
                new PanelClickGui.ColorTheme(
                        0xFF663329,
                        0xFFFFEAE5,
                        0xFFE5D2CF,
                        0xFF807573,
                        0x990E0E0A,
                        0x33FFFFFF,
                        0,
                        0xFFCCCCCC,
                        0xFF808080,
                        0xFFFFFFFF,
                        0xFFFF7D66,
                        0x50000000,
                        0xFF0D0D0D,
                        0xFF1A1A1A,
                        0x20FFFFFF,
                        0xFF262626,
                        0xFFFF9966,
                        0xFFFFFFFF,
                        0xFF262626,
                        0xFF262626,
                        0xFF262626,
                        0xFFFF9966,
                        0xFFFF9966,
                        0xFFFF9966,
                        0xFFFFFFFF,
                        0xFF262626,
                        0xFFFF9966,
                        0xFFFF9966,
                        0xFF262626,
                        0x50CCCCCC,
                        0x50999999,
                        0,
                        ImColor.rgba(1, 1, 1, 0.14f),
                        ImColor.rgba(1, 1, 1, 0.21f),
                        ImColor.rgba(1, 1, 1, 0.37f)
                )
        )
        ;
        final String key;
        final ColorTheme theme;

        @Override
        public String toString() {
            return key;
        }
    }
}
