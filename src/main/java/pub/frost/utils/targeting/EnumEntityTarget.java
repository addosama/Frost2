package pub.frost.utils.targeting;

import lombok.RequiredArgsConstructor;
import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.wrappers.shared.entity.*;

import java.util.function.Predicate;

@RequiredArgsConstructor
@TranslationKey("strings.enum.targeting.targets.entities.~")
public enum EnumEntityTarget implements Named {
    PLAYERS(Wrappers.EntityPlayer, "players"),
    ANIMALS(Wrappers.EntityAnimal, "animals"),
    MOBS(Wrappers.EntityMob, "mobs"),
    VILLAGERS(Wrappers.EntityVillager, "villagers"),
    OTHER(
            (Predicate<Class<?>>) c -> !(PLAYERS.isTarget(c) || ANIMALS.isTarget(c) || MOBS.isTarget(c) || VILLAGERS.isTarget(c)),
            "other"
    ),;

    final Predicate<Class<?>> predicate;
    final String key;

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
