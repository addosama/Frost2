package pub.frost.utils;

import imgui.ImVec2;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VecUtils {
    /**
     * Returns a new vector with x value equal to the second parameter, along the line between this vector and the passed in vector, or null if not possible.
     */
    public static Vec3 getIntermediateWithXValue(Vec3 input, Vec3 vec, double x) {
        double d0 = vec.xCoord - input.xCoord;
        double d1 = vec.yCoord - input.yCoord;
        double d2 = vec.zCoord - input.zCoord;
        if (d0 * d0 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (x - input.xCoord) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(input.xCoord + d0 * d3, input.yCoord + d1 * d3, input.zCoord + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with y value equal to the second parameter, along the line between this vector and the passed in vector, or null if not possible.
     */
    public static Vec3 getIntermediateWithYValue(Vec3 input, Vec3 vec, double y) {
        double d0 = vec.xCoord - input.xCoord;
        double d1 = vec.yCoord - input.yCoord;
        double d2 = vec.zCoord - input.zCoord;
        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (y - input.yCoord) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(input.xCoord + d0 * d3, input.yCoord + d1 * d3, input.zCoord + d2 * d3) : null;
        }
    }

    /**
     * Returns a new vector with z value equal to the second parameter, along the line between this vector and the passed in vector, or null if not possible.
     */
    public static Vec3 getIntermediateWithZValue(Vec3 input, Vec3 vec, double z) {
        double d0 = vec.xCoord - input.xCoord;
        double d1 = vec.yCoord - input.yCoord;
        double d2 = vec.zCoord - input.zCoord;
        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (z - input.zCoord) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vec3(input.xCoord + d0 * d3, input.yCoord + d1 * d3, input.zCoord + d2 * d3) : null;
        }
    }

    public static float cross(ImVec2 o, ImVec2 a, ImVec2 b) {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
    }

    public static List<ImVec2> convexHull(ImVec2... pts) {
        return convexHull(Arrays.asList(pts));
    }
    public static List<ImVec2> convexHull(List<ImVec2> pts) {
        int n = pts.size();
        if (n <= 3) return new ArrayList<>(pts);

        pts.sort((a, b) -> {
            if (a.x == b.x) return Float.compare(a.y, b.y);
            return Float.compare(a.x, b.x);
        });

        List<ImVec2> hull = new ArrayList<>();

        for (ImVec2 p : pts) {
            while (hull.size() >= 2 &&
                    cross(hull.get(hull.size() - 2),
                            hull.get(hull.size() - 1),
                            p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        int t = hull.size() + 1;
        for (int i = n - 2; i >= 0; i--) {
            ImVec2 p = pts.get(i);
            while (hull.size() >= t &&
                    cross(hull.get(hull.size() - 2),
                            hull.get(hull.size() - 1),
                            p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        hull.remove(hull.size() - 1);
        return hull;
    }

    // Vec3 math helpers (Minecraft 1.8 Vec3 lacks these)
    public static Vec3 subtract(Vec3 a, Vec3 b) { return new Vec3(a.xCoord - b.xCoord, a.yCoord - b.yCoord, a.zCoord - b.zCoord); }

    public static Vec3 cross(Vec3 a, Vec3 b) {
        return new Vec3(
                a.yCoord * b.zCoord - a.zCoord * b.yCoord,
                a.zCoord * b.xCoord - a.xCoord * b.zCoord,
                a.xCoord * b.yCoord - a.yCoord * b.xCoord);
    }

    public static double dot(Vec3 a, Vec3 b) { return a.xCoord * b.xCoord + a.yCoord * b.yCoord + a.zCoord * b.zCoord; }

    public static Vec3 scale(Vec3 v, double s) { return new Vec3(v.xCoord * s, v.yCoord * s, v.zCoord * s); }

    public static Vec3 negate(Vec3 v) { return new Vec3(-v.xCoord, -v.yCoord, -v.zCoord); }
}
