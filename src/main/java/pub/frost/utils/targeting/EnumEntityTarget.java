package pub.frost.utils.targeting;

import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.wrappers.shared.entity.*;

import java.util.function.Predicate;

@TranslationKey("strings.~")
public enum EnumEntityTarget implements Named {
    PLAYERS(Wrappers.EntityPlayer, "players"),
    ANIMALS(Wrappers.EntityAnimal, "animals"),
    MOBS(Wrappers.EntityMob, "mobs"),
    VILLAGERS(Wrappers.EntityVillager, "villagers"),
    OTHER(
            (Predicate<Class<?>>) c -> !(PLAYERS.isTarget(c) || ANIMALS.isTarget(c) || MOBS.isTarget(c) || VILLAGERS.isTarget(c)),
            "other"
    ),;

    final String key;
    final Predicate<Class<?>> predicate;

    EnumEntityTarget(Predicate<Class<?>> predicate, String key) {
        this.key = "targets.entities." + key;
        this.predicate = predicate;
    }
    <T extends WEntity> EnumEntityTarget(T entityWrapper, String key) {
        this((Predicate<Class<?>>) entityWrapper::isTarget, key);
    }

    public boolean isTarget(Class<?> clazz) {
        return predicate.test(clazz);
    }

    @Override
    public String toString() {
        return key;
    }
}
