package pub.frost.client.feature.overlay;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import lombok.RequiredArgsConstructor;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.utils.ImTextRenderer;

import java.util.function.Supplier;

@RequiredArgsConstructor
public abstract class ClientOverlay {
    private final String key, icon;
    private final Supplier<String> nameSupplier;

    public ClientOverlay(String key) {
        this(key, "", () -> key);
    }
    public ClientOverlay(String key, String icon) {
        this(key, icon, () -> key);
        if (FrostCore.DEBUG) assert this instanceof Named;
    }
    public ClientOverlay(String key, Supplier<String> nameSupplier) {
        this(key, "", nameSupplier);
    }

    protected final int DEFAULT_WINDOW_FLAGS = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize;

    public void tick() {}
    public final void render(boolean dummy, boolean input, float tickDelta) {
        if (!isVisible()) return;
        final ImVec2 renderPos, normalizedOffset; {
            ImVec2[] contentRenderData = preRender(dummy, input);
            renderPos = contentRenderData[0];
            normalizedOffset = contentRenderData[1];
        }
        if (!dummy) {
            doRender(renderPos, normalizedOffset, ImGui.getBackgroundDrawList(), input, tickDelta);
        }
        postRender(dummy, input);
    }

    protected ImVec2[] preRender(boolean dummy, boolean input) {
        if (dummy) ImGui.pushStyleVar(ImGuiStyleVar.Alpha, 0.01f);

        final boolean showTitle = isTitleAlwaysVisible() || input;
        int windowFlags = DEFAULT_WINDOW_FLAGS;
        if (!input) windowFlags |= ImGuiWindowFlags.NoInputs;
        if (!showTitle) windowFlags |= ImGuiWindowFlags.NoBackground;

        final float contentOffset = 9;

        final ImVec2 windowPos;
        final ImVec2 renderPos;
        final ImVec2 normalizedOffset;

        float yOffset = 0;
        ImGui.pushFont(FontManager.INSTANCE.puHui16);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 12f);
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0xFF33221F);

        ImGui.setNextWindowSize(200, 30);
        ImGui.begin(this.toString(), windowFlags);
        windowPos = ImGui.getWindowPos();

        // draw title
        {
            if (!showTitle) ImGui.pushStyleVar(ImGuiStyleVar.Alpha, 0.001f);
            else yOffset += 30 + contentOffset;

            float xOffset = 6;
            if (icon != null && !icon.isEmpty()) {
                ImGui.pushFont(FontManager.INSTANCE.icon14);
                ImGui.setCursorPos(
                        xOffset, 8
                );
                ImGui.textColored(0xFFFFEAE5, icon);
                xOffset += 38;
                ImGui.popFont();
            } else xOffset += 6;

            String name = getName();
            ImVec2 namePos = ImTextRenderer.centerText(
                    name,
                    xOffset, 15,
                    false, true
            );
            ImGui.setCursorPos(namePos);
            ImGui.textColored(0xFFFFEAE5, name);
            if (!showTitle) ImGui.popStyleVar();
        }

        ImVec2 windowSize = ImGui.getWindowSize();
        float dummyW = 0;
        if (windowSize.x > 160 - 8) dummyW = 8;
        ImGui.sameLine(0, 0);
        ImGui.setCursorPosY(0);
        ImGui.dummy(dummyW, 30);
        windowSize = ImGui.getWindowSize();

        {
            ImVec2 displaySize = ImGui.getIO().getDisplaySize();
            float windowCenterX = windowPos.x + windowSize.x / 2;
            float windowCenterY = windowPos.y + windowSize.y / 2;

            final float normalizedX, normalizedY;

            if (windowCenterX > displaySize.x / 2)
                normalizedX = -1;
            else normalizedX = 1;

            if (windowCenterY > displaySize.y / 2)
                normalizedY = -1;
            else normalizedY = 1;

            normalizedOffset = new ImVec2(normalizedX, normalizedY);
        }

        renderPos = windowPos.plus(
                normalizedOffset.x < 0? windowSize.x : 0,
                normalizedOffset.y < 0? -contentOffset + (showTitle? 0 : windowSize.y) : yOffset
        );
        ImGui.end();

        ImGui.popStyleColor();
        ImGui.popStyleVar(3);
        ImGui.popFont();

        return new ImVec2[] {renderPos, normalizedOffset};
    }
    protected abstract void doRender(ImVec2 pos, ImVec2 normalizedOffset, ImDrawList draws, boolean input, float tickDelta);
    protected void postRender(boolean dummy, boolean input) {
        if (dummy) ImGui.popStyleVar();
    }

    public boolean isVisible() {
        return true;
    }
    public boolean isTitleAlwaysVisible() {
        return false;
    }

    @Override
    public String toString() {
        return key;
    }

    public String getName() {
        return nameSupplier.get();
    }
}
