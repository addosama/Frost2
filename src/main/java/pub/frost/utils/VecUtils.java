package pub.frost.utils;

import org.joml.Vector3d;

public class VecUtils {
    /**
     * Returns a new vector with x value equal to the second parameter, along the line between this vector and the passed in vector, or null if not possible.
     */
    public static Vector3d getIntermediateWithXValue(Vector3d input, Vector3d vec, double x) {
        double d0 = vec.x - input.x;
        double d1 = vec.y - input.y;
        double d2 = vec.z - input.z;
        if (d0 * d0 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (x - input.x) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(input.x + d0 * d3, input.y + d1 * d3, input.z + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with y value equal to the second parameter, along the line between this vector and the passed in vector, or null if not possible.
     */
    public static Vector3d getIntermediateWithYValue(Vector3d input, Vector3d vec, double y) {
        double d0 = vec.x - input.x;
        double d1 = vec.y - input.y;
        double d2 = vec.z - input.z;
        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (y - input.y) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(input.x + d0 * d3, input.y + d1 * d3, input.z + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with z value equal to the second parameter, along the line between this vector and the passed in vector, or null if not possible.
     */
    public static Vector3d getIntermediateWithZValue(Vector3d input, Vector3d vec, double z) {
        double d0 = vec.x - input.x;
        double d1 = vec.y - input.y;
        double d2 = vec.z - input.z;
        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (z - input.z) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(input.x + d0 * d3, input.y + d1 * d3, input.z + d2 * d3) : null;
        }
    }
}
