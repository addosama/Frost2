package pub.frost.utils;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
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

    private static float[] getRotation(Vec3 eyePos, Vec3 targetPoint) {
        double yaw, pitch;
        {
            double xDiff = eyePos.xCoord - targetPoint.xCoord;
            double yDiff = eyePos.yCoord - targetPoint.yCoord;
            double zDiff = eyePos.zCoord - targetPoint.zCoord;
            double xyDist = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(zDiff, 2));

            yaw = Math.toDegrees(Math.atan2(zDiff, xDiff)) + 90;
            pitch = 90 - Math.toDegrees(Math.atan2(xyDist, yDiff));
        }
        return new float[]{(float) yaw, (float) pitch};
    }

    public static Rotation getRotationAimingPoint(Vec3 eyePos, Vec3 targetPoint) {
        double yaw, pitch;

        {
            double xDiff = eyePos.xCoord - targetPoint.xCoord;
            double yDiff = eyePos.yCoord - targetPoint.yCoord;
            double zDiff = eyePos.zCoord - targetPoint.zCoord;
            double xyDist = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(zDiff, 2));

            yaw = Math.toDegrees(Math.atan2(zDiff, xDiff)) + 90;
            pitch = 90 - Math.toDegrees(Math.atan2(xyDist, yDiff));
        }

        return new Rotation((float) yaw, (float) pitch);
    }
    public static Rotation getRotationDeltaAimingPoint(Vec3 eyePos, Rotation rotation, Vec3 targetPoint) {
        float[] rotAimingPoint = getRotation(eyePos, targetPoint);
        return new Rotation(
                wrapYawTo180(rotAimingPoint[0]) - wrapYawTo180(rotation.getYaw()),
                rotation.getPitch() - rotAimingPoint[1]
        );
    }

    public static Rotation searchRotationHittingBoundingBox(Vec3 eyePos, AxisAlignedBB target, Predicate<Rotation> predicate, int maxStep) {
        int currentStep = 0;
        List<AxisAlignedBB> boundingBoxes = Collections.singletonList(target);
        while (currentStep <= maxStep) {
            List<AxisAlignedBB> nextList = new ArrayList<>();
            for (AxisAlignedBB box : boundingBoxes) {
                Vec3 center = BoundingBoxUtils.getCenter(box);
                Rotation rotation = getRotationAimingPoint(eyePos, center);
                if (predicate.test(rotation)) return rotation;
                else for (Vec3 vertex : BoundingBoxUtils.getVertices(box)) {
                    Rotation vertexRotation = getRotationAimingPoint(eyePos, vertex);
                    if (predicate.test(vertexRotation)) return vertexRotation;
                    else nextList.add(BoundingBoxUtils.createBox(vertex, center));
                }
            }
            boundingBoxes = nextList;
            currentStep ++;
        }

        return null;
    }

    public static Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }
}
