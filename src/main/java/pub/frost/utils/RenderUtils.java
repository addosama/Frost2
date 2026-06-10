package pub.frost.utils;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class RenderUtils {
    public static ImVec2 worldToScreen(
            Vec3 point,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int windowWidth, int windowHeight
    ) {
        // 1. 必须使用 4D 向量
        Vector4f pos = new Vector4f((float) point.xCoord, (float) point.yCoord, (float) point.zCoord, 1.0f);

        // 2. 变换顺序：先 View 再 Projection
        modelViewMatrix.transform(pos);
        projectionMatrix.transform(pos);

        // 3. 如果 w <= 0，点在相机背面，直接剪裁
        if (pos.w <= 0) return null;

        // 4. 计算 NDC (归一化设备坐标)
        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;

        // 5. 映射到屏幕 (注意：ImGui 使用的是缩放后的坐标 ScaledResolution)
        // 如果文字还是太小或位置偏，尝试把 width/height 换成 ImGui.getIO().getDisplaySize()
        float x = (ndcX + 1.0f) * 0.5f * windowWidth;
        float y = (1.0f - ndcY) * 0.5f * windowHeight;

        return new ImVec2(x, y);
    }

    public static Matrix4f getModelViewMatrix() {
        return RenderUtils.getMatrix(GL11.GL_MODELVIEW_MATRIX);
    }
    public static Matrix4f getProjectionMatrix() {
        return RenderUtils.getMatrix(GL11.GL_PROJECTION_MATRIX);
    }

    public static Matrix4f getMatrix(int matrixType) {FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        org.lwjgl.opengl.GL11.glGetFloat(matrixType, buffer);
        float[] values = new float[16];
        buffer.get(values);
        Matrix4f matrix = new Matrix4f();
        matrix.set(values);
        matrix.transpose(); // 必须转置，因为 OpenGL 是列主序，而 vecmath transform 是按行算的
        return matrix;
    }
    
    private static Vector4f toViewSpace(Vec3 point, Matrix4f modelViewMatrix) {
        Vector4f pos = new Vector4f((float) point.xCoord, (float) point.yCoord, (float) point.zCoord, 1.0f);
        modelViewMatrix.transform(pos);
        return pos;
    }
    private static ImVec2 projectViewSpace(Vector4f viewPos, Matrix4f projectionMatrix, int windowWidth, int windowHeight) {
        Vector4f clip = new Vector4f(viewPos.x, viewPos.y, viewPos.z, viewPos.w);
        projectionMatrix.transform(clip);

        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;

        float x = (ndcX + 1.0f) * 0.5f * windowWidth;
        float y = (1.0f - ndcY) * 0.5f * windowHeight;

        return new ImVec2(x, y);
    }
    private static void drawClippedLine(
            ImDrawList list,
            Vec3 a, Vec3 b,
            int color, float thickness,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int windowWidth, int windowHeight
    ) {
        Vector4f va = toViewSpace(a, modelViewMatrix);
        Vector4f vb = toViewSpace(b, modelViewMatrix);

        boolean aVisible = va.z <= 0;
        boolean bVisible = vb.z <= 0;

        // 两端都在近裁剪面后方，直接跳过
        if (!aVisible && !bVisible) return;

        // 只有一端可见时，把另一端裁剪到 near plane
        if (aVisible != bVisible) {
            float dz = vb.z - va.z;
            if (Math.abs(dz) < 1.0E-6f) return;

            float t = (0 - va.z) / dz;
            t = Math.max(0, Math.min(1.0f, t));

            Vector4f clipped = new Vector4f(
                    va.x + (vb.x - va.x) * t,
                    va.y + (vb.y - va.y) * t,
                    0,
                    1.0f
            );

            if (!aVisible) {
                va = clipped;
            } else {
                vb = clipped;
            }
        }

        ImVec2 p1 = projectViewSpace(va, projectionMatrix, windowWidth, windowHeight);
        ImVec2 p2 = projectViewSpace(vb, projectionMatrix, windowWidth, windowHeight);
        list.addLine(p1, p2, color, thickness);
    }

    public static void drawBoundingBox(
            ImDrawList list, AxisAlignedBB boundingBox, float thickness, int color,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int windowWidth, int windowHeight
    ) {
        Vec3[] v = BoundingBoxUtils.getVertices(boundingBox);

        // 12 条边：底面 4 条、顶面 4 条、4 条竖边
        int[][] edges = {
                {0, 1}, {1, 2}, {2, 3}, {3, 0},
                {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };

        for (int[] edge : edges) {
            drawClippedLine(
                    list,
                    v[edge[0]], v[edge[1]],
                    color, thickness,
                    modelViewMatrix, projectionMatrix,
                    windowWidth, windowHeight
            );
        }
    }

    public static void drawBoundingBox2DOutline(
            ImDrawList list, AxisAlignedBB boundingBox, float thickness, int color,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int windowWidth, int windowHeight
    ) {
        Vec3[] vertices = BoundingBoxUtils.getVertices(boundingBox);
        List<ImVec2> pts =  new ArrayList<>();
        Matrix4f modelView = getModelViewMatrix(), projection = getProjectionMatrix();
        for (Vec3 vec3 : vertices) {
            ImVec2 vec2 = worldToScreen(
                    vec3,
                    modelViewMatrix, projectionMatrix,
                    windowWidth, windowHeight
            );
            if (vec2 != null) pts.add(vec2);
        }

        pts = VecUtils.convexHull(pts);
        if (!pts.isEmpty()) pts.add(pts.get(0));
        ImVec2[] array = pts.toArray(new ImVec2[0]);

        list.addPolyline(
                array, array.length,
                color,
                0,
                thickness
        );
    }

    public static void drawRect(
            ImDrawList draws,
            float x, float y, float width, float height,
            int colorABGR
    ) {
        draws.addRect(
                x, y, x + width, y + height,
                colorABGR
        );
    }

    public static void renderBox(
            AxisAlignedBB lerpedBB,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int width, int height,
            EnumBoxRenderType type,
            float thickness, float shadowExThickness,
            int color, int shadowColor
    ) {
        // project vertices to screen
        final Vec3[] vertexArray = BoundingBoxUtils.getVertices(lerpedBB);
        final ImVec2[] vertexScreenPosArray = new ImVec2[vertexArray.length];
        ImVec2 minVec, maxVec;
        {
            for (int i = 0; i < 8; i++) {
                Vec3 vertex = vertexArray[i];
                vertexScreenPosArray[i] = RenderUtils.worldToScreen(
                        vertex,
                        modelViewMatrix, projectionMatrix,
                        width, height
                );
            }

            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
            for (ImVec2 vec : vertexScreenPosArray) {
                if (vec == null) {
                    continue;
                }
                if (vec.x < minX) minX = vec.x;
                if (vec.y < minY) minY = vec.y;
                if (vec.x > maxX) maxX = vec.x;
                if (vec.y > maxY) maxY = vec.y;
            }
            minVec = new ImVec2(minX, minY);
            maxVec = new ImVec2(maxX, maxY);
        }
        final float shadowThickness = thickness + shadowExThickness;
        switch (type) {
            case RECT: {
                if (shadowExThickness > 0 && shadowColor != 0) {
                    ImGui.getBackgroundDrawList().addRect(
                            minVec, maxVec,
                            shadowColor,
                            0,
                            shadowThickness
                    );
                }
                ImGui.getBackgroundDrawList().addRect(
                        minVec, maxVec,
                        color,
                        0,
                        thickness
                );
                break;
            }
            case BOX_2D: {
                if (shadowExThickness > 0 && shadowColor != 0) {
                    RenderUtils.drawBoundingBox2DOutline(
                            ImGui.getBackgroundDrawList(),
                            lerpedBB,
                            shadowThickness, shadowColor,
                            modelViewMatrix, projectionMatrix,
                            width, height
                    );
                }
                RenderUtils.drawBoundingBox2DOutline(
                        ImGui.getBackgroundDrawList(),
                        lerpedBB,
                        thickness, color,
                        modelViewMatrix, projectionMatrix,
                        width, height
                );
                break;
            }
            case BOX_3D: {
                if (shadowExThickness > 0 && shadowColor != 0) {
                    RenderUtils.drawBoundingBox(
                            ImGui.getBackgroundDrawList(),
                            lerpedBB,
                            shadowThickness, shadowColor,
                            modelViewMatrix, projectionMatrix,
                            width, height
                    );
                }
                RenderUtils.drawBoundingBox(
                        ImGui.getBackgroundDrawList(),
                        lerpedBB,
                        thickness, color,
                        modelViewMatrix, projectionMatrix,
                        width, height
                );
            }
        }
    }

    public static void drawHorizontalGradientRect(
            ImDrawList draws,
            float x, float y, float width, float height,
            int... colorsABGR
    ) {
        if (colorsABGR.length < 2) {
            if (colorsABGR.length == 1) drawRect(draws, x, y, width, height, colorsABGR[0]);
            return;
        }

        int colorSize = colorsABGR.length;
        float singleWidth = width / (colorSize - 1);
        float currentX = x + width;

        for (int index = 0; index < colorSize - 1; index++) {
            int col1 = colorsABGR[Math.abs(index - colorSize) - 1];
            int col2 = colorsABGR[Math.abs((index + 1) - colorSize) - 1];
            draws.addRectFilledMultiColor(
                    currentX - singleWidth, y,
                    currentX, y + height,
                    col2, col1,
                    col1, col2
            );
            currentX -= singleWidth;
        }
    }
    /*
    public static void drawVerticalGradientRect(float x, float y, float width, float height, int... colors) {
        if (colors.length < 2) {
            if (colors.length == 1) drawRect(x, y, width, height, colors[0]);
            return;
        }

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int colorSize = colors.length;
        float singleHeight = height / (colorSize - 1);
        float currentY = y;
        Function<Integer, float[]> rgbaFun = color -> new float[] {
                (float)(color >> 16 & 255) / 255.0F,
                (float)(color >> 8 & 255) / 255.0F,
                (float)(color & 255) / 255.0F,
                (float)(color >> 24 & 255) / 255.0F
        };

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (int index = 0; index < colorSize - 1; index++) {
            float[] rgba = rgbaFun.apply(colors[index]);
            float[] next = rgbaFun.apply(colors[index + 1]);

            worldrenderer.pos(x + width, currentY, 0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
            worldrenderer.pos(x, currentY, 0).color(rgba[0], rgba[1], rgba[2], rgba[3]).endVertex();
            worldrenderer.pos(x, currentY + singleHeight, 0).color(next[0], next[1], next[2], next[3]).endVertex();
            worldrenderer.pos(x + width, currentY + singleHeight, 0).color(next[0], next[1], next[2], next[3]).endVertex();
            currentY += singleHeight;
        }
//        worldrenderer.pos((double)right, (double)top, 0).color(f1, f2, f3, f).endVertex();
//        worldrenderer.pos((double)left, (double)top, 0).color(f1, f2, f3, f).endVertex();
//        worldrenderer.pos((double)left, (double)bottom, 0).color(f5, f6, f7, f4).endVertex();
//        worldrenderer.pos((double)right, (double)bottom, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
     */
}