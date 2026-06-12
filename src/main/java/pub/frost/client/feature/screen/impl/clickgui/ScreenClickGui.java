package pub.frost.client.feature.screen.impl.clickgui;

import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import lombok.Getter;
import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.client.core.FrostCore;
import pub.frost.client.feature.bindable.api.IBindable;
import pub.frost.client.feature.screen.ClientScreen;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;

public class ScreenClickGui extends ClientScreen implements IBindable {
    public ScreenClickGui() {
        FrostCore.getInstance().getBindableManager().register(this);
    }

    @Getter
    private final PanelClickGui panel = new PanelClickGui();

    @Override
    public void render(boolean dummy, float tickDelta) {
        if (dummy) ImGui.pushStyleVar(ImGuiStyleVar.Alpha, 0.01f);

        panel.render(dummy, tickDelta);

        if (dummy) ImGui.popStyleVar();
    }

    @Override
    public void onInput(InputDevice device, int code, int action) {
        panel.onInput(device, code, action);
    }

    @Override
    public int getKeybind() {
        return 0x36;
    }
    @Override
    public void onActive(int action) {
        FrostCore.getInstance().getClientScreenManager().setCurrentScreen(ScreenClickGui.class);
    }
}
