package pub.frost.client.feature.overlay.impl;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.overlay.ClientOverlay;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.utils.ImTextRenderer;
import pub.frost.utils.data.EnumTextFormatting;

public class ToggledModulesOverlay extends ClientOverlay {
    public ToggledModulesOverlay() {
        super("overlays.toggledmodules");
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(true);

    @Property("Sidebar")
    public final BooleanProperty sidebar = new BooleanProperty(true);
    @Property("SidebarGradient")
    public final BooleanProperty sidebarGradient = new BooleanProperty(true).setVisibilitySupplier(BooleanProperty.class, sidebar::get);
    @Property("SidebarShadow")
    public final BooleanProperty sidebarShadow = new BooleanProperty(false).setVisibilitySupplier(BooleanProperty.class, sidebar::get);

    @Property("Background")
    public final BooleanProperty background = new BooleanProperty(true);
    @Property("TextShadow")
    public final BooleanProperty textShadow = new BooleanProperty(false);

    @Override
    protected void doRender(boolean dummy, boolean input, float tickDelta) {
        if (!enabled.get()) return;
        int windowFlags = DEFAULT_WINDOW_FLAGS;
        if (!input) windowFlags |= ImGuiWindowFlags.NoInputs;
        ImGui.begin(this.toString(), windowFlags);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
        ImGui.pushFont(FontManager.INSTANCE.puhui14);

        for (
                AbstractModule module :
                FrostCore.getInstance().getModuleManager().getModules(AbstractModule::isEnabled, (m1, m2) -> {
                    float diff = ImTextRenderer.getTextWidth(m2.getName()) - ImTextRenderer.getTextWidth(m1.getName());
                    return diff < 0? -1 : diff == 0? 0 : 1;
                })
        ) {
            renderModule(
                    dummy, input, tickDelta,
                    module
            );
        }

        ImGui.popFont();
        ImGui.popStyleVar();
        ImGui.end();
    }

    private void renderModule(boolean dummy, boolean input, float tickDelta, AbstractModule module) {
        ImGui.beginGroup();
        ImVec2 sidebarPosMin;
        ImVec2 textPos;
        ImVec2 panelPosMin, panelPosMax;
        String moduleName = module.getName();

        if (sidebar.get()) {
            ImGui.dummy(2, 0);
            sidebarPosMin = ImGui.getItemRectMin();
            ImGui.sameLine();
        } else sidebarPosMin = ImGui.getCursorScreenPos();

        {
            ImGui.beginGroup();
            ImGui.dummy(0, 4);
            ImGui.dummy(10, 0);
            ImGui.sameLine();
            ImGui.textColored(0, EnumTextFormatting.removeFormat(moduleName));
            textPos = ImGui.getItemRectMin();
            ImGui.sameLine();
            ImGui.dummy(10, 0);
            ImGui.dummy(0, 4);
            ImGui.endGroup();
            panelPosMin = ImGui.getItemRectMin();
            panelPosMax = ImGui.getItemRectMax();
        }

        if (!dummy) {
            final int
                    bgColor = ImColor.rgba("#141933CC"),
                    sidebarColor = ImColor.rgba("#E5EAFFFF"),
                    textColor = ImColor.rgba("#E5EAFFFF")
            ;
            ImDrawList draws = ImGui.getWindowDrawList();
            if (background.get()) {
                draws.addRectFilled(
                        panelPosMin, panelPosMax,
                        bgColor
                );
            }
            if (sidebar.get()) {
                float panelHeight = panelPosMax.y - panelPosMin.y;
                // draw sidebar
                {
                    ImVec2 sideBarPosMax = sidebarPosMin.plus(2, panelHeight);
                    if (sidebarShadow.get()) {
                        draws.addRectFilled(
                                sidebarPosMin.plus(1, 0),
                                sideBarPosMax.plus(1, 0),
                                (sidebarColor & 16579836) >> 2 | sidebarColor & -16777216
                        );
                    }
                    draws.addRectFilled(
                            sidebarPosMin, sideBarPosMax,
                            sidebarColor
                    );
                }
                if (sidebarGradient.get()) {
                    ImVec2 gradientPosMin = sidebarPosMin.plus(2, 0), gradientPosMax = gradientPosMin.plus(8, panelHeight);
                    final int gradientStartColor = ImGui.getColorU32i(sidebarColor, 0.2f),
                            gradientEndColor = ImGui.getColorU32i(sidebarColor, 0);
                    draws.addRectFilledMultiColor(
                            gradientPosMin, gradientPosMax,
                            gradientStartColor,
                            gradientEndColor,
                            gradientEndColor,
                            gradientStartColor
                    );
                }
            }
            ImTextRenderer.drawText(
                    draws,
                    moduleName,
                    textPos.x, textPos.y,
                    textColor,
                    textShadow.get()
            );
        }

        ImGui.endGroup();
    }
}
