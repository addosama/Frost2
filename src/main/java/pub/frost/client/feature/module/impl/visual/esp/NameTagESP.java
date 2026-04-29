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
import pub.frost.utils.ImTextRenderer;
import pub.frost.utils.RenderUtils;
import pub.frost.utils.data.BoundingBox;

public class NameTagESP extends SubModule<ESP> {
    public NameTagESP(ESP esp) {
        super(esp);
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(true);
    @Property("3DPosition")
    public final BooleanProperty use3DPos = new BooleanProperty(false);

    public void renderNameTag(
            BoundingBox bb,
            Matrix4f cachedModelView, Matrix4f cachedProjection,
            int width, int height,
            ImVec2 boxMinVec, ImVec2 boxMaxVec,
            String name, float boxWidth
    ) {
        if (boxMaxVec == null) return;
        float nameWidth = ImTextRenderer.getTextWidth(name);

        float textX, textY;
        if (use3DPos.get()) {
            ImVec2 centerVec = RenderUtils.worldToScreen(
                    bb.getCenter().sub(0, bb.getSizeY() / 2, 0),
                    cachedModelView, cachedProjection,
                    width, height
            );
            if (centerVec == null) return;
            textX = centerVec.x - nameWidth / 2;
            textY = centerVec.y;
        } else {
            if (boxMinVec == null) return;
            textX = boxMinVec.x + (boxWidth - nameWidth) / 2;
            textY = boxMaxVec.y;
        }

        ImTextRenderer.drawOutlinedText(
                ImGui.getBackgroundDrawList(),
                name,
                textX, textY,
                -1, 0xFF000000
        );
    }
}
