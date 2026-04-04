package pub.frost.base.rendering;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.implot.ImPlot;
import lombok.Getter;
import loutre.imgui.lwjgl2.ImGuiDisplay;
import loutre.imgui.lwjgl2.ImGuiLWJGL2;

public class ImGuiContext {
    @Getter
    private static final ImGuiContext instance = new ImGuiContext();

    private final ImGuiDisplay imGuiDisplay = new ImGuiDisplay();
    private final ImGuiLWJGL2 imGuiImplGl2 = new ImGuiLWJGL2();

    private boolean initialized = false;
    public void initialize() {
        if (initialized) return;

        ImGui.createContext();
        ImPlot.createContext();

        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().setFreeTypeRenderer(true);

        imGuiDisplay.init();
        imGuiImplGl2.init();
        new FontManager();

        initialized = true;
    }

    public void startFrame() {
        if (!initialized) initialize();
        imGuiImplGl2.newFrame();
        imGuiDisplay.newFrame();
        ImGui.newFrame();
    }
    public void endFrame() {
        ImGui.render();
        imGuiImplGl2.renderDrawData(ImGui.getDrawData());
    }

    public static void draw(Runnable runnable) {
        instance.startFrame();
        runnable.run();
        instance.endFrame();
    }
}
