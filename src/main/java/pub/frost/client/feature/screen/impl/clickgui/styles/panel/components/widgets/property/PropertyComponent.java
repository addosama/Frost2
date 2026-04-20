package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.utils.ImTextRenderer;

public abstract class PropertyComponent<T> extends PanelComponent implements ElementRenderer<T> {
    protected final PropertyDescriptor descriptor;
    public PropertyComponent(PanelClickGui gui, PropertyDescriptor descriptor) {
        super(gui);
        this.descriptor = descriptor;
    }

    @Override
    public final void render(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 0);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0);
        ImGui.beginChild(
                this.toString(),
                0f, 30f,
                0
        );
        renderText(dummy, tickDelta);
        renderWidgets(dummy, tickDelta);
        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }

    protected void renderText(boolean dummy, float tickDelta) {
        ImGui.pushFont(FontManager.INSTANCE.puHui12);
        float lineHeight = ImGui.getTextLineHeight();
        float y = ImGui.getCursorPosY();
        ImGui.setCursorPosY(y + (30 - lineHeight) / 2);
        ImTextRenderer.drawText(
                ImGui.getWindowDrawList(),
                descriptor.getName(),
                ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(),
                gui.getTheme().getMainColor()
        );
        ImGui.textColored(0, descriptor.getName());
        ImGui.setCursorPosY(y);
        ImGui.popFont();
    }
    protected abstract void renderWidgets(boolean dummy, float tickDelta);

    protected ImVec2 setupOverrideButtonPosition(ImVec2 cursor, float elementWidth) {
        ImGui.setCursorPosX(cursor.x + ImGui.getContentRegionAvailX() - elementWidth - 20);
        ImGui.setCursorPosY(cursor.y + 8);
        return ImGui.getCursorScreenPos();
    }
    protected boolean renderOverrideButton(boolean dummy, ImVec2 pos, boolean highlight) {
        boolean clicked = ImGui.invisibleButton(this + ".override", 14, 14);
        if (!dummy) {
            ImGui.pushFont(FontManager.INSTANCE.icon14);
            String icon = "\uedaf";
            ImVec2 iconSize = ImGui.calcTextSize(icon);
            ImVec2 iconPos = pos.plus(
                    (14 - iconSize.x) / 2,
                    (14 - iconSize.y) / 2
            );
            ImTextRenderer.drawText(
                    ImGui.getWindowDrawList(),
                    icon,
                    iconPos.x, iconPos.y - 0.5f,
                    highlight? gui.getTheme().getIconHighlightColor() : gui.getTheme().getMainColor()
            );
            ImGui.popFont();
        }
        return clicked;
    }

    @Override
    public boolean isVisible() {
        return descriptor.isVisible();
    }
}
