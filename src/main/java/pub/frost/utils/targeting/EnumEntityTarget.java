package pub.frost.utils.targeting;

import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.wrappers.shared.entity.*;

import java.util.function.Predicate;

public enum EnumEntityTarget implements Named {
    PLAYERS(WEntityPlayer.class, "players"),
    ANIMALS(WEntityAnimal.class, "animals"),
    MOBS(WEntityMob.class, "mobs"),
    VILLAGERS(WEntityVillager.class, "villagers"),
    OTHER(
            c -> !(PLAYERS.isTarget(c) || ANIMALS.isTarget(c) || MOBS.isTarget(c) || VILLAGERS.isTarget(c)),
            "other"
    ),;

    final String key;
    final Predicate<Class<?>> predicate;

    EnumEntityTarget(Predicate<Class<?>> predicate, String key) {
        this.key = "targets.entities." + key;
        this.predicate = predicate;
    }
    EnumEntityTarget(Class<? extends WEntity> entityWrapper, String key) {
        this(c -> FrostCore.getInstance().getWrapperManager().getWrapper(entityWrapper).isTarget(c), key);
    }

    public boolean isTarget(Class<?> clazz) {
        return predicate.test(clazz);
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public String getName() {
        return FrostCore.getLocalizer().get("strings." + this + ".name");
    }
}
