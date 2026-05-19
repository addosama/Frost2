package pub.frost.utils;

import org.joml.Vector3d;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.utils.data.BlockPosition;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.EnumDirection;
import pub.frost.utils.data.raytrace.HitResult;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxUtils implements Wrappers {
    private static final double EPSILON = 1.0E-7D;

    public static Vector3d getFaceCenter(BoundingBox box, EnumDirection direction) {
        return box.getCenter().add(
                new Vector3d(direction.getNormalizedVec()).mul(
                        box.getSizeX() / 2, box.getSizeY() / 2, box.getSizeZ() / 2
                )
        );
    }

    public static List<EnumDirection> getAllPossibleHitFacesByEyePos(Vector3d eyePos, BoundingBox blockBoundingBox) {
        List<EnumDirection> list = new ArrayList<>();
        if (eyePos.y > blockBoundingBox.maxY) list.add(EnumDirection.UP);
        if (eyePos.y < blockBoundingBox.minY) list.add(EnumDirection.DOWN);
        if (eyePos.z < blockBoundingBox.minZ) list.add(EnumDirection.NORTH);
        if (eyePos.z > blockBoundingBox.maxZ) list.add(EnumDirection.SOUTH);
        if (eyePos.x > blockBoundingBox.maxX) list.add(EnumDirection.EAST);
        if (eyePos.x < blockBoundingBox.minX) list.add(EnumDirection.WEST);
        return list;
    }

    public static List<BoundingBox> mergeBoxes(List<BoundingBox> input) {
        List<BoundingBox> boxes = new ArrayList<>(input);

        boolean changed;
        do {
            changed = false;

            for (int i = 0; i < boxes.size(); i++) {
                BoundingBox a = boxes.get(i);

                for (int j = i + 1; j < boxes.size(); j++) {
                    BoundingBox merged = a.tryMerge(boxes.get(j));

                    if (merged != null) {
                        boxes.set(i, merged);
                        boxes.remove(j);
                        changed = true;
                        j--; // 防止跳项
                        a = merged;
                    }
                }
            }
        } while (changed);

        return boxes;
    }

    public static HitResult getAreaHitResult(
            BoundingBox box1, BoundingBox box2,
            Object entity,
            Vector3d entityLook, double reachDistance, float tickDelta
    ) {
        Vector3d eyePos = Entity.getPositionEyes(entity, tickDelta);
        Vector3d reachEnd = new Vector3d(eyePos).add(
                entityLook.x() * reachDistance,
                entityLook.y() * reachDistance,
                entityLook.z() * reachDistance
        );

        double maxDistanceSq = reachDistance * reachDistance;
        HitResult lookingObject = EntityUtils.getLookingObject(
                entity, entityLook, reachDistance, tickDelta, false
        );
        if (lookingObject != null
                && lookingObject.getType() == HitResult.EnumHitType.BLOCK
                && lookingObject.getHitVec() != null) {
            maxDistanceSq = Math.min(maxDistanceSq, eyePos.distanceSquared(lookingObject.getHitVec()));
        }

        HitResult hitResult = calculateSweptIntercept(box1, box2, eyePos, reachEnd);
        if (hitResult == null || hitResult.getHitVec() == null) return null;
        if (eyePos.distanceSquared(hitResult.getHitVec()) > maxDistanceSq) return null;
        return hitResult;
    }

    private static HitResult calculateSweptIntercept(BoundingBox box1, BoundingBox box2, Vector3d rayStart, Vector3d rayEnd) {
        List<Plane> planes = getSweptAreaPlanes(box1, box2);
        if (planes.isEmpty()) return null;

        Vector3d ray = new Vector3d(rayEnd).sub(rayStart);
        double enter = 0.0D;
        double exit = 1.0D;
        Plane enterPlane = null;

        for (Plane plane : planes) {
            double distance = plane.distance(rayStart);
            double denom = plane.normal.dot(ray);

            if (Math.abs(denom) < EPSILON) {
                if (distance > EPSILON) return null;
                continue;
            }

            double t = -distance / denom;
            if (denom < 0.0D) {
                if (t > enter) {
                    enter = t;
                    enterPlane = plane;
                }
            } else if (t < exit) {
                exit = t;
            }

            if (enter - exit > EPSILON) return null;
        }

        if (exit < -EPSILON || enter > 1.0D + EPSILON) return null;

        double hitT = Math.max(0.0D, enter);
        Vector3d hitVec = new Vector3d(rayStart).add(new Vector3d(ray).mul(hitT));
        EnumDirection direction = enterPlane == null ? null : getDirectionByNormal(enterPlane.normal);
        return HitResult.buildBlockHit(new BlockPosition(hitVec), direction, hitVec);
    }

    private static List<Plane> getSweptAreaPlanes(BoundingBox box1, BoundingBox box2) {
        Vector3d[] box1Vertices = box1.getVertices();
        Vector3d[] box2Vertices = box2.getVertices();
        Vector3d[] vertices = new Vector3d[box1Vertices.length + box2Vertices.length];
        System.arraycopy(box1Vertices, 0, vertices, 0, box1Vertices.length);
        System.arraycopy(box2Vertices, 0, vertices, box1Vertices.length, box2Vertices.length);

        List<Plane> planes = new ArrayList<>();
        for (int i = 0; i < vertices.length - 2; i++) {
            for (int j = i + 1; j < vertices.length - 1; j++) {
                for (int k = j + 1; k < vertices.length; k++) {
                    Plane plane = buildHullPlane(vertices, i, j, k);
                    if (plane != null && planes.stream().noneMatch(existing -> existing.isSame(plane))) {
                        planes.add(plane);
                    }
                }
            }
        }

        return planes;
    }

    private static Plane buildHullPlane(Vector3d[] vertices, int i, int j, int k) {
        Vector3d normal = new Vector3d(vertices[j]).sub(vertices[i])
                .cross(new Vector3d(vertices[k]).sub(vertices[i]));
        if (normal.lengthSquared() < EPSILON * EPSILON) return null;
        normal.normalize();

        double d = -normal.dot(vertices[i]);
        boolean hasPositive = false;
        boolean hasNegative = false;

        for (Vector3d vertex : vertices) {
            double distance = normal.dot(vertex) + d;
            if (distance > EPSILON) hasPositive = true;
            else if (distance < -EPSILON) hasNegative = true;
            if (hasPositive && hasNegative) return null;
        }

        if (hasPositive) {
            normal.negate();
            d = -d;
        }

        return new Plane(normal, d);
    }

    private static EnumDirection getDirectionByNormal(Vector3d normal) {
        double x = Math.abs(normal.x);
        double y = Math.abs(normal.y);
        double z = Math.abs(normal.z);

        if (x >= y && x >= z) return normal.x > 0.0D ? EnumDirection.EAST : EnumDirection.WEST;
        if (y >= z) return normal.y > 0.0D ? EnumDirection.UP : EnumDirection.DOWN;
        return normal.z > 0.0D ? EnumDirection.SOUTH : EnumDirection.NORTH;
    }

    private static class Plane {
        private final Vector3d normal;
        private final double d;

        private Plane(Vector3d normal, double d) {
            this.normal = normal;
            this.d = d;
        }

        private double distance(Vector3d vec) {
            return normal.dot(vec) + d;
        }

        private boolean isSame(Plane other) {
            return normal.distanceSquared(other.normal) < EPSILON * EPSILON
                    && Math.abs(d - other.d) < EPSILON;
        }
    }
}
