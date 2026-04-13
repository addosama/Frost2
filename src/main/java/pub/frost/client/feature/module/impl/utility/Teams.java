package pub.frost.client.feature.module.impl.utility;

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

    private boolean test(Object entity) {
        if (!isEnabled()) return false;
        return FormatUtils.getFirstColor(EntityUtils.tryGetDisplayName(mcWrapper.getPlayer(mc))) == FormatUtils.getFirstColor(EntityUtils.tryGetDisplayName(entity));
    }

    public static boolean isTeammate(Object entity) {
        return getInstance().test(entity);
    }
}
