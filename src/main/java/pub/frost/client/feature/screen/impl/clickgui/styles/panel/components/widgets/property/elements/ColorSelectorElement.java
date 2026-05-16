package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements;

import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.ElementRenderer;
import pub.frost.utils.ColorUtils;
import pub.frost.utils.MathUtils;
import pub.frost.utils.RenderUtils;

import java.awt.*;

public class ColorSelectorElement extends PanelComponent implements ElementRenderer<float[]> {
    public ColorSelectorElement(PanelClickGui gui, boolean alpha) {
        super(gui);
    }

    @Override
    public float[] renderElement(boolean dummy, float tickDelta, String id, float[] valueHSBA) {
        boolean mouseClicked = ImGui.invisibleButton(
                id,
                18, 18
        );
        ImVec2 itemMin = ImGui.getItemRectMin(), itemMax = ImGui.getItemRectMax();
        if (mouseClicked) ImGui.openPopup(id + ".popup");
        boolean popupOpen = ImGui.isPopupOpen(id + ".popup");

        if (popupOpen) {
            ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 16);
            ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 1f);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 10, 10);
            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 8);
            ImGui.pushStyleColor(ImGuiCol.PopupBg, gui.getTheme().getWindowBgColor());
            ImGui.pushStyleColor(ImGuiCol.Border, gui.getTheme().getWindowBorderColor());

            if (ImGui.beginPopup(id + ".popup")) {
                renderPopupContent(dummy, tickDelta, id, valueHSBA);
                ImGui.endPopup();
            }

            ImGui.popStyleColor(2);
            ImGui.popStyleVar(4);
        }

        if (!dummy) {
            int color = ColorUtils.HSBtoBGR(valueHSBA[0], valueHSBA[1], valueHSBA[2]);
            if (valueHSBA[3] >= 0) {
                color = ColorUtils.reAlpha(
                        color,
                        (int) (valueHSBA[3] * 255)
                );
            }
            ImGui.getWindowDrawList().addRectFilled(
                    itemMin, itemMax,
                    color, 6f
            );
        }

        return valueHSBA;
    }

    public void renderPopupContent(boolean dummy, float tickDelta, String id, float[] value) {
        final ImDrawList draws = ImGui.getWindowDrawList();

        float hRet, sRet, bRet, aRet;
        {
            hRet = value[0];
            sRet = value[1];
            bRet = value[2];
            aRet = value[3];
        }

        // s & b
        final float sbRadius = 12f;
        final ImVec2 sbMin, sbSize;
        {
            ImGui.invisibleButton(
                    id + ".popup.sbButton",
                    240, 240
            );
            sbMin = ImGui.getItemRectMin();
            sbSize = ImGui.getItemRectSize();

            if (ImGui.isItemActive()) {
                ImVec2 mouseVal = ImGui.getMousePos().minus(sbMin.plus(sbRadius, sbRadius)).div(sbSize.minus(sbRadius * 2, sbRadius * 2));
                sRet = MathUtils.clamp(mouseVal.x, 0f, 1f);
                bRet = MathUtils.clamp(1 - mouseVal.y, 0f, 1f);
            }
        }

        // hue
        final ImVec2 hMin, hMax;
        {
            ImGui.invisibleButton(
                    id + ".popup.hButton",
                    240, 18
            );
            hMin = ImGui.getItemRectMin();
            hMax = ImGui.getItemRectMax();

            if (ImGui.isMouseDragging(0) && ImGui.isItemActive()) {
                float val = (ImGui.getMousePosX() - (hMin.x + 6)) / (ImGui.getItemRectSizeX() - 12);
                hRet = MathUtils.clamp(val, 0f, 1f);
            }
        }

        // alpha
        final ImVec2 aMin, aMax;
        if (aRet >= 0) {
            ImGui.invisibleButton(
                    id + ".popup.aButton",
                    240, 18
            );
            aMin = ImGui.getItemRectMin();
            aMax = ImGui.getItemRectMax();

            if (ImGui.isMouseDragging(0) && ImGui.isItemActive()) {
                float val = (ImGui.getMousePosX() - (hMin.x + 6)) / (ImGui.getItemRectSizeX() - 12);
                aRet = MathUtils.clamp(val, 0f, 1f);
            }
        }
        else {
            aMin = null;
            aMax = null;
        }

        final int hueColor = ColorUtils.HSBtoBGR(hRet, 1, 1);
        final int colorFullAlpha = ColorUtils.HSBtoBGR(hRet, sRet, bRet);

        // draw
        if (!dummy) {
            // s & b
            renderRoundedSbPicker(
                    draws,
                    sbMin, sbSize, 12,
                    new ImVec2(sRet, 1 - bRet), colorFullAlpha, hueColor
            );

            // h
            renderRoundedHueBar(
                    draws,
                    hMin, hMax,
                    hRet, hueColor
            );

            // a
            if (aRet >= 0) {
                renderRoundedAlphaBar(
                        draws,
                        aMin, aMax,
                        aRet, colorFullAlpha
                );
            }
        }

        value[0] = hRet;
        value[1] = sRet;
        value[2] = bRet;
        value[3] = aRet;
    }

    private void renderRoundedSbPicker(
            ImDrawList draws,
            ImVec2 pos, ImVec2 size, float radius,
            ImVec2 value, int valueColorFullAlpha, int hueColor
    ) {
        // draw rounds
        {
            // l t round
            draws.addCircleFilled(
                    pos.plus(radius, radius),
                    radius,
                    0xFFFFFFFF
            );
            // r t round
            draws.addCircleFilled(
                    pos.plus(size.x - radius, radius),
                    radius,
                    hueColor
            );
            // l b round
            draws.addCircleFilled(
                    pos.plus(radius, size.y - radius),
                    radius,
                    0xFF000000
            );
            // r b round
            draws.addCircleFilled(
                    pos.plus(size.x - radius, size.y - radius),
                    radius,
                    0xFF000000
            );
        }
        // draw edge gradients
        {
            // top
            draws.addRectFilledMultiColor(
                    pos.plus(radius, 0),
                    pos.plus(size.x - radius, radius),
                    0xFFFFFFFF, hueColor, hueColor, 0xFFFFFFFF
            );
            // left
            draws.addRectFilledMultiColor(
                    pos.plus(0, radius),
                    pos.plus(radius, size.y - radius),
                    0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000, 0xFF000000
            );
            // right
            draws.addRectFilledMultiColor(
                    pos.plus(size.x - radius, radius),
                    pos.plus(size.x, size.y - radius),
                    hueColor, hueColor, 0xFF000000, 0xFF000000
            );
            // bottom
            draws.addRectFilled(
                    pos.plus(radius, size.y - radius),
                    pos.plus(size.x - radius, size.y),
                    0xFF000000
            );
        }
        // draw picker area
        {
            ImVec2 pickerMin = pos.plus(radius, radius), pickerMax = pos.plus(size.x - radius, size.y - radius);
            draws.addRectFilledMultiColor(
                    pickerMin, pickerMax,
                    0xFFFFFFFF, hueColor,
                    hueColor, 0xFFFFFFFF
            );
            draws.addRectFilledMultiColor(
                    pickerMin, pickerMax,
                    0, 0,
                    0xFF000000, 0xFF000000
            );

            // indicator
            {
                ImVec2 indicatorPos = pos.plus(
                        radius + (size.x - radius * 2) * value.x,
                        radius + (size.y - radius * 2) * value.y
                );
                draws.addCircleFilled(
                        indicatorPos,
                        9, 0xFFFFFFFF
                );
                draws.addCircleFilled(
                        indicatorPos,
                        6, valueColorFullAlpha
                );
                draws.addCircle(
                        indicatorPos,
                        6, 0x16000000
                );
            }
        }
    }

    private void renderRoundedHueBar(
            ImDrawList draws,
            ImVec2 minVec, ImVec2 maxVec,
            float hValue, int hColor
    ) {
        final float barX = minVec.x + 6, barY = minVec.y + 5;
        final float barWidth = maxVec.x - minVec.x - 12, barHeight = maxVec.y - minVec.y - 10;
        // bar
        {
            // round & width fix
            {
                // left round
                draws.addCircleFilled(
                        minVec.plus(4, 9),
                        4,
                        0xFF0000FF
                );
                // left width fix
                draws.addRectFilled(
                        minVec.plus(4, 5),
                        minVec.plus(6, 13),
                        0xFF0000FF
                );
                // right round
                draws.addCircleFilled(
                        maxVec.minus(4, 9),
                        4,
                        0xFF0000FF
                );
                // right width fix
                draws.addRectFilled(
                        maxVec.minus(13, 6),
                        maxVec.minus(4, 5),
                        0xFF0000FF
                );
            }

            // hue bar
            final float step = 1 / 7f;
            RenderUtils.drawHorizontalGradientRect(
                    draws,
                    barX, barY, barWidth, barHeight,
                    ColorUtils.HSBtoBGR(0, 1, 1),
                    ColorUtils.HSBtoBGR(step * 1, 1, 1),
                    ColorUtils.HSBtoBGR(step * 2, 1, 1),
                    ColorUtils.HSBtoBGR(step * 3, 1, 1),
                    ColorUtils.HSBtoBGR(step * 4, 1, 1),
                    ColorUtils.HSBtoBGR(step * 5, 1, 1),
                    ColorUtils.HSBtoBGR(step * 6, 1, 1),
                    ColorUtils.HSBtoBGR(1, 1, 1)
            );
        }

        // indicator
        {
            ImVec2 indicatorCenter = new ImVec2(barX + barWidth * hValue, barY + barHeight / 2);
            // bg
            drawIndicator(
                    draws,
                    indicatorCenter,
                    6f, 3f, 6f,
                    0xFFFFFFFF
            );
            // color
            drawIndicator(
                    draws,
                    indicatorCenter,
                    3f, 3f, 3f,
                    hColor
            );
        }
    }

    private void renderRoundedAlphaBar(
            ImDrawList draws,
            ImVec2 minVec, ImVec2 maxVec,
            float alphaValue, int colFullAlpha
    ) {
        final float barX = minVec.x + 6, barY = minVec.y + 5;
        final float barWidth = maxVec.x - minVec.x - 12, barHeight = maxVec.y - minVec.y - 10;
        // bar
        {
            final int colNoAlpha = ColorUtils.reAlpha(colFullAlpha, 0);
            // round & width fix
            {
                // left round
                draws.addCircleFilled(
                        minVec.plus(4, 9),
                        4,
                        colNoAlpha
                );
                // left width fix
                draws.addRectFilled(
                        minVec.plus(4, 5),
                        minVec.plus(6, 13),
                        colNoAlpha
                );
                // right round
                draws.addCircleFilled(
                        maxVec.minus(4, 9),
                        4,
                        colFullAlpha
                );
                // right width fix
                draws.addRectFilled(
                        maxVec.minus(13, 6),
                        maxVec.minus(4, 5),
                        colFullAlpha
                );
            }

            // alpha bar
            RenderUtils.drawHorizontalGradientRect(
                    draws,
                    barX, barY, barWidth, barHeight,
                    colNoAlpha, colFullAlpha
            );
        }

        // indicator
        {
            ImVec2 indicatorCenter = new ImVec2(barX + barWidth * alphaValue, barY + barHeight / 2);
            // bg
            drawIndicator(
                    draws,
                    indicatorCenter,
                    6f, 3f, 6f,
                    0xFFFFFFFF
            );
        }
    }

    private void drawIndicator(
            ImDrawList draws,
            ImVec2 indicatorCenter,
            float xOffset, float yOffset, float radius,
            int color
    ) {
        draws.addCircleFilled(
                indicatorCenter.minus(0, yOffset),
                radius, color
        );
        draws.addCircleFilled(
                indicatorCenter.plus(0, yOffset),
                radius, color
        );
        draws.addRectFilled(
                indicatorCenter.minus(xOffset, yOffset),
                indicatorCenter.plus(xOffset, yOffset),
                color
        );
    }

    @Override
    public float getElementWidth(float[] value) {
        return 18;
    }

    @Override @Deprecated
    public void render(boolean dummy, float tickDelta) {}
}
