package pub.frost.client.feature.module.impl.visual.esp;

import imgui.ImGui;
import imgui.ImVec2;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.visual.ESP;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.utils.RenderUtils;
import pub.frost.utils.data.BoundingBox;

public class ChestESP extends AbstractSubModule<ESP> {
    public ChestESP(ESP esp) {
        super(esp);
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(true);

    public void renderChest(
            BoundingBox bb,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int width, int height
    ) {
        ESP esp = this.parent;
        switch (esp.box.mode.getValue()) {
            case RECT: {
                ImVec2[] projected = projectBoundingBox(bb, modelViewMatrix, projectionMatrix, width, height);
                if (projected == null) return;
                float thickness = esp.box.thickness.get();
                if (esp.box.outline.get()) {
                    ImGui.getBackgroundDrawList().addRect(
                            projected[0], projected[1],
                            0xFF000000,
                            0,
                            thickness * 3
                    );
                }
                ImGui.getBackgroundDrawList().addRect(
                        projected[0], projected[1],
                        -1,
                        0,
                        thickness
                );
                return;
            }
            case BOX_2D: {
                float thickness = esp.box.thickness.get();
                if (esp.box.shadow.get()) {
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
                float thickness = esp.box.thickness.get();
                if (esp.box.shadow.get()) {
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

    private ImVec2[] projectBoundingBox(
            BoundingBox bb,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int width, int height
    ) {
        Vector3d[] vertices = bb.getVertices();
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        boolean anyValid = false;
        for (Vector3d vertex : vertices) {
            ImVec2 screen = RenderUtils.worldToScreen(vertex, modelViewMatrix, projectionMatrix, width, height);
            if (screen != null) {
                anyValid = true;
                if (screen.x < minX) minX = screen.x;
                if (screen.y < minY) minY = screen.y;
                if (screen.x > maxX) maxX = screen.x;
                if (screen.y > maxY) maxY = screen.y;
            }
        }
        if (!anyValid) return null;
        return new ImVec2[]{new ImVec2(minX, minY), new ImVec2(maxX, maxY)};
    }
}
