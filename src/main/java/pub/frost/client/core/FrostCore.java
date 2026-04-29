package pub.frost.client.core;

import lombok.Getter;
import pub.frost.base.event.api.EventBus;
import pub.frost.base.rendering.ClientRenderContext;
import pub.frost.client.config.ConfigManager;
import pub.frost.client.feature.bindable.BindableManager;
import pub.frost.client.feature.helper.network.LagManager;
import pub.frost.client.feature.helper.network.PacketManager;
import pub.frost.client.feature.helper.player.interact.PlayerListener;
import pub.frost.client.feature.helper.player.rotation.RotationManager;
import pub.frost.client.feature.module.ModuleManager;
import pub.frost.client.feature.screen.ClientScreenManager;
import pub.frost.client.i18n.I18n;
import pub.frost.client.i18n.Localizer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
public final class FrostCore implements Initializer {
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

    private I18n i18nHelper;
    private EventBus eventBus;
    private BindableManager bindableManager;
    private ModuleManager moduleManager;
    private ConfigManager configManager;

    private ClientScreenManager clientScreenManager;
    
    private ClientHelpers helpers;

    public FrostCore() {
        instance = this;
    }

    public void initClient() {
        i18nHelper = new I18n();
        i18nHelper.loadLanguages();

        eventBus = new EventBus();
        registerToEventBus(ClientRenderContext.getInstance());
        bindableManager = registerToEventBus(new BindableManager());
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();

        clientScreenManager = registerToEventBus(new ClientScreenManager());

        helpers = new ClientHelpers();

        FrostCore.getClientDir().toFile().mkdirs();
        moduleManager.registerModules();
        clientScreenManager.registerScreens();
        configManager.init();
    }

    public void shutdown() {
        configManager.saveAndWriteAllConfig();
    }

    @Deprecated
    public RotationManager getRotationManager() {
        return helpers.rotationManager;
    }
    @Deprecated
    public PlayerListener getPlayerListener() {
        return helpers.playerListener;
    }
    @Deprecated
    public LagManager getLagManager() {
        return helpers.lagManager;
    }
    @Deprecated
    public PacketManager getPacketManager() {
        return helpers.packetManager;
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
    public static ClientHelpers getHelpers() {
        return instance.helpers;
    }
}
