package pub.frost.utils;

import org.joml.Vector3d;
import pub.frost.utils.data.BoundingBox;
import pub.frost.utils.data.EnumDirection;

import java.util.ArrayList;
import java.util.List;

public class BoundingBoxUtils {
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
}
