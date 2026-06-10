package pub.frost.utils.targeting;

import lombok.RequiredArgsConstructor;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.i18n.annotations.TranslationKey;

import java.util.function.Predicate;

@RequiredArgsConstructor
@TranslationKey("strings.enum.targeting.targets.entities.~")
public enum EnumEntityTarget implements Named {
    PLAYERS(EntityPlayer.class, "players"),
    ANIMALS(EntityAnimal.class, "animals"),
    MOBS(EntityMob.class, "mobs"),
    VILLAGERS(EntityVillager.class, "villagers"),
    OTHER(
            (Predicate<Class<?>>) c -> !(PLAYERS.isTarget(c) || ANIMALS.isTarget(c) || MOBS.isTarget(c) || VILLAGERS.isTarget(c)),
            "other"
    ),;

    final Predicate<Class<?>> predicate;
    final String key;

    EnumEntityTarget(Class<?> targetClass, String key) {
        this((Predicate<Class<?>>) targetClass::isAssignableFrom, key);
    }

    public boolean isTarget(Class<?> clazz) {
        return predicate.test(clazz);
    }

    @Override
    public String toString() {
        return key;
    }
}
