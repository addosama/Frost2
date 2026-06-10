package pub.frost.utils.raycast;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.AbstractMap;
import java.util.Map;

public class RayCastUtils {
    public static Map.Entry<Boolean, Vec3> getSimpleHitResult(Vec3 eyePos, float rotYaw, float rotPitch, AxisAlignedBB target) {
        // yaw / pitch -> direction
        double yawRad = Math.toRadians(rotYaw);
        double pitchRad = Math.toRadians(rotPitch);

        double dirX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dirY = -Math.sin(pitchRad);
        double dirZ =  Math.cos(yawRad) * Math.cos(pitchRad);

        // Ray-AABB intersection (slab method)
        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        // X
        if (Math.abs(dirX) < 1e-9) {
            if (eyePos.xCoord < target.minX || eyePos.xCoord > target.maxX) {
                return new AbstractMap.SimpleEntry<>(false, null);
            }
        } else {
            double tx1 = (target.minX - eyePos.xCoord) / dirX;
            double tx2 = (target.maxX - eyePos.xCoord) / dirX;
            tMin = Math.max(tMin, Math.min(tx1, tx2));
            tMax = Math.min(tMax, Math.max(tx1, tx2));
        }

        // Y
        if (Math.abs(dirY) < 1e-9) {
            if (eyePos.yCoord < target.minY || eyePos.yCoord > target.maxY) {
                return new AbstractMap.SimpleEntry<>(false, null);
            }
        } else {
            double ty1 = (target.minY - eyePos.yCoord) / dirY;
            double ty2 = (target.maxY - eyePos.yCoord) / dirY;
            tMin = Math.max(tMin, Math.min(ty1, ty2));
            tMax = Math.min(tMax, Math.max(ty1, ty2));
        }

        // Z
        if (Math.abs(dirZ) < 1e-9) {
            if (eyePos.zCoord < target.minZ || eyePos.zCoord > target.maxZ) {
                return new AbstractMap.SimpleEntry<>(false, null);
            }
        } else {
            double tz1 = (target.minZ - eyePos.zCoord) / dirZ;
            double tz2 = (target.maxZ - eyePos.zCoord) / dirZ;
            tMin = Math.max(tMin, Math.min(tz1, tz2));
            tMax = Math.min(tMax, Math.max(tz1, tz2));
        }

        // No hit
        if (tMax < tMin || tMax < 0) {
            return new AbstractMap.SimpleEntry<>(false, null);
        }

        // If inside box, use exit point; otherwise first entry point
        double tHit = tMin >= 0 ? tMin : tMax;

        Vec3 hitVec = new Vec3(
                eyePos.xCoord + dirX * tHit,
                eyePos.yCoord + dirY * tHit,
                eyePos.zCoord + dirZ * tHit
        );

        return new AbstractMap.SimpleEntry<>(true, hitVec);
    }

    public static MovingObjectPosition raytraceBlocks(World world, Vec3 eyePos, Vec3 lookVec, double blockReachDistance) {
        Vec3 vec32 = eyePos.add(new Vec3(
                lookVec.xCoord * blockReachDistance,
                lookVec.yCoord * blockReachDistance,
                lookVec.zCoord * blockReachDistance
        ));
        return world.rayTraceBlocks(
                eyePos, vec32, false, false, true
        );
    }
}
