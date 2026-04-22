package pub.frost.utils;

import imgui.ImDrawList;
import imgui.ImVec2;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import pub.frost.utils.data.BoundingBox;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class RenderUtils {
    public static ImVec2 worldToScreen(
            Vector3d point,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int windowWidth, int windowHeight
    ) {
        // 1. 必须使用 4D 向量
        Vector4f pos = new Vector4f((float) point.x, (float) point.y, (float) point.z, 1.0f);

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
        return matrix;
    }
    
    private static Vector4f toViewSpace(Vector3d point, Matrix4f modelViewMatrix) {
        Vector4f pos = new Vector4f((float) point.x, (float) point.y, (float) point.z, 1.0f);
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
            Vector3d a, Vector3d b,
            int color,
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
        list.addLine(p1, p2, color);
    }

    public static void drawBoundingBox(
            ImDrawList list, BoundingBox boundingBox, int color,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int windowWidth, int windowHeight
    ) {
        Vector3d[] v = boundingBox.getVertices();

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
                    color,
                    modelViewMatrix, projectionMatrix,
                    windowWidth, windowHeight
            );
        }
    }

    public static void drawBoundingBox2DOutline(
            ImDrawList list, BoundingBox boundingBox, float thickness, int color,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            int windowWidth, int windowHeight
    ) {
        Vector3d[] vertices = boundingBox.getVertices();
        List<ImVec2> pts =  new ArrayList<>();
        Matrix4f modelView = getModelViewMatrix(), projection = getProjectionMatrix();
        for (Vector3d vec3 : vertices) {
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
}