package pub.frost.utils.raycast;

import org.joml.Vector3d;
import pub.frost.utils.data.BoundingBox;

import java.util.AbstractMap;
import java.util.Map;

public class RaycastUtils {
    public static Map.Entry<Boolean, Vector3d> raycast(Vector3d eyePos, float rotYaw, float rotPitch, BoundingBox target) {
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
            if (eyePos.x < target.minX || eyePos.x > target.maxX) {
                return new AbstractMap.SimpleEntry<>(false, null);
            }
        } else {
            double tx1 = (target.minX - eyePos.x) / dirX;
            double tx2 = (target.maxX - eyePos.x) / dirX;
            tMin = Math.max(tMin, Math.min(tx1, tx2));
            tMax = Math.min(tMax, Math.max(tx1, tx2));
        }

        // Y
        if (Math.abs(dirY) < 1e-9) {
            if (eyePos.y < target.minY || eyePos.y > target.maxY) {
                return new AbstractMap.SimpleEntry<>(false, null);
            }
        } else {
            double ty1 = (target.minY - eyePos.y) / dirY;
            double ty2 = (target.maxY - eyePos.y) / dirY;
            tMin = Math.max(tMin, Math.min(ty1, ty2));
            tMax = Math.min(tMax, Math.max(ty1, ty2));
        }

        // Z
        if (Math.abs(dirZ) < 1e-9) {
            if (eyePos.z < target.minZ || eyePos.z > target.maxZ) {
                return new AbstractMap.SimpleEntry<>(false, null);
            }
        } else {
            double tz1 = (target.minZ - eyePos.z) / dirZ;
            double tz2 = (target.maxZ - eyePos.z) / dirZ;
            tMin = Math.max(tMin, Math.min(tz1, tz2));
            tMax = Math.min(tMax, Math.max(tz1, tz2));
        }

        // No hit
        if (tMax < tMin || tMax < 0) {
            return new AbstractMap.SimpleEntry<>(false, null);
        }

        // If inside box, use exit point; otherwise first entry point
        double tHit = tMin >= 0 ? tMin : tMax;

        Vector3d hitVec = new Vector3d(
                eyePos.x + dirX * tHit,
                eyePos.y + dirY * tHit,
                eyePos.z + dirZ * tHit
        );

        return new AbstractMap.SimpleEntry<>(true, hitVec);
    }
}
