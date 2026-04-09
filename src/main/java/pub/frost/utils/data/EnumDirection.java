package pub.frost.utils.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3i;

@Getter
@RequiredArgsConstructor
public enum EnumDirection {
    DOWN(0, 1, new Vector3i(0, -1, 0)),
    UP(1, 0, new Vector3i(0, 1, 0)),
    NORTH(2, 3, new Vector3i(0, 0, -1)),
    SOUTH(3, 2, new Vector3i(0, 0, 1)),
    WEST(4, 5, new Vector3i(-1, 0, 0)),
    EAST(5, 4, new Vector3i(1, 0, 0)),;

    final int index;
    final int oppositeIndex;
    final Vector3i normalizedVec;

    public EnumDirection getOpposite() {
        return EnumDirection.values()[getOppositeIndex()];
    }

    public static EnumDirection getByIndex(int index) {
        return values()[Math.max(0, Math.min(values().length - 1, index))];
    }
}
