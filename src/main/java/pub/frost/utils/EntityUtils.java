package pub.frost.utils;

import org.joml.Vector3d;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.utils.data.BoundingBox;
import pub.frost.wrappers.shared.item.WItem;

public class EntityUtils implements Wrappers {
    public static String tryGetDisplayName(Object entity) {
        return Entity.getDisplayName(entity);
    }

    public static BoundingBox getBoundingBoxAtPosition(Object entity, Vector3d position) {
        double x = position.x;
        double y = position.y;
        double z = position.z;
        float width = Entity.getWidth(entity);
        float height = Entity.getHeight(entity);

        return new BoundingBox(
                x - width / 2,
                y,
                z - width / 2,
                x + width / 2,
                y + height,
                z + width / 2
        );
    }
    public static boolean isHoldingItem(Object livingEntity, WItem targetItem) {
        Object itemHeld = EntityLivingBase.getHeldItem(livingEntity);
        if (itemHeld == null) return false;
        return targetItem.isTarget(
                ItemStack.getItem(itemHeld)
        );
    }
}
