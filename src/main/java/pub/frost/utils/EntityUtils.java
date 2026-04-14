package pub.frost.utils;

import pub.frost.base.wrapping.Wrappers;

public class EntityUtils implements Wrappers {
    public static String tryGetDisplayName(Object entity) {
        return Entity.getDisplayName(entity);
    }
}
