package pub.frost.client.feature.module.impl.combat.killaura;

import lombok.Getter;
import net.minecraft.entity.Entity;
import pub.frost.client.feature.module.annotations.SubModule;
import pub.frost.client.feature.module.api.AbstractSubModule;
import pub.frost.client.feature.module.impl.combat.KillAura;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.WorldUtils;

import java.util.Collections;
import java.util.List;

@SubModule(KillAura.class)
public class KillAuraSearching extends AbstractSubModule<KillAura> {
    @Getter
    private List<Entity> lastSearchResult = Collections.emptyList();

    public void tick() {
        lastSearchResult = WorldUtils.searchEntity(
                Entity.class,
                e -> getParent().targeting.isTarget(e, EntityUtils.getPositionEyes(mc.thePlayer, 1))
        );
    }
}
