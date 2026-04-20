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
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.impl.*;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.descriptor.PropertyDescriptor;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.NumberProperty;
import pub.frost.utils.ImTextRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModulePanel extends PanelComponent {
    private final AbstractModule module;
    private final Supplier<Float> widthSupplier;
    private final List<PanelComponent> components;
    public ModulePanel(PanelClickGui gui, AbstractModule module, Supplier<Float> widthSupplier) {
        super(gui);
        this.module = module;
        this.widthSupplier = widthSupplier;
        this.components = new ArrayList<>();
        module.getPropertyList().forEach(d -> {
            PanelComponent component = getPropertyComponent(d);
            if (component != null) components.add(component);
        });
    }

    private PanelComponent getPropertyComponent(PropertyDescriptor descriptor) {
        AbstractProperty<?> abstractProp = descriptor.getProperty();
        PropertyComponent<?> component = null;
        if (abstractProp != null) {
            if (abstractProp instanceof BooleanProperty) {
                component = new BooleanPropComponent(gui, descriptor, (BooleanProperty) abstractProp);
            } else if (abstractProp instanceof NumberProperty) {
                component = new NumberPropComponent(gui, descriptor, (NumberProperty) abstractProp);
            } else if (abstractProp instanceof ModeProperty) {
                component = new ModePropComponent(gui, descriptor, (ModeProperty) abstractProp);
            } else if (abstractProp instanceof MultipleBooleanProperty) {
                component = new MultipleBooleanPropComponent(gui, descriptor, (MultipleBooleanProperty) abstractProp);
            }
        } else if (descriptor.isGroup()) {
            List<PanelComponent> groupComponents = new ArrayList<>();
            descriptor.getChildProperties().forEach(d -> {
                PanelComponent child = getPropertyComponent(d);
                if (child != null) groupComponents.add(child);
            });
            component = new GroupedPropElement(gui, descriptor, groupComponents);
        }
        return component;
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
            ImGui.pushFont(FontManager.INSTANCE.puHui10);
            ImGui.dummy(10, 0);
            ImGui.sameLine();
            ImTextRenderer.drawText(
                    ImGui.getWindowDrawList(),
                    module.getName(),
                    ImGui.getCursorScreenPosX(), ImGui.getCursorScreenPosY(),
                    gui.getTheme().getSecondaryColor()
            );
            ImGui.textColored(0, module.getName());
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
