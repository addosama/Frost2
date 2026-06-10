package pub.frost.client.feature.screen;

import lombok.Getter;
import pub.frost.base.event.api.annotations.EventHandler;
import pub.frost.base.event.impl.events.EventGameTick;
import pub.frost.base.event.impl.events.EventInput;
import pub.frost.base.event.impl.events.EventPostRender;
import pub.frost.base.event.impl.events.EventRender2D;
import pub.frost.base.event.impl.types.InputDevice;
import net.minecraft.client.Minecraft;
import pub.frost.base.event.impl.types.TickType;
import pub.frost.client.feature.screen.impl.clickgui.ScreenClickGui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ClientScreenManager {
    private static final Map<Class<? extends ClientScreen>, ClientScreen> clientScreenMap = new HashMap<>();

    protected final Minecraft mc = Minecraft.getMinecraft();

    private @Getter ClientScreen currentScreen = null;
    public void setCurrentScreen(ClientScreen screen) {
        if (this.currentScreen != null) {
            closeCurrentScreen();
        }
        this.currentScreen = screen;
        if (screen != null) {
            screen.onDisplay();
        }
    }
    public void setCurrentScreen(Class<? extends ClientScreen> screenClass) {
        ClientScreen screen = getScreen(screenClass);
        if (screen != null) setCurrentScreen(screen);
    }
    public void closeCurrentScreen() {
        if (this.currentScreen == null) return;
        this.currentScreen.onClose();
        this.currentScreen = null;
        mc.setIngameFocus();
    }

    @EventHandler
    private void onPostGameTick(EventGameTick event) {
        if (event.getType() == TickType.POST) {
            if (currentScreen != null) {
                if (!currentScreen.allowCursorGrabbing()) {
                    mc.setIngameNotInFocus();
                }
            }
        }
    }

    @EventHandler(priority = 100)
    private void onRender2D(EventRender2D event) {
        if (currentScreen != null) {
            currentScreen.render(true, event.getTickDelta());
        }
    }
    @EventHandler(priority = 100)
    private void onPostRender(EventPostRender event) {
        if (currentScreen != null) {
            currentScreen.render(false, event.getTickDelta());
        }
    }

    @EventHandler(priority = 3)
    private void onInput(EventInput event) {
        if (currentScreen != null) {
            if ((currentScreen.shouldBlockMouseInput() && event.getType() == InputDevice.MOUSE)) event.setCancelled(true);
            else if (currentScreen.shouldBlockKeyboardInput() && event.getType() == InputDevice.KEYBOARD) {
                event.setCancelled(true);
                if (event.getKey() == 1) closeCurrentScreen();
            }
            if (currentScreen != null) currentScreen.onInput(event.getType(), event.getKey(), event.getAction());
        }
    }

    public void registerScreen(ClientScreen... screens) {
        for (ClientScreen screen : screens) {
            clientScreenMap.put(screen.getClass(), screen);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ClientScreen> T getScreen(Class<T> screenClass, Supplier<T> fallback) {
        return (T) clientScreenMap.computeIfAbsent(
                screenClass,
                key -> fallback.get()
        );
    }
    public <T extends ClientScreen> T getScreen(Class<T> screenClass) {
        return getScreen(screenClass, () -> null);
    }

    public void registerScreens() {
        registerScreen(
                new ScreenClickGui()
        );
    }
}
