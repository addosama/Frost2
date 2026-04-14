package pub.frost.client.core;

import lombok.Getter;
import pub.frost.base.event.api.EventBus;
import pub.frost.base.rendering.ImGuiContext;
import pub.frost.client.feature.bindable.BindableManager;
import pub.frost.client.feature.helper.PlayerListener;
import pub.frost.client.feature.helper.RotationManager;
import pub.frost.client.feature.module.ModuleManager;
import pub.frost.client.feature.screen.ClientScreenManager;
import pub.frost.client.i18n.I18n;
import pub.frost.client.i18n.Localizer;

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

    private I18n i18nHelper;
    private EventBus eventBus;
    private BindableManager bindableManager;
    private ModuleManager moduleManager;
    private ClientScreenManager clientScreenManager;

    private RotationManager rotationManager;
    private PlayerListener playerListener;

    public FrostCore() {
        instance = this;
    }

    public void initClient() {
        i18nHelper = new I18n();
        i18nHelper.loadLanguages();

        eventBus = new EventBus();
        eventBus.register(ImGuiContext.getInstance());
        bindableManager = new BindableManager();
        eventBus.register(bindableManager);
        moduleManager = new ModuleManager();
        clientScreenManager = new ClientScreenManager();
        eventBus.register(clientScreenManager);

        rotationManager = new RotationManager();
        eventBus.register(rotationManager);
        playerListener = new PlayerListener();
        eventBus.register(playerListener);

        moduleManager.registerModules();
        clientScreenManager.registerScreens();
    }
}
