package pub.frost.client.feature.module.impl.visual.esp;

import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Matrix4f;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.visual.ESP;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.color.ColorProperty;
import pub.frost.client.property.impl.mode.ModeProperty;
import pub.frost.client.property.impl.number.FloatProperty;
import pub.frost.utils.EnumBoxRenderType;
import pub.frost.utils.RenderUtils;
import pub.frost.utils.data.BoundingBox;

public class BoxESP extends AbstractSubModule<ESP> {
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
    @Property("shadow")
    public final BooleanProperty shadow = new BooleanProperty(true);

    @Property("BoxColor")
    public final ColorProperty boxColor = new ColorProperty(0xFFFFFFFF, true);
    @Property("ShadowColor")
    public final ColorProperty shadowColor = new ColorProperty(0x33000000, true).setVisibilitySupplier(shadow::get);

    @Property("TickPos")
    public final BooleanProperty tickPos = new BooleanProperty(false);

    public void renderBox(
            BoundingBox lerpedBB, BoundingBox prevBB, BoundingBox tickBB,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int width, int height
    ) {
        float thickness = this.thickness.get();
        if (tickPos.get()) {
            RenderUtils.drawBoundingBox(
                    ImGui.getBackgroundDrawList(),
                    prevBB,
                    thickness, 0x330000FF,
                    modelViewMatrix, projectionMatrix,
                    width, height
            );
            RenderUtils.drawBoundingBox(
                    ImGui.getBackgroundDrawList(),
                    tickBB,
                    thickness, 0xFFFF0000,
                    modelViewMatrix, projectionMatrix,
                    width, height
            );
        }
        RenderUtils.renderBox(
                lerpedBB,
                modelViewMatrix, projectionMatrix,
                width, height,
                mode.get(), thickness, shadow.get()? 2 : 0,
                boxColor.getValueABGR(), shadowColor.getValueABGR()
        );
    }
}
