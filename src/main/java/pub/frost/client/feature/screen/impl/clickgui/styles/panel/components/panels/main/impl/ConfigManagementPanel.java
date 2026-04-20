package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.impl;

import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.config.Config;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.MainPanel;
import pub.frost.utils.ImTextRenderer;

import java.nio.file.Path;
import java.util.Map;

public class ConfigManagementPanel extends MainPanel {
    public ConfigManagementPanel(PanelClickGui gui) {
        super(gui);
    }

    @Override
    protected void renderPanelContent(boolean dummy, float tickDelta) {
        renderTitle(dummy, tickDelta);
        renderSplit(dummy, tickDelta);
        renderConfigList(dummy, tickDelta);
    }

    private void renderTitle(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8, 3);
        ImGui.beginChild(
                this + ".title",
                0f, 36f,
                ImGuiChildFlags.AlwaysUseWindowPadding
        );

        // render left icons
        {
            ImGui.beginGroup();
            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 6, 0);

            // refresh button
            if (renderIconButton(dummy, tickDelta, "\ue7d0")) {
                FrostCore.getInstance().getConfigManager().refreshConfigList();
            }

            ImGui.sameLine();

            // save button
            if (renderIconButton(dummy, tickDelta, "\ue868")) {
                Config currentConfig = FrostCore.getInstance().getConfigManager().getCurrentConfig();
                currentConfig.save();
                FrostCore.getInstance().getConfigManager().writeConfig(currentConfig);
            }

            ImGui.popStyleVar();
            ImGui.endGroup();
        }

        ImGui.endChild();
        ImGui.popStyleVar();
    }
    private void renderSplit(boolean dummy, float tickDelta) {
        ImGui.dummy(ImGui.getContentRegionAvailX(), 1);
        if (!dummy) {
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getItemRectMin(), ImGui.getItemRectMax(),
                    gui.getTheme().getSplitColor()
            );
        }
    }
    private void renderConfigList(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8, 0);
        ImGui.beginChild(
                this + ".configList",
                0f, 0f,
                ImGuiChildFlags.AlwaysUseWindowPadding | ImGuiChildFlags.AutoResizeY
        );

        for (Map.Entry<Path, Config> entry : FrostCore.getInstance().getConfigManager().getConfigMap().entrySet()) {
            renderConfigButton(dummy, tickDelta, entry);
        }

        ImGui.endChild();
        ImGui.popStyleVar();
    }

    private boolean renderIconButton(boolean dummy, float tickDelta, String icon) {
        boolean active = ImGui.invisibleButton(this + "iconbutton." + icon, 30, 30);
        if (!dummy) {
            ImGui.pushFont(FontManager.INSTANCE.icon14);
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getItemRectMin(), ImGui.getItemRectMax(),
                    gui.getTheme().getModulePanelBgColor(), 6f
            );
            ImGui.getWindowDrawList().addRect(
                    ImGui.getItemRectMin(), ImGui.getItemRectMax(),
                    gui.getTheme().getSplitColor(), 6f,
                    1.8f
            );

            float centerX = ImGui.getItemRectMinX() + 15f, centerY = ImGui.getItemRectMinY() + 15f;
            ImGui.getWindowDrawList().addText(
                    ImTextRenderer.centerText(
                            icon,
                            centerX, centerY,
                            true, true
                    ).minus(0, 1),
                    gui.getTheme().getMainColor(),
                    icon
            );
            ImGui.popFont();
        }
        return active;
    }
    private void renderConfigButton(boolean dummy, float tickDelta, Map.Entry<Path, Config> entry) {
        ImGui.beginGroup();
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12, 8);
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 8);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, 1.95f);

        ImGui.pushStyleColor(ImGuiCol.ChildBg, gui.getTheme().getModulePanelBgColor());
        ImGui.pushStyleColor(ImGuiCol.Border,  gui.getTheme().getSplitColor());

        ImGui.beginChild(
                this + ".configList." + entry,
                ImGui.getContentRegionAvailX(), 0,
                ImGuiChildFlags.AlwaysUseWindowPadding | ImGuiChildFlags.AutoResizeY | ImGuiChildFlags.Border
        );
        {
            // config info
            {
                ImGui.beginGroup();

                String configName = entry.getValue().getName();
                ImGui.pushFont(FontManager.INSTANCE.puHui12);
                ImTextRenderer.drawText(
                        ImGui.getWindowDrawList(),
                        configName,
                        ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(),
                        gui.getTheme().getMainColor()
                );
                ImGui.textColored(0, configName);
                ImGui.popFont();

                ImGui.pushFont(FontManager.INSTANCE.puHui10);
                ImGui.textColored(gui.getTheme().getSecondaryColor(), entry.getKey().getFileName().toString());
                ImGui.popFont();

                ImGui.endGroup();
            }
            ImGui.sameLine();
            // action button
            {
                ImGui.setCursorPosX(ImGui.getCursorPosX() + (ImGui.getContentRegionAvailX() - 60));
                ImGui.setCursorPosY(ImGui.getCursorPosY() + (ImGui.getContentRegionAvailY() - 24) / 2);
                if (entry.getKey().toFile().equals(FrostCore.getInstance().getConfigManager().getCurrentConfig().getFile())) {
                    renderSaveConfigButton(dummy, tickDelta, entry);
                } else renderLoadConfigButton(dummy, tickDelta, entry);
            }
        }
        ImGui.endChild();

        ImGui.popStyleColor(2);
        ImGui.popStyleVar(4);
        ImGui.endGroup();
    }

    private void renderSaveConfigButton(boolean dummy, float tickDelta, Map.Entry<Path, Config> config) {
        String buttonLabel = FrostCore.getLocalizer().get("strings.save") + "###" + config.getKey();
        if (renderButton(dummy, tickDelta, buttonLabel, false)) {
            config.getValue().save();
            FrostCore.getInstance().getConfigManager().writeConfig(config.getValue());
        }
    }
    private void renderLoadConfigButton(boolean dummy, float tickDelta, Map.Entry<Path, Config> config) {
        String buttonLabel = FrostCore.getLocalizer().get("strings.load") + "###" + config.getKey();
        if (renderButton(dummy, tickDelta, buttonLabel, true)) {
            FrostCore.getInstance().getConfigManager().switchConfig(config.getValue());
        }
    }
    private boolean renderButton(boolean dummy, float tickDelta, String label, boolean highlight) {
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 6);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 10, 4);

        ImGui.pushStyleColor(
                ImGuiCol.Button,
                highlight? gui.getTheme().getHighlightButtonBgColor() : gui.getTheme().getButtonBgColor()
        );
        ImGui.pushStyleColor(
                ImGuiCol.ButtonHovered,
                highlight? gui.getTheme().getHighlightButtonHoveredBgColor() : gui.getTheme().getButtonHoveredBgColor()
        );
        ImGui.pushStyleColor(
                ImGuiCol.ButtonActive,
                highlight? gui.getTheme().getHighlightButtonActiveBgColor() : gui.getTheme().getButtonActiveBgColor()
        );
        ImGui.pushStyleColor(
                ImGuiCol.Text,
                highlight? gui.getTheme().getHighlightButtonTextColor() : gui.getTheme().getMainColor()
        );

        ImGui.pushFont(FontManager.INSTANCE.puHui12);

        boolean active = ImGui.button(label, 60f, 24f);

        ImGui.popFont();
        ImGui.popStyleColor(4);
        ImGui.popStyleVar(2);

        return active;
    }
}
