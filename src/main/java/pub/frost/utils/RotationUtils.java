package pub.frost.utils;

import net.minecraft.util.MathHelper;
import org.joml.Vector3d;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.Rotation;

import java.util.*;
import java.util.function.Predicate;

public class RotationUtils {
    public static float wrapYawTo180(float value) {
        value %= 360.0F;
        if (value >= 180.0F) {
            value -= 360.0F;
        }

        if (value < -180.0F) {
            value += 360.0F;
        }

        return value;
    }

    public static float getDirection(float rotationYaw, float moveForward, float moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return rotationYaw;
    }

    public static Rotation getRotationAimingPoint(Vector3d eyePos, Vector3d targetPoint) {
        double yaw, pitch;

        {
            double xDiff = eyePos.x() - targetPoint.x();
            double yDiff = eyePos.y() - targetPoint.y();
            double zDiff = eyePos.z() - targetPoint.z();
            double xyDist = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(zDiff, 2));

            yaw = Math.toDegrees(Math.atan2(zDiff, xDiff)) + 90;
            pitch = 90 - Math.toDegrees(Math.atan2(xyDist, yDiff));
        }

        return new Rotation((float) yaw, (float) pitch);
    }

    public static Rotation searchRotationHittingBoundingBox(Vector3d eyePos, BoundingBox target, Predicate<Rotation> predicate, int maxStep) {
        int currentStep = 0;
        List<BoundingBox> boundingBoxes = Collections.singletonList(target);
        while (currentStep <= maxStep) {
            List<BoundingBox> nextList = new ArrayList<>();
            for (BoundingBox box : boundingBoxes) {
                Vector3d center = box.getCenter();
                Rotation rotation = getRotationAimingPoint(eyePos, center);
                if (predicate.test(rotation)) return rotation;
                else for (Vector3d vertex : box.getVertices()) {
                    Rotation vertexRotation = getRotationAimingPoint(eyePos, vertex);
                    if (predicate.test(vertexRotation)) return vertexRotation;
                    else nextList.add(new BoundingBox(vertex, center));
                }
            }
            boundingBoxes = nextList;
            currentStep ++;
        }

        return null;
    }

    public static Vector3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vector3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }
}
