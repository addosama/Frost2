package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;

import java.util.List;

public class GroupedPropElement extends PropertyComponent<Object> {
    private final List<PanelComponent> components;

    public GroupedPropElement(PanelClickGui gui, PropertyDescriptor descriptor, List<PanelComponent> components) {
        super(gui, descriptor);
        this.components = components;
    }

    @Override
    protected void renderWidgets(boolean dummy, float tickDelta) {
        ImVec2 cursor = ImGui.getCursorPos();
        ImGui.setCursorPosX(cursor.x + ImGui.getContentRegionAvailX() - 20);
        ImGui.setCursorPosY(cursor.y + 5);
        if (ImGui.invisibleButton(this + ".button", 20, 20)) {
            ImGui.setNextWindowPos(ImGui.getWindowPos().plus(-12, ImGui.getWindowHeight() + 4));
            ImGui.openPopup(this + ".popup");
        }
        boolean popup = ImGui.isPopupOpen(this + ".popup");
        if (!dummy) {
            ImGui.pushFont(FontManager.INSTANCE.icon14);
            ImVec2 buttonPos = ImGui.getItemRectMin();
            ImGui.getWindowDrawList().addText(
                    buttonPos.plus(2, 3),
                    popup? gui.getTheme().getIconHighlightColor() : gui.getTheme().getMainColor(), "\ue678"
            );
            ImGui.popFont();
        }
        if (popup) {
            renderPopup(dummy, tickDelta);
        }
    }

    private void renderPopup(boolean dummy, float tickDelta) {
        float width = ImGui.getWindowWidth();

        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 1f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12, 4);
        ImGui.pushStyleColor(ImGuiCol.PopupBg, gui.getTheme().getModulePanelBgColor());
        ImGui.pushStyleColor(ImGuiCol.Border, gui.getTheme().getWindowBorderColor());

        if (ImGui.beginPopup(this + ".popup")) {
            ImGui.beginChild(
                    this + ".popup.panel",
                    width, 0f,
                    ImGuiChildFlags.AutoResizeY
            );
            for (PanelComponent component : components) {
                if (component.isVisible()) component.render(dummy, tickDelta);
            }
            ImGui.endChild();
            ImGui.endPopup();
        }

        ImGui.popStyleColor(2);
        ImGui.popStyleVar(3);
    }

    @Override
    public Object renderElement(boolean dummy, float tickDelta, String id, Object value) {return null;}

    @Override
    public boolean isVisible() {
        return super.isVisible();
    }
}
