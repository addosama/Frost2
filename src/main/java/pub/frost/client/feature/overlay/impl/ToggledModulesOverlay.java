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

// todo
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
    public final BooleanProperty sidebarGradient = new BooleanProperty(true).setVisibilitySupplier(sidebar::get);
    @Property("SidebarShadow")
    public final BooleanProperty sidebarShadow = new BooleanProperty(false).setVisibilitySupplier(sidebar::get);

    @Property("Background")
    public final BooleanProperty background = new BooleanProperty(true);
    @Property("TextShadow")
    public final BooleanProperty textShadow = new BooleanProperty(false);

    @Override
    protected void doRender(ImVec2 pos, ImVec2 normalizedOffset, ImDrawList draws, boolean input, float tickDelta) {
        ImGui.pushFont(FontManager.INSTANCE.puhui14);

//        final int normalizedXOffset, normalizedYOffset;
//        {
//            ImVec2 displaySize = ImGui.getIO().getDisplaySize();
//            ImVec2 windowPos = ImGui.getWindowPos();
//            normalizedXOffset = windowPos.x <= displaySize.x / 2? 1 : -1;
//            normalizedYOffset = windowPos.y <= displaySize.y / 2? 1 : -1;
//        }
//
//        float lastItemHeight = 0;
//        for (
//                AbstractModule module :
//                FrostCore.getInstance().getModuleManager().getModules(AbstractModule::isEnabled, (m1, m2) -> {
//                    float diff = ImTextRenderer.getTextWidth(m2.getName()) - ImTextRenderer.getTextWidth(m1.getName());
//                    return diff < 0? -normalizedYOffset : diff == 0? 0 : normalizedYOffset;
//                })
//        ) {
//            if (normalizedYOffset < 0) {
//                ImGui.setCursorPosY(ImGui.getCursorPosY() - lastItemHeight * 2);
//            }
//            renderModule(
//                    input, tickDelta,
//                    normalizedXOffset, normalizedYOffset,
//                    module
//            );
//            lastItemHeight = ImGui.getItemRectSizeY();
//        }

        ImGui.popFont();
    }

    private void renderModule(
            boolean input, float tickDelta,
            int normalizedXOffset, int normalizedYOffset,
            AbstractModule module
    ) {
        ImGui.beginGroup();
        ImVec2 sidebarPosMin;
        ImVec2 textPos;
        ImVec2 panelPosMin, panelPosMax;
        String moduleName = module.getName();

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

        if (sidebar.get()) {
            ImGui.dummy(2, 0);
            if (normalizedXOffset < 0) {
                sidebarPosMin = new ImVec2(panelPosMax.x, panelPosMin.y);
            } else {
                sidebarPosMin = panelPosMin;
                panelPosMin = panelPosMin.plus(2, 0);
                panelPosMax = panelPosMax.plus(2, 0);
            }
            ImGui.sameLine();
        } else sidebarPosMin = ImGui.getCursorScreenPos();

        if (true) {
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
                    ImVec2 sideBarPosMax = sidebarPosMin.plus(2 * normalizedXOffset, panelHeight);
                    if (sidebarShadow.get()) {
                        draws.addRectFilled(
                                sidebarPosMin.plus(normalizedXOffset, 0),
                                sideBarPosMax.plus(normalizedXOffset, 0),
                                (sidebarColor & 16579836) >> 2 | sidebarColor & -16777216
                        );
                    }
                    draws.addRectFilled(
                            sidebarPosMin, sideBarPosMax,
                            sidebarColor
                    );
                }
                if (sidebarGradient.get()) {
                    ImVec2 gradientPosMin = sidebarPosMin.plus(2 * normalizedXOffset, 0), gradientPosMax = gradientPosMin.plus(8 * normalizedXOffset, panelHeight);
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

    @Override
    public boolean isVisible() {
        return enabled.get();
    }
}
