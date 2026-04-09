package pub.frost.utils.data;

import org.joml.Vector3d;

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
                minX + getSizeX(),
                minY + getSizeY(),
                minZ + getSizeZ()
        );
    }
}
