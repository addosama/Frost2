package pub.frost.client.core;

import lombok.Getter;
import pub.frost.base.event.api.EventBus;
import pub.frost.client.feature.module.ModuleManager;
import pub.frost.client.i18n.I18n;
import pub.frost.client.i18n.Localizer;
import pub.frost.wrappers.WrapperManager;

@Getter
public final class FrostCore {
    private static final @Getter String CLIENT_NAME = "Frost";
    private static final @Getter int
            MAJOR_VERSION = 1,
            MINOR_VERSION = 0,
            PATCH_VERSION = 0;
    public static String getVersionString() {
        return MAJOR_VERSION + "." + MINOR_VERSION + "." + PATCH_VERSION;
    }

    public static final boolean DEBUG = true;

    private static @Getter FrostCore instance;
    public static Localizer getLocalizer() {
        return getInstance().getI18nHelper().getCurrentLocalizer();
    }

    private WrapperManager wrapperManager;

    private I18n i18nHelper;
    private EventBus eventBus;
    private ModuleManager moduleManager;

    public FrostCore() {
        instance = this;
    }

    public void initClient() {
        wrapperManager = new WrapperManager();

        i18nHelper = new I18n();
        i18nHelper.loadLanguages();

        eventBus = new EventBus();
        moduleManager = new ModuleManager();

        moduleManager.registerModules();
    }
}
