package pub.frost.utils;

import org.joml.Vector3d;
import pub.frost.utils.data.Rotation;

public class RotationUtils {
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
}
