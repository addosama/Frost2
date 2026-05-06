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
        final ImVec2 sbMin, sbMax, sbSize;
        {
            ImGui.invisibleButton(
                    id + ".popup.sbButton",
                    240, 240
            );
            sbMin = ImGui.getItemRectMin();
            sbMax = ImGui.getItemRectMax();
            sbSize = ImGui.getItemRectSize();

            if (ImGui.isItemActive()) {
                ImVec2 mouseVal = ImGui.getMousePos().minus(sbMin).div(sbSize);
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

        // draw
        if (!dummy) {
            // s & b
            {
                draws.addRectFilledMultiColor(
                        sbMin, sbMax,
                        0xFFFFFFFF, ColorUtils.toABGR(Color.HSBtoRGB(hRet, 1, 1)),
                        ColorUtils.toABGR(Color.HSBtoRGB(hRet, 1, 1)), 0xFFFFFFFF
                );
                draws.addRectFilledMultiColor(
                        sbMin, sbMax,
                        0, 0,
                        0xFF000000, 0xFF000000
                );

                ImVec2 sbValue = sbMin.plus(sbSize.x * sRet, sbSize.y * (1 - bRet));
                draws.addCircleFilled(
                        sbValue,
                        6, 0xFFFFFFFF
                );
                draws.addCircleFilled(
                        sbValue,
                        4, 0x16000000
                );
                draws.addCircleFilled(
                        sbValue,
                        4, value
                );
                draws.addCircle(
                        sbValue,
                        4, 0x16000000
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

        return ColorUtils.reAlpha(
                ColorUtils.toABGR(Color.HSBtoRGB(hRet, sRet, bRet)),
                alphaRet
        );
    }

    @Override
    public float getElementWidth(Integer value) {
        return 18;
    }

    @Override @Deprecated
    public void render(boolean dummy, float tickDelta) {}
}
