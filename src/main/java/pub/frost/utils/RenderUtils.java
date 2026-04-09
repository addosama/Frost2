package pub.frost.utils;

import imgui.ImVec2;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector4f;
import java.nio.FloatBuffer;

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
        if (pos.w <= 0.05f) return null;

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
}