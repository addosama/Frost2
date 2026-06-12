package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.panels.main.child;

import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.PropertyComponent;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.utils.ImTextRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PropertyPanel extends PanelComponent {
    private final Supplier<String> titleSupplier;
    private final Supplier<Float> widthSupplier;
    private final List<PanelComponent> components;
    public PropertyPanel(PanelClickGui gui, Supplier<String> titleSupplier, List<PropertyDescriptor> descriptorList, Supplier<Float> widthSupplier) {
        super(gui);
        this.titleSupplier = titleSupplier;
        this.widthSupplier = widthSupplier;
        this.components = new ArrayList<>();
        descriptorList.forEach(d -> {
            PanelComponent component = PropertyComponent.buildForDescriptor(gui, d);
            if (component != null) components.add(component);
        });
    }
    public PropertyPanel(PanelClickGui gui, AbstractModule module, Supplier<Float> widthSupplier) {
        this(gui, module::getName, module.getPropertyList(), widthSupplier);
    }

    @Override
    public void render(boolean dummy, float tickDelta) {
        float width = widthSupplier.get();

        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 2);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0);
        ImGui.beginChild(
                this.toString(),
                width, 0,
                ImGuiChildFlags.AutoResizeY
        );
        // title
        {
            String title = titleSupplier.get();
            ImGui.pushFont(FontManager.INSTANCE.puHui10);
            ImGui.dummy(10, 0);
            ImGui.sameLine();
            ImTextRenderer.drawText(
                    ImGui.getWindowDrawList(),
                    title,
                    ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(),
                    gui.getTheme().getSecondaryColor()
            );
            ImGui.textColored(0, title);
            ImGui.popFont();
        }
        // props
        {
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12, 4);
            ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 8);
            ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, 1f);
            ImGui.pushStyleColor(ImGuiCol.ChildBg, gui.getTheme().getModulePanelBgColor());
            ImGui.pushStyleColor(ImGuiCol.Border, gui.getTheme().getSplitColor());
            ImGui.beginChild(
                    this + ".props",
                    0, 0,
                    ImGuiChildFlags.AlwaysUseWindowPadding | ImGuiChildFlags.AutoResizeY | ImGuiChildFlags.Border
            );

            boolean firstProp = true;
            for (PanelComponent component : components) {
                boolean render = component.isVisible();
                if (render) {
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
                    component.render(dummy, tickDelta);
                }
            }

            ImGui.endChild();
            ImGui.popStyleColor(2);
            ImGui.popStyleVar(3);
        }
        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
    }
}
