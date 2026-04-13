package pub.frost.utils;

import pub.frost.client.core.FrostCore;
import pub.frost.wrappers.shared.entity.WEntity;
import pub.frost.wrappers.shared.entity.WEntityPlayer;

public class EntityUtils {
    private static final WEntity entityWrapper = FrostCore.getWrapper(WEntity.class);
    private static final WEntityPlayer playerWrapper = FrostCore.getWrapper(WEntityPlayer.class);

    public static String tryGetDisplayName(Object entity) {
        if (playerWrapper.isTarget(entity)) return playerWrapper.getDisplayName(entity);
        else return entityWrapper.getName(entity);
    }
}
