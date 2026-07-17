package pub.frost.utils;

import net.minecraft.entity.Entity;
import pub.frost.utils.wrappers.MinecraftWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class WorldUtils implements MinecraftWrapper {
    public static <T extends Entity> List<T> searchEntity(Class<T> entityType, Predicate<T> predicate) {
        if (mc.theWorld == null) return Collections.emptyList();
        List<T> list = new ArrayList<>();
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entityType.isAssignableFrom(entity.getClass())) {
                T casted = (T) entity;
                if (predicate.test(casted)) list.add(casted);
            }
        }
        return list;
    }
}
