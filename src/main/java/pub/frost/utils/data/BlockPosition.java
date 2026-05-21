package pub.frost.utils.data;

import org.joml.Vector3dc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public class BlockPosition extends Vector3i {
    public BlockPosition(int x, int y, int z) {
        super(x, y, z);
    }
    public BlockPosition(double x, double y, double z) {
        super((int) x, (int) y, (int) z);
    }
    public BlockPosition(Vector3dc vec) {
        this(vec.x(), vec.y(), vec.z());
    }
    public BlockPosition(Vector3ic vec3i) {
        super(vec3i);
    }

    public BlockPosition offset(EnumDirection direction, int value) {
        this.add(direction.getNormalizedVec().mul(value, new Vector3i()));
        return this;
    }
    public BlockPosition offset(EnumDirection direction) {
        return this.offset(direction, 1);
    }
}
