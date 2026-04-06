package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;

public abstract class PropertyComponent extends PanelComponent {
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
        ImGui.textColored(gui.getTheme().getMainColor(), descriptor.getName());
        ImGui.setCursorPosY(y);
        ImGui.popFont();
    }
    protected abstract void renderWidgets(boolean dummy, float tickDelta);
}
