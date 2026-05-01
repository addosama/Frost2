package pub.frost.client.feature.overlay.impl;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.overlay.ClientOverlay;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.ImTextRenderer;

@TranslationKey("modules.hud.props.toggledmodules.name")
public class ToggledModulesOverlay extends ClientOverlay implements Named {
    public ToggledModulesOverlay() {
        super("overlays.toggledmodules");
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(true);

    @Property("HorizontalPadding")
    public final IntegerProperty horizontalPadding = new IntegerProperty(0, 20, 1, 10);
    @Property("VerticalPadding")
    public final IntegerProperty verticalPadding = new IntegerProperty(0, 10, 1, 4);

    @Property("Sidebar")
    public final BooleanProperty sidebar = new BooleanProperty(true);
    @Property("SidebarWidth")
    public final IntegerProperty sidebarWidth = new IntegerProperty(1, 10, 1, 4).setVisibilitySupplier(sidebar::get);
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
        final int hPadding = horizontalPadding.get(), vPadding = verticalPadding.get();
        final int sidebarWidth = sidebar.get()? this.sidebarWidth.get() : 0;

        float maxWidth = 0;
        float yOffset = 0;
        for (
                AbstractModule module :
                FrostCore.getInstance().getModuleManager().getModules(AbstractModule::isEnabled, (m1, m2) -> {
                    float diff = ImTextRenderer.getTextWidth(m2.getName()) - ImTextRenderer.getTextWidth(m1.getName());
                    return diff < 0? -1 : diff == 0? 0 : 1;
                })
        ) {
            final String textRendering = module.getName();
            final float predicatedW = ImTextRenderer.getTextWidth(textRendering) + hPadding * 2 + sidebarWidth;
            final float predicatedH = ImTextRenderer.getTextHeight() + vPadding * 2;

            maxWidth = Math.max(maxWidth, predicatedW);

            renderModule(
                    pos.plus(0, yOffset), draws,
                    textRendering, sidebarWidth, predicatedW, predicatedH,
                    hPadding, vPadding,
                    normalizedOffset, module
            );
            yOffset += predicatedH * normalizedOffset.y;
        }

        ImGui.popFont();
    }

    private void renderModule(
            ImVec2 pos, ImDrawList draws,
            String text, float sidebarWidth, float predicatedW, float predicatedH,
            int hPadding, int vPadding,
            ImVec2 normalizedOffset, AbstractModule module
    ) {
        final int bgColor = ImColor.rgba("#141933CC"), sidebarColor = ImColor.rgba("#E5EAFFFF"), textColor = ImColor.rgba("#E5EAFFFF");

        if (background.get()) {
            draws.addRectFilled(
                    pos, pos.plus(predicatedW * normalizedOffset.x, predicatedH * normalizedOffset.y),
                    bgColor
            );
        }

        ImVec2 sidebarEndXPos = pos;
        if (sidebarWidth > 0) {
            sidebarEndXPos = pos.plus(sidebarWidth * normalizedOffset.x, 0);
            draws.addRectFilled(
                    pos, sidebarEndXPos.plus(0, predicatedH * normalizedOffset.y),
                    sidebarColor
            );
            if (sidebarShadow.get()) {
                draws.addRectFilled(
                        sidebarEndXPos,
                        sidebarEndXPos.plus(2 * normalizedOffset.x, predicatedH * normalizedOffset.y),
                        (sidebarColor & 16579836) >> 2 | sidebarColor & -16777216
                );
            }
            if (sidebarGradient.get()) {
                final int gradientStartColor = ImGui.getColorU32i(sidebarColor, 0.2f),
                        gradientEndColor = ImGui.getColorU32i(sidebarColor, 0);

                draws.addRectFilledMultiColor(
                        sidebarEndXPos,
                        sidebarEndXPos.plus(8 * normalizedOffset.x, predicatedH * normalizedOffset.y),
                        gradientStartColor, gradientEndColor, gradientEndColor, gradientStartColor
                );
            }
        }

        // render text
        {
            float textX = sidebarEndXPos.x + normalizedOffset.x * hPadding;
            float textY = sidebarEndXPos.y + normalizedOffset.y * vPadding;
            if (normalizedOffset.x < 0) textX -= ImTextRenderer.getTextWidth(text);
            if (normalizedOffset.y < 0) textY -= ImTextRenderer.getTextHeight();

            ImTextRenderer.drawText(
                    draws, text,
                    textX, textY,
                    textColor,
                    textShadow.get()
            );
        }
    }

    @Override
    public boolean isVisible() {
        return enabled.get();
    }
}
