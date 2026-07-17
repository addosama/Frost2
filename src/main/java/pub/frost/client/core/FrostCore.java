package pub.frost.client.core;

import lombok.Getter;
import pub.frost.base.event.api.EventBus;
import pub.frost.base.input.InputManager;
import pub.frost.base.rendering.ClientRenderContext;
import pub.frost.client.config.ConfigManager;
import pub.frost.client.feature.bindable.BindableManager;
import pub.frost.client.feature.module.ModuleManager;
import pub.frost.client.feature.screen.ClientScreenManager;
import pub.frost.client.i18n.I18n;
import pub.frost.client.i18n.Localizer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public final class FrostCore implements Initializer {
    public static final String CLIENT_NAME = "Frost";
    public static final int
            MAJOR_VERSION = 1,
            MINOR_VERSION = 0,
            PATCH_VERSION = 0;
    public static String getVersionString() {
        return MAJOR_VERSION + "." + MINOR_VERSION + "." + PATCH_VERSION;
    }

    public static final boolean DEBUG = true;
    public static void debugAssert(boolean condition, String... failStr) {
        if (DEBUG) assert condition : failStr[0] == null || failStr[0].isEmpty()? "No information provided" : failStr[0];
    }

    private static @Getter FrostCore instance;

    private I18n i18nHelper;
    private EventBus eventBus;
    private InputManager inputManager;
    private BindableManager bindableManager;
    private ModuleManager moduleManager;
    private ConfigManager configManager;

    private ClientScreenManager clientScreenManager;

    private ClientSettings clientSettings;
    private ClientHelpers helpers;

    public FrostCore() {
        instance = this;
    }

    public void initClient() {
        i18nHelper = new I18n();
        i18nHelper.loadLanguages();

        eventBus = new EventBus();
        inputManager = new InputManager();

        registerToInputManager(registerToEventBus(
                ClientRenderContext.getInstance()
        ));
        bindableManager = registerToEventBus(registerToInputManager(
                new BindableManager()
        ));
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();

        clientScreenManager = registerToEventBus(registerToInputManager(
                new ClientScreenManager()
        ));

        clientSettings = new ClientSettings();
        helpers = new ClientHelpers();

        FrostCore.getClientDir().toFile().mkdirs();
        moduleManager.registerModules();
        clientScreenManager.registerScreens();
        configManager.init();
    }

    public void shutdown() {
        configManager.saveAndWriteAllConfig();
    }

    public static Path getClientDir() {
        return Paths.get(System.getProperty("user.dir"), "frost");
    }
    public static Localizer getLocalizer() {
        return getInstance().getI18nHelper().getCurrentLocalizer();
    }
    
    public static EventBus getEventBus() {
        return instance.eventBus;
    }
    public static InputManager getInputManager() {
        return instance.inputManager;
    }
    public static ClientHelpers getHelpers() {
        return instance.helpers;
    }
}
