package pub.frost.utils.data;

import org.joml.Vector3d;
import pub.frost.utils.VecUtils;
import pub.frost.utils.data.raytrace.HitResult;

import static pub.frost.utils.MathUtils.same;

public class BoundingBox {
    public final double minX, minY, minZ, maxX, maxY, maxZ;

    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }
    public BoundingBox(Vector3d vec1, Vector3d vec2) {
        this(
                vec1.x(), vec1.y(), vec1.z(),
                vec2.x(), vec2.y(), vec2.z()
        );
    }
    public BoundingBox(Vector3d center, double xExpand, double yExpand, double zExpand) {
        this(
                center.x - xExpand, center.y - yExpand, center.z - zExpand,
                center.x + xExpand, center.y + yExpand, center.z + zExpand
        );
    }

    public Vector3d getMinVector() {
        return new Vector3d(minX, minY, minZ);
    }
    public Vector3d getMaxVector() {
        return new Vector3d(maxX, maxY, maxZ);
    }
    
    public Vector3d[] getVertices() {
        return new Vector3d[] {
                new Vector3d(minX, minY, minZ),
                new Vector3d(minX, minY, maxZ),
                new Vector3d(maxX, minY, maxZ),
                new Vector3d(maxX, minY, minZ),
                new Vector3d(minX, maxY, minZ),
                new Vector3d(minX, maxY, maxZ),
                new Vector3d(maxX, maxY, maxZ),
                new Vector3d(maxX, maxY, minZ)
        };
    }

    public double getSizeX() {
        return maxX - minX;
    }
    public double getSizeY() {
        return maxY - minY;
    }
    public double getSizeZ() {
        return maxZ - minZ;
    }

    public Vector3d getCenter() {
        return new Vector3d(
                minX + getSizeX() * 0.5D,
                minY + getSizeY() * 0.5D,
                minZ + getSizeZ() * 0.5D
        );
    }

    public BoundingBox move(double x, double y, double z) {
        return new BoundingBox(
                minX + x,
                minY + y,
                minZ + z,
                maxX + x,
                maxY + y,
                maxZ + z
        );
    }
    public BoundingBox move(Vector3d vec) {
        return move(vec.x(), vec.y(), vec.z());
    }
    public BoundingBox addCoord(double x, double y, double z) {
        double d = this.minX;
        double e = this.minY;
        double f = this.minZ;
        double g = this.maxX;
        double h = this.maxY;
        double i = this.maxZ;
        if (x < 0.0D) {
            d += x;
        } else if (x > 0.0D) {
            g += x;
        }

        if (y < 0.0D) {
            e += y;
        } else if (y > 0.0D) {
            h += y;
        }

        if (z < 0.0D) {
            f += z;
        } else if (z > 0.0D) {
            i += z;
        }

        return new BoundingBox(d, e, f, g, h, i);
    }
    public BoundingBox expand(double x, double y, double z) {
        double d = this.minX - x;
        double e = this.minY - y;
        double f = this.minZ - z;
        double g = this.maxX + x;
        double h = this.maxY + y;
        double i = this.maxZ + z;
        return new BoundingBox(d, e, f, g, h, i);
    }

    public HitResult calculateIntercept(Vector3d vecA, Vector3d vecB) {
        Vector3d vec3 = VecUtils.getIntermediateWithXValue(vecA, vecB, this.minX);
        Vector3d vec32 = VecUtils.getIntermediateWithXValue(vecA, vecB, this.maxX);
        Vector3d vec33 = VecUtils.getIntermediateWithYValue(vecA, vecB, this.minY);
        Vector3d vec34 = VecUtils.getIntermediateWithYValue(vecA, vecB, this.maxY);
        Vector3d vec35 = VecUtils.getIntermediateWithZValue(vecA, vecB, this.minZ);
        Vector3d vec36 = VecUtils.getIntermediateWithZValue(vecA, vecB, this.maxZ);
        Vector3d vec37 = null;
        
        if (!this.isVecInYZ(vec3)) {
            vec3 = null;
        }
        if (!this.isVecInYZ(vec32)) {
            vec32 = null;
        }
        if (!this.isVecInXZ(vec33)) {
            vec33 = null;
        }
        if (!this.isVecInXZ(vec34)) {
            vec34 = null;
        }
        if (!this.isVecInXY(vec35)) {
            vec35 = null;
        }
        if (!this.isVecInXY(vec36)) {
            vec36 = null;
        }
        if (vec3 != null) {
            vec37 = vec3;
        }

        if (vec32 != null && (vec37 == null || vecA.distanceSquared(vec32) < vecA.distanceSquared(vec37))) {
            vec37 = vec32;
        }
        if (vec33 != null && (vec37 == null || vecA.distanceSquared(vec33) < vecA.distanceSquared(vec37))) {
            vec37 = vec33;
        }
        if (vec34 != null && (vec37 == null || vecA.distanceSquared(vec34) < vecA.distanceSquared(vec37))) {
            vec37 = vec34;
        }
        if (vec35 != null && (vec37 == null || vecA.distanceSquared(vec35) < vecA.distanceSquared(vec37))) {
            vec37 = vec35;
        }
        if (vec36 != null && (vec37 == null || vecA.distanceSquared(vec36) < vecA.distanceSquared(vec37))) {
            vec37 = vec36;
        }
        if (vec37 == null) {
            return null;
        } else {
            EnumDirection enumFacing = null;
            if (vec37 == vec3) {
                enumFacing = EnumDirection.WEST;
            } else if (vec37 == vec32) {
                enumFacing = EnumDirection.EAST;
            } else if (vec37 == vec33) {
                enumFacing = EnumDirection.DOWN;
            } else if (vec37 == vec34) {
                enumFacing = EnumDirection.UP;
            } else if (vec37 == vec35) {
                enumFacing = EnumDirection.NORTH;
            } else {
                enumFacing = EnumDirection.SOUTH;
            }

            return HitResult.buildBlockHit(new BlockPosition(0, 0, 0), enumFacing, vec37);
        }
    }

    public boolean isVecInside(Vector3d vec) {
        if (!(vec.x <= this.minX) && !(vec.x >= this.maxX)) {
            if (!(vec.y <= this.minY) && !(vec.y >= this.maxY)) {
                return !(vec.z <= this.minZ) && !(vec.z >= this.maxZ);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    public boolean intersectsWith(BoundingBox other) {
        if (!(other.maxX <= this.minX) && !(other.minX >= this.maxX)) {
            if (!(other.maxY <= this.minY) && !(other.minY >= this.maxY)) {
                return !(other.maxZ <= this.minZ) && !(other.minZ >= this.maxZ);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public Vector3d negDistanceEscape(BoundingBox box) {
        if (!intersectsWith(box)) return new Vector3d();

        return new Vector3d(
                box.minX - this.maxX,
                box.minY - this.maxY,
                box.minZ - this.maxZ
        );
    }
    public Vector3d posDistanceEscape(BoundingBox box) {
        if (!intersectsWith(box)) return new Vector3d();

        return new Vector3d(
                box.maxX - this.minX,
                box.maxY - this.minY,
                box.maxZ - this.minZ
        );
    }
    public Vector3d minDistanceEscape(BoundingBox box) {
        if (!intersectsWith(box)) return new Vector3d();

        Vector3d pos = posDistanceEscape(box);
        Vector3d neg = negDistanceEscape(box);

        return new Vector3d(
                Math.abs(neg.x) < Math.abs(pos.x) ? neg.x : pos.x,
                Math.abs(neg.y) < Math.abs(pos.y) ? neg.y : pos.y,
                Math.abs(neg.z) < Math.abs(pos.z) ? neg.z : pos.z
        );
    }

    public BoundingBox tryMerge(BoundingBox other) {
        // merge on X
        if (same(this.minY, other.minY) && same(this.maxY, other.maxY) &&
                same(this.minZ, other.minZ) && same(this.maxZ, other.maxZ) &&
                this.maxX >= other.minX && other.maxX >= this.minX) {

            return new BoundingBox(
                    Math.min(this.minX, other.minX),
                    this.minY,
                    this.minZ,
                    Math.max(this.maxX, other.maxX),
                    this.maxY,
                    this.maxZ
            );
        }

        // merge on Y
        if (same(this.minX, other.minX) && same(this.maxX, other.maxX) &&
                same(this.minZ, other.minZ) && same(this.maxZ, other.maxZ) &&
                this.maxY >= other.minY && other.maxY >= this.minY) {

            return new BoundingBox(
                    this.minX,
                    Math.min(this.minY, other.minY),
                    this.minZ,
                    this.maxX,
                    Math.max(this.maxY, other.maxY),
                    this.maxZ
            );
        }

        // merge on Z
        if (same(this.minX, other.minX) && same(this.maxX, other.maxX) &&
                same(this.minY, other.minY) && same(this.maxY, other.maxY) &&
                this.maxZ >= other.minZ && other.maxZ >= this.minZ) {

            return new BoundingBox(
                    this.minX,
                    this.minY,
                    Math.min(this.minZ, other.minZ),
                    this.maxX,
                    this.maxY,
                    Math.max(this.maxZ, other.maxZ)
            );
        }

        return null;
    }

    private boolean isVecInYZ(Vector3d vec) {
        if (vec == null) {
            return false;
        } else {
            return vec.y >= this.minY && vec.y <= this.maxY && vec.z >= this.minZ && vec.z <= this.maxZ;
        }
    }
    private boolean isVecInXZ(Vector3d vec) {
        if (vec == null) {
            return false;
        } else {
            return vec.x >= this.minX && vec.x <= this.maxX && vec.z >= this.minZ && vec.z <= this.maxZ;
        }
    }
    private boolean isVecInXY(Vector3d vec) {
        if (vec == null) {
            return false;
        } else {
            return vec.x >= this.minX && vec.x <= this.maxX && vec.y >= this.minY && vec.y <= this.maxY;
        }
    }
}
