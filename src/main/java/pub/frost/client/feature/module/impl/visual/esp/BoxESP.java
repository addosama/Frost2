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
import pub.frost.client.property.impl.number.IntegerProperty;
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

    @Property("thickness")
    public final FloatProperty thickness = new FloatProperty(0.5f, 3f, 0.5f, 1f);

    @Property("outline")
    public final BooleanProperty outline = new BooleanProperty(true).setVisibilitySupplier(() -> mode.is(EnumBoxRenderType.RECT));

    @Property("shadow")
    public final BooleanProperty shadow = new BooleanProperty(true).setVisibilitySupplier(() -> !mode.is(EnumBoxRenderType.RECT));

    public void renderBox(
            BoundingBox bb,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int width, int height,
            ImVec2 rectMin, ImVec2 rectMax
    ) {
        float thickness = this.thickness.get();
        switch (mode.getValue()) {
            case RECT: {
                if (outline.get()) {
                    ImGui.getBackgroundDrawList().addRect(
                            rectMin, rectMax,
                            0xFF000000,
                            0,
                            thickness * 3
                    );
                }
                ImGui.getBackgroundDrawList().addRect(
                        rectMin, rectMax,
                        -1,
                        0,
                        thickness
                );
                return;
            }
            case BOX_2D: {
                if (shadow.get()) {
                    RenderUtils.drawBoundingBox2DOutline(
                            ImGui.getBackgroundDrawList(),
                            bb,
                            thickness * 4, 0x33000000,
                            modelViewMatrix, projectionMatrix,
                            width, height
                    );
                }
                RenderUtils.drawBoundingBox2DOutline(
                        ImGui.getBackgroundDrawList(),
                        bb,
                        thickness, -1,
                        modelViewMatrix, projectionMatrix,
                        width, height
                );
                return;
            }
            case BOX_3D: {
                if (shadow.get()) {
                    RenderUtils.drawBoundingBox(
                            ImGui.getBackgroundDrawList(),
                            bb,
                            thickness * 4, 0x33000000,
                            modelViewMatrix, projectionMatrix,
                            width, height
                    );
                }
                RenderUtils.drawBoundingBox(
                        ImGui.getBackgroundDrawList(),
                        bb,
                        thickness, -1,
                        modelViewMatrix, projectionMatrix,
                        width, height
                );
            }
        }
    }
}
