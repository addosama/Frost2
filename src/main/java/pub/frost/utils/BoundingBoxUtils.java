package pub.frost.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxUtils {
    private static final double EPSILON = 1.0E-7D;

    public static Vec3 getFaceCenter(AxisAlignedBB box, EnumFacing direction) {
        Vec3i vec = direction.getDirectionVec();
        return new Vec3(
                (box.minX + box.maxX) / 2.0 + vec.getX() * (box.maxX - box.minX) / 2.0,
                (box.minY + box.maxY) / 2.0 + vec.getY() * (box.maxY - box.minY) / 2.0,
                (box.minZ + box.maxZ) / 2.0 + vec.getZ() * (box.maxZ - box.minZ) / 2.0
        );
    }

    public static AxisAlignedBB getFaceBoundingBox(AxisAlignedBB box, EnumFacing direction) {
        switch (direction) {
            case DOWN  : return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
            case UP    : return new AxisAlignedBB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
            case NORTH : return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
            case SOUTH : return new AxisAlignedBB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
            case WEST  : return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
            case EAST  : return new AxisAlignedBB(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
            default    : return box;
        }
    }

    public static List<EnumFacing> getAllPossibleHitFacesByEyePos(Vec3 eyePos, AxisAlignedBB bb) {
        List<EnumFacing> list = new ArrayList<>();
        if (eyePos.yCoord > bb.maxY) list.add(EnumFacing.UP);
        if (eyePos.yCoord < bb.minY) list.add(EnumFacing.DOWN);
        if (eyePos.zCoord < bb.minZ) list.add(EnumFacing.NORTH);
        if (eyePos.zCoord > bb.maxZ) list.add(EnumFacing.SOUTH);
        if (eyePos.xCoord > bb.maxX) list.add(EnumFacing.EAST);
        if (eyePos.xCoord < bb.minX) list.add(EnumFacing.WEST);
        return list;
    }

    public static List<AxisAlignedBB> mergeBoxes(List<AxisAlignedBB> input) {
        List<AxisAlignedBB> boxes = new ArrayList<>(input);
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < boxes.size(); i++) {
                AxisAlignedBB a = boxes.get(i);
                for (int j = i + 1; j < boxes.size(); j++) {
                    AxisAlignedBB merged = tryMerge(a, boxes.get(j));
                    if (merged != null) {
                        boxes.set(i, merged);
                        boxes.remove(j);
                        changed = true;
                        j--;
                        a = merged;
                    }
                }
            }
        } while (changed);
        return boxes;
    }

    private static AxisAlignedBB tryMerge(AxisAlignedBB a, AxisAlignedBB b) {
        if (same(a.minY, b.minY) && same(a.maxY, b.maxY) && same(a.minZ, b.minZ) && same(a.maxZ, b.maxZ)
                && a.maxX >= b.minX && b.maxX >= a.minX)
            return new AxisAlignedBB(Math.min(a.minX, b.minX), a.minY, a.minZ, Math.max(a.maxX, b.maxX), a.maxY, a.maxZ);
        if (same(a.minX, b.minX) && same(a.maxX, b.maxX) && same(a.minZ, b.minZ) && same(a.maxZ, b.maxZ)
                && a.maxY >= b.minY && b.maxY >= a.minY)
            return new AxisAlignedBB(a.minX, Math.min(a.minY, b.minY), a.minZ, a.maxX, Math.max(a.maxY, b.maxY), a.maxZ);
        if (same(a.minX, b.minX) && same(a.maxX, b.maxX) && same(a.minY, b.minY) && same(a.maxY, b.maxY)
                && a.maxZ >= b.minZ && b.maxZ >= a.minZ)
            return new AxisAlignedBB(a.minX, a.minY, Math.min(a.minZ, b.minZ), a.maxX, a.maxY, Math.max(a.maxZ, b.maxZ));
        return null;
    }

    private static boolean same(double a, double b) { return Math.abs(a - b) < 1e-9; }

    public static MovingObjectPosition getAreaHitResult(
            AxisAlignedBB box1, AxisAlignedBB box2,
            Entity entity,
            Vec3 entityLook, double reachDistance, float tickDelta
    ) {
        Vec3 eyePos = EntityUtils.getPositionEyes(entity, tickDelta);
        Vec3 reachEnd = eyePos.addVector(
                entityLook.xCoord * reachDistance,
                entityLook.yCoord * reachDistance,
                entityLook.zCoord * reachDistance
        );

        double maxDistanceSq = reachDistance * reachDistance;
        MovingObjectPosition lookingObject = EntityUtils.getLookingObject(
                entity, entityLook, reachDistance, tickDelta, false
        );
        if (lookingObject != null
                && lookingObject.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                && lookingObject.hitVec != null) {
            maxDistanceSq = Math.min(maxDistanceSq, eyePos.squareDistanceTo(lookingObject.hitVec));
        }

        MovingObjectPosition hitResult = calculateSweptIntercept(box1, box2, eyePos, reachEnd);
        if (hitResult == null || hitResult.hitVec == null) return null;
        if (eyePos.squareDistanceTo(hitResult.hitVec) > maxDistanceSq) return null;
        return hitResult;
    }

    private static MovingObjectPosition calculateSweptIntercept(
            AxisAlignedBB box1, AxisAlignedBB box2, Vec3 rayStart, Vec3 rayEnd
    ) {
        List<Plane> planes = getSweptAreaPlanes(box1, box2);
        if (planes.isEmpty()) return null;

        Vec3 ray = VecUtils.subtract(rayEnd, rayStart);
        double enter = 0.0D, exit = 1.0D;
        Plane enterPlane = null;

        for (Plane plane : planes) {
            double d = plane.distance(rayStart);
            double denom = VecUtils.dot(plane.normal, ray);
            if (Math.abs(denom) < EPSILON) {
                if (d > EPSILON) return null;
                continue;
            }
            double t = -d / denom;
            if (denom < 0.0D) { if (t > enter) { enter = t; enterPlane = plane; } }
            else if (t < exit) { exit = t; }
            if (enter - exit > EPSILON) return null;
        }
        if (exit < -EPSILON || enter > 1.0D + EPSILON) return null;

        double hitT = Math.max(0.0D, enter);
        Vec3 hitVec = rayStart.addVector(ray.xCoord * hitT, ray.yCoord * hitT, ray.zCoord * hitT);
        EnumFacing side = enterPlane == null ? null : getDirectionByNormal(enterPlane.normal);
        return new MovingObjectPosition(hitVec, side, new BlockPos(
                (int) Math.floor(hitVec.xCoord), (int) Math.floor(hitVec.yCoord), (int) Math.floor(hitVec.zCoord)));
    }

    private static List<Plane> getSweptAreaPlanes(AxisAlignedBB box1, AxisAlignedBB box2) {
        Vec3[] v1 = getVertices(box1);
        Vec3[] v2 = getVertices(box2);
        Vec3[] vertices = new Vec3[v1.length + v2.length];
        System.arraycopy(v1, 0, vertices, 0, v1.length);
        System.arraycopy(v2, 0, vertices, v1.length, v2.length);

        List<Plane> planes = new ArrayList<>();
        for (int i = 0; i < vertices.length - 2; i++)
            for (int j = i + 1; j < vertices.length - 1; j++)
                for (int k = j + 1; k < vertices.length; k++) {
                    Plane plane = buildHullPlane(vertices, i, j, k);
                    if (plane != null && planes.stream().noneMatch(ex -> ex.isSame(plane)))
                        planes.add(plane);
                }
        return planes;
    }

    private static Plane buildHullPlane(Vec3[] vertices, int i, int j, int k) {
        Vec3 a = VecUtils.subtract(vertices[j], vertices[i]);
        Vec3 b = VecUtils.subtract(vertices[k], vertices[i]);
        Vec3 normal = VecUtils.cross(a, b);
        double lenSq = normal.xCoord * normal.xCoord + normal.yCoord * normal.yCoord + normal.zCoord * normal.zCoord;
        if (lenSq < EPSILON * EPSILON) return null;
        normal = VecUtils.scale(normal, 1.0 / Math.sqrt(lenSq));

        double d = -VecUtils.dot(normal, vertices[i]);
        boolean hasPos = false, hasNeg = false;
        for (Vec3 v : vertices) {
            double dist = VecUtils.dot(normal, v) + d;
            if (dist > EPSILON) hasPos = true;
            else if (dist < -EPSILON) hasNeg = true;
            if (hasPos && hasNeg) return null;
        }
        if (hasPos) { normal = VecUtils.negate(normal); d = -d; }
        return new Plane(normal, d);
    }

    private static EnumFacing getDirectionByNormal(Vec3 n) {
        double ax = Math.abs(n.xCoord), ay = Math.abs(n.yCoord), az = Math.abs(n.zCoord);
        if (ax >= ay && ax >= az) return n.xCoord > 0 ? EnumFacing.EAST : EnumFacing.WEST;
        if (ay >= az) return n.yCoord > 0 ? EnumFacing.UP : EnumFacing.DOWN;
        return n.zCoord > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
    }

    public static Vec3 getCenter(AxisAlignedBB box) {
        return new Vec3(
                (box.minX + box.maxX) / 2.0,
                (box.minY + box.maxY) / 2.0,
                (box.minZ + box.maxZ) / 2.0
        );
    }

    public static Vec3[] getVertices(AxisAlignedBB box) {
        return new Vec3[]{
                new Vec3(box.minX, box.minY, box.minZ),
                new Vec3(box.minX, box.minY, box.maxZ),
                new Vec3(box.minX, box.maxY, box.minZ),
                new Vec3(box.minX, box.maxY, box.maxZ),
                new Vec3(box.maxX, box.minY, box.minZ),
                new Vec3(box.maxX, box.minY, box.maxZ),
                new Vec3(box.maxX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.maxY, box.maxZ)
        };
    }

    public static AxisAlignedBB createBox(Vec3 a, Vec3 b) {
        return new AxisAlignedBB(
                Math.min(a.xCoord, b.xCoord), Math.min(a.yCoord, b.yCoord), Math.min(a.zCoord, b.zCoord),
                Math.max(a.xCoord, b.xCoord), Math.max(a.yCoord, b.yCoord), Math.max(a.zCoord, b.zCoord)
        );
    }

    public static AxisAlignedBB move(AxisAlignedBB bb, double x, double y, double z) {
        return new AxisAlignedBB(
                bb.minX + x,
                bb.minY + y,
                bb.minZ + z,
                bb.maxX + x,
                bb.maxY + y,
                bb.maxZ + z
        );
    }
    public static AxisAlignedBB move(AxisAlignedBB bb, Vec3 vec) {
        return move(bb, vec.xCoord, vec.yCoord, vec.zCoord);
    }

    public static AxisAlignedBB lerp(AxisAlignedBB prev, AxisAlignedBB bb, float delta) {
        return new AxisAlignedBB(
                MathUtils.lerp(prev.minX, bb.minX, delta),
                MathUtils.lerp(prev.minY, bb.minY, delta),
                MathUtils.lerp(prev.minZ, bb.minZ, delta),
                MathUtils.lerp(prev.maxX, bb.maxX, delta),
                MathUtils.lerp(prev.maxY, bb.maxY, delta),
                MathUtils.lerp(prev.maxZ, bb.maxZ, delta)
        );
    }

    public static Vec3 getMinVector(AxisAlignedBB bb) {
        return new Vec3(bb.minX, bb.minY, bb.minZ);
    }
    public static Vec3 getMaxVector(AxisAlignedBB bb) {
        return new Vec3(bb.maxX, bb.maxY, bb.maxZ);
    }

    public static double getSizeX(AxisAlignedBB bb) {
        return bb.maxX - bb.minX;
    }
    public static double getSizeY(AxisAlignedBB bb) {
        return bb.maxY - bb.minY;
    }
    public static double getSizeZ(AxisAlignedBB bb) {
        return bb.maxZ - bb.minZ;
    }

    private static class Plane {
        final Vec3 normal;
        final double d;
        Plane(Vec3 n, double d) { this.normal = n; this.d = d; }
        double distance(Vec3 v) { return VecUtils.dot(normal, v) + d; }
        boolean isSame(Plane o) { return Math.abs(VecUtils.dot(normal, o.normal) - 1.0) < EPSILON && Math.abs(d - o.d) < EPSILON; }
    }
}