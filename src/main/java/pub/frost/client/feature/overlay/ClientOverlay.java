package pub.frost.client.feature.overlay;

import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class ClientOverlay {
    private final String key;

    protected final int DEFAULT_WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.AlwaysAutoResize;
    
    public final void render(boolean dummy, boolean input, float tickDelta) {
        preRender(dummy);
        update(dummy, input, tickDelta);
        doRender(dummy, input, tickDelta);
        postRender(dummy);
    }

    protected void preRender(boolean dummy) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 2, 2);
        if (dummy) ImGui.pushStyleVar(ImGuiStyleVar.Alpha, 0.01f);
    }
    protected void update(boolean dummy, boolean input, float tickDelta) {}
    protected abstract void doRender(boolean dummy, boolean input, float tickDelta);
    protected void postRender(boolean dummy) {
        if (dummy) ImGui.popStyleVar();
        ImGui.popStyleVar();
    }

    @Override
    public String toString() {
        return key;
    }
}
