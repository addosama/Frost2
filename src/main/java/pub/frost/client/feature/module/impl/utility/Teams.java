package pub.frost.client.feature.module.impl.utility;

import net.minecraft.entity.Entity;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.module.annotations.Module;
import pub.frost.client.feature.module.api.AbstractModule;
import pub.frost.client.feature.module.api.ModuleCategory;
import pub.frost.utils.EntityUtils;
import pub.frost.utils.FormatUtils;

@Module(
        key = "Teams",
        category = ModuleCategory.UTILITY
)
public class Teams extends AbstractModule {
    private static Teams getInstance() {
        return FrostCore.getInstance().getModuleManager().getModule(Teams.class);
    }

    private boolean test(Entity entity) {
        if (!isEnabled()) return false;
        return FormatUtils.getFirstColor(EntityUtils.tryGetDisplayName(mc.thePlayer)) == FormatUtils.getFirstColor(EntityUtils.tryGetDisplayName(entity));
    }

    public static boolean isTeammate(Entity entity) {
        return getInstance().test(entity);
    }
}
