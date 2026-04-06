package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.child;

import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;

import java.util.List;
import java.util.function.Supplier;

public class ModulePanel extends PanelComponent {
    private final AbstractModule module;
    private final Supplier<Float> widthSupplier;
    public ModulePanel(PanelClickGui gui, AbstractModule module, Supplier<Float> widthSupplier) {
        super(gui);
        this.module = module;
        this.widthSupplier = widthSupplier;
    }

    @Override
    public void render(boolean dummy, float tickDelta) {
        float width = widthSupplier.get();
        List<PropertyDescriptor> props = module.getPropertyList();

        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 2);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0);
        ImGui.beginChild(
                this.toString(),
                width, 0,
                ImGuiChildFlags.AutoResizeY
        );
        // title
        {
            ImGui.pushFont(FontManager.INSTANCE.puHui10);
            ImGui.dummy(10, 0);
            ImGui.sameLine();
            ImGui.textColored(gui.getTheme().getSecondaryColor(), module.getName());
            ImGui.popFont();
        }
        // props
        {
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12, 4);
            ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 8);
            ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, 1.9f);
            ImGui.pushStyleColor(ImGuiCol.ChildBg, gui.getTheme().getModulePanelBgColor());
            ImGui.pushStyleColor(ImGuiCol.Border, gui.getTheme().getSplitColor());
            ImGui.beginChild(
                    this + ".props",
                    0, 0,
                    ImGuiChildFlags.AlwaysUseWindowPadding | ImGuiChildFlags.AutoResizeY | ImGuiChildFlags.Border
            );

            boolean firstProp = true;
            for (PropertyDescriptor prop : props) {
                if (!firstProp) {
                    float splitWidth = ImGui.getContentRegionAvailX();
                    ImGui.dummy(splitWidth, 1);
                    if (!dummy) {
                        ImGui.getWindowDrawList().addRectFilled(
                                ImGui.getItemRectMin(), ImGui.getItemRectMax(),
                                gui.getTheme().getSplitColor()
                        );
                    }
                } else firstProp = false;
                drawPropertyComponent(prop);
            }

            ImGui.endChild();
            ImGui.popStyleColor(2);
            ImGui.popStyleVar(3);
        }
        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }

    private void drawPropertyComponent(PropertyDescriptor descriptor) {
        ImGui.beginGroup();
        ImGui.textColored(0xFF000000, descriptor.getName());
        ImGui.endGroup();
        ImGui.getWindowDrawList().addRect(
                ImGui.getItemRectMin(), ImGui.getItemRectMax(),
                0xFF00FF00
        );
    }
}
