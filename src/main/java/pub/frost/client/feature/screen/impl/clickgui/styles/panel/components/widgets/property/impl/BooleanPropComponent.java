package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl;

import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.BooleanProperty;
import pub.frost.utils.ImTextRenderer;

public class BooleanPropComponent extends PropertyComponent {
    private final BooleanProperty prop;
    public BooleanPropComponent(PanelClickGui gui, PropertyDescriptor descriptor, BooleanProperty prop) {
        super(gui, descriptor);
        this.prop = prop;
    }

    @Override
    protected void renderWidgets(boolean dummy, float tickDelta) {
        float baseWidth = 30;
        boolean overrideEnabled = prop.isOverridingEnabled();
        ImVec2 cursor = ImGui.getCursorPos();

        ImGui.setCursorPosY(cursor.y + 6);
        ImGui.setCursorPosX(cursor.x + ImGui.getContentRegionAvailX() - baseWidth);
        ImVec2 switchPos = ImGui.getCursorScreenPos();
        boolean switchClicked = ImGui.invisibleButton(this + ".switch", 30, 18);

        ImVec2 overridePos = null;
        boolean overrideClicked = false;

        if (overrideEnabled) {
            ImGui.setCursorPosY(cursor.y + 8);
            ImGui.setCursorPosX(cursor.x + ImGui.getContentRegionAvailX() - baseWidth - 20);
            overridePos = ImGui.getCursorScreenPos();
            overrideClicked = ImGui.invisibleButton(this + ".override", 14, 14);
        }

        boolean enabled = prop.getValue();
        boolean override = prop.isOverrideActive();
        if (!dummy) {
            // switch
            {
                ImGui.getWindowDrawList().addRectFilled(
                        switchPos, switchPos.plus(30, 18),
                        enabled ? gui.getTheme().getSwitchEnabledBgColor() : gui.getTheme().getSwitchDisabledBgColor(),
                        9f
                );
                ImVec2 offset = new ImVec2(1, 1);
                if (enabled) offset = offset.plus(12, 0);
                ImGui.getWindowDrawList().addRectFilled(
                        switchPos.plus(offset), switchPos.plus(offset).plus(16, 16),
                        gui.getTheme().getSwitchIndicatorColor(), 8f
                );
            }
            // override
            if (overrideEnabled) {
                ImGui.pushFont(FontManager.INSTANCE.icon14);
                String icon = "\uedaf";
                ImVec2 iconSize = ImGui.calcTextSize(icon);
                ImVec2 iconPos = overridePos.plus(
                        (14 - iconSize.x) / 2,
                        (14 - iconSize.y) / 2
                );
                ImTextRenderer.drawText(
                        ImGui.getWindowDrawList(),
                        icon,
                        iconPos.x, iconPos.y - 0.5f,
                        override? gui.getTheme().getIconHighlightColor() : gui.getTheme().getMainColor()
                );
                ImGui.popFont();
            }
        }
        if (switchClicked) {
            prop.set(!enabled);
        }
        if (overrideClicked) {
            ImGui.openPopup(this + ".override.popup");
        }

        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 1.9f);
        ImGui.pushStyleColor(ImGuiCol.PopupBg, ImColor.rgba(0, 0, 0, 100));
        if (ImGui.beginPopup(this + ".override.popup")) {
            ImGui.text("Hello Popup");
            ImGui.endPopup();
        }
        ImGui.popStyleColor();
        ImGui.popStyleVar(2);
    }
}
