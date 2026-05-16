package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.elements;

import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.ElementRenderer;
import pub.frost.utils.ColorUtils;
import pub.frost.utils.MathUtils;

import java.awt.*;

public class ColorSelectorElement extends PanelComponent implements ElementRenderer<Integer> {
    public ColorSelectorElement(PanelClickGui gui, boolean alpha) {
        super(gui);
    }

    @Override
    public Integer renderElement(boolean dummy, float tickDelta, String id, Integer value) {
        boolean mouseClicked = ImGui.invisibleButton(
                id,
                18, 18
        );
        ImVec2 itemMin = ImGui.getItemRectMin(), itemMax = ImGui.getItemRectMax();
        if (mouseClicked) ImGui.openPopup(id + ".popup");
        boolean popupOpen = ImGui.isPopupOpen(id + ".popup");

        if (!dummy) {
            ImGui.getWindowDrawList().addRectFilled(
                    itemMin, itemMax,
                    value, 6f
            );
        }

        int valueRet = value;
        if (popupOpen) {
            ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 16);
            ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 1f);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 10, 10);
            ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 8);
            ImGui.pushStyleColor(ImGuiCol.PopupBg, gui.getTheme().getWindowBgColor());
            ImGui.pushStyleColor(ImGuiCol.Border, gui.getTheme().getWindowBorderColor());

            if (ImGui.beginPopup(id + ".popup")) {
                valueRet = renderPopupContent(dummy, tickDelta, id, value);
                ImGui.endPopup();
            }

            ImGui.popStyleColor(2);
            ImGui.popStyleVar(4);
        }

        return valueRet;
    }

    public Integer renderPopupContent(boolean dummy, float tickDelta, String id, Integer value) {
        final ImDrawList draws = ImGui.getWindowDrawList();

        float hRet, sRet, bRet;
        int alphaRet;
        {
            final ImVec4 color = ColorUtils.toImVec4(value);
            float[] colorHSB = new float[3];
            colorHSB = Color.RGBtoHSB(
                    (int) (color.x * 255), (int) (color.y * 255), (int) (color.z * 255),
                    colorHSB
            );

            hRet = colorHSB[0];
            sRet = colorHSB[1];
            bRet = colorHSB[2];

            alphaRet = (int) (color.w * 255);
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
        final ImVec2 hMin, hMax, hSize;
        {
            ImGui.invisibleButton(
                    id + ".popup.hButton",
                    240, 18
            );
            hMin = ImGui.getItemRectMin();
            hMax = ImGui.getItemRectMax();
            hSize = ImGui.getItemRectSize();

            if (ImGui.isMouseDragging(0) && ImGui.isItemActive()) {

            }
        }

        final int hueColor = ColorUtils.toABGR(Color.HSBtoRGB(hRet, 1, 1));
        final int colorFullAlpha = ColorUtils.toABGR(Color.HSBtoRGB(hRet, sRet, bRet));
        final int colorRet = ColorUtils.reAlpha(
                colorFullAlpha,
                alphaRet
        );

        // draw
        if (!dummy) {
            // s & b
            {
                renderRoundedSbPicker(
                        draws,
                        sbMin, sbSize, 12,
                        new ImVec2(sRet, 1 - bRet), hueColor
                );
            }

            // h
            {
                draws.addRectFilledMultiColor(
                        hMin.plus(0, 5), hMax.minus(0, 5),
                        0xFFFFFFFF, 0xFF000000,
                        0xFF000000, 0xFFFFFFFF
                );
            }
        }

        return colorRet;
    }

    private void renderRoundedSbPicker(
            ImDrawList draws,
            ImVec2 pos, ImVec2 size, float radius,
            ImVec2 value, int hueColor
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
                        6, 0xFFFFFFFF
                );
                draws.addCircleFilled(
                        indicatorPos,
                        4, 0x16000000
                );
                draws.addCircleFilled(
                        indicatorPos,
                        4, hueColor
                );
                draws.addCircle(
                        indicatorPos,
                        4, 0x16000000
                );
            }
        }
    }

    private void renderRoundedHueBar(

    ) {

    }

    @Override
    public float getElementWidth(Integer value) {
        return 18;
    }

    @Override @Deprecated
    public void render(boolean dummy, float tickDelta) {}
}
