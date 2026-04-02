package pub.frost.base.rendering;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.implot.ImPlot;
import loutre.imgui.lwjgl2.ImGuiDisplay;
import loutre.imgui.lwjgl2.ImGuiLWJGL2;

public class ImGuiContext {
    private final static ImGuiDisplay imGuiDisplay = new ImGuiDisplay();
    private final static ImGuiLWJGL2 imGuiImplGl2 = new ImGuiLWJGL2();

    private static boolean initialized = false;

    public static void initialize() {
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

    public static void draw(Runnable runnable) {
        if (!initialized) initialize();
        imGuiImplGl2.newFrame();
        imGuiDisplay.newFrame();
        ImGui.newFrame();

        runnable.run();

        ImGui.endFrame();
        ImGui.render();
        imGuiImplGl2.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(1024)) {
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
        }
    }
}
