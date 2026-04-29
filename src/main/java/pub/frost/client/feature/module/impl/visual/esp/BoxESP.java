package pub.frost.client.feature.module.impl.visual.esp;

import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Matrix4f;
import pub.frost.client.feature.module.api.SubModule;
import pub.frost.client.feature.module.impl.visual.ESP;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.utils.EnumBoxRenderType;
import pub.frost.utils.RenderUtils;
import pub.frost.utils.data.BoundingBox;

public class BoxESP extends SubModule<ESP> {
    public BoxESP(ESP esp) {
        super(esp);
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(true);
    @Property("mode")
    public final ModeProperty<EnumBoxRenderType> mode = new ModeProperty<>(EnumBoxRenderType.RECT);
    @Property("expand")
    public final FloatProperty expand = new FloatProperty(0, 1, 0.1f, 0.1f);

    public void renderBox(
            BoundingBox bb,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int width, int height,
            ImVec2 rectMin, ImVec2 rectMax
    ) {
        switch (mode.getValue()) {
            case RECT: {
                ImGui.getBackgroundDrawList().addRect(
                        rectMin, rectMax,
                        0xFF000000,
                        0,
                        3f
                );
                ImGui.getBackgroundDrawList().addRect(
                        rectMin, rectMax,
                        -1,
                        0,
                        1f
                );
                return;
            }
            case BOX_2D: {
                RenderUtils.drawBoundingBox2DOutline(
                        ImGui.getBackgroundDrawList(),
                        bb,
                        1, -1,
                        modelViewMatrix, projectionMatrix,
                        width, height
                );
                return;
            }
            case BOX_3D: {
                RenderUtils.drawBoundingBox(
                        ImGui.getBackgroundDrawList(),
                        bb,
                        1, -1,
                        modelViewMatrix, projectionMatrix,
                        width, height
                );
            }
        }
    }
}
