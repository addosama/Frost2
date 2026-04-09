package pub.frost.utils.data;

import org.joml.Vector3d;
import org.joml.Vector3i;

public class BlockPosition extends Vector3i {
    public BlockPosition(int x, int y, int z) {
        super(x, y, z);
    }

    public BlockPosition(double x, double y, double z) {
        super((int) x, (int) y, (int) z);
    }

    public BlockPosition(Vector3d vec) {
        this(vec.x, vec.y, vec.z);
    }
}
