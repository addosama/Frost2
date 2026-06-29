package pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.override;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import pub.frost.base.event.impl.types.InputDevice;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.screen.components.InputListener;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.PanelClickGui;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.PanelComponent;
import pub.frost.client.feature.screen.impl.clickgui.styles.panel.components.widgets.property.ElementRenderer;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.overriding.OverrideData;
import pub.frost.client.property.overriding.suppliers.OverrideOnKey;
import pub.frost.utils.InputUtils;

import java.util.ArrayList;
import java.util.List;

public class OverridePopupComponent<T> extends PanelComponent implements InputListener {
    private interface InputConsumer extends InputListener {
        String getID();
    }

    private final AbstractProperty<T, ? extends AbstractProperty> prop;
    private final ElementRenderer<T> elementRenderer;

    private InputConsumer inputConsumer = null;

    public OverridePopupComponent(PanelClickGui gui, AbstractProperty<T, ? extends AbstractProperty> prop, ElementRenderer<T> elementRenderer) {
        super(gui);
        this.prop = prop;
        this.elementRenderer = elementRenderer;
    }

    @Override
    public void render(boolean dummy, float tickDelta) {
        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 12);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 1f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 4, 4);
        ImGui.pushStyleColor(ImGuiCol.PopupBg, gui.getTheme().getModulePanelBgColor());
        ImGui.pushStyleColor(ImGuiCol.Border, gui.getTheme().getWindowBorderColor());
        if (ImGui.beginPopup(this.toString())) {
            ImGui.pushStyleColor(ImGuiCol.Button, gui.getTheme().getButtonBgColor());
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, gui.getTheme().getButtonActiveBgColor());
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, gui.getTheme().getButtonHoveredBgColor());
            ImGui.pushStyleColor(ImGuiCol.Text, gui.getTheme().getMainColor());

            int renderedData = renderOverrideData(dummy, tickDelta);
            {
                FontManager.pushFont(FontManager.INSTANCE.puHui12);
                ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 9);
                String text = "+ Add Override";
                boolean noDataRendered = renderedData == 0;
                if (!noDataRendered) {
                    ImGui.dummy(Math.max(ImGui.calcTextSizeX(text) + 24, ImGui.getItemRectSizeX()), 4);
                }
                if (ImGui.button(text + "###" + this + ".addButton", noDataRendered? 180 : ImGui.getItemRectSizeX(), 28)) {
                    prop.addOverrideData(new OverrideData<>(new OverrideOnKey(0), prop.getValue()));
                }
                ImGui.popStyleVar();
                ImGui.popFont();
            }

            ImGui.popStyleColor(4);
            ImGui.endPopup();
        }
        ImGui.popStyleColor(2);
        ImGui.popStyleVar(3);
    }

    private int renderOverrideData(boolean dummy, float tickDelta) {
        List<OverrideData<T>> removeList = new ArrayList<>();
        int index = 0;
        for (OverrideData<T> data : prop.getOverrideData()) {
            if (index > 0) ImGui.dummy(ImGui.getItemRectSizeX(), 4);
            if (data.getApplySupplier() instanceof OverrideOnKey) {
                final OverrideOnKey supplier = (OverrideOnKey) data.getApplySupplier();
                final boolean hold = supplier.shouldActiveWhenRelease();

                boolean switchType = false;

                ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
                ImGui.beginGroup();

                ImGui.dummy(0, 4);

                {
                    ImGui.beginGroup();
                    ImGui.dummy(4, 0);
                    ImGui.sameLine();
                    // ImVec2 groupMin = ImGui.getCursorPos();

                    data.setValue(elementRenderer.renderElement(dummy, tickDelta, data.toString(), data.getValue()));
                    ImGui.sameLine(0, 4);

                    FontManager.pushFont(FontManager.INSTANCE.puHui12);
                    ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 6);
                    ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 6, 1);

                    // type button
                    {
                        String text = hold? "Hold" : "Click";
                        if (ImGui.button(text + "###type." + data)) {
                            switchType = true;
                        }
                    }
                    ImGui.sameLine(0, 4);
                    // keybind button
                    {
                        String id = "key." + data;
                        boolean active = inputConsumer != null && inputConsumer.getID().equals(id);
                        String text = active? "LISTENING" : InputUtils.getKeyName(supplier.getKeybind());
                        boolean buttonClicked = ImGui.button(text + "###" + id);
                        if (buttonClicked) {
                            long clickTime = System.currentTimeMillis();
                            if (active) {
                                supplier.setKeybind(-1);
                                inputConsumer = null;
                                gui.setActiveListener(null);
                            } else {
                                inputConsumer = new InputConsumer() {
                                    @Override
                                    public void onInput(InputDevice device, int code, int action) {
                                        if (code == 0) return;
                                        if (action != 2) {
                                            if (device == InputDevice.MOUSE) {
                                                if (code == -1) return;
                                                if (action != 1) return;
                                            }
                                            supplier.setKeybind(code);
                                            inputConsumer = null;
                                            gui.setActiveListener(null);
                                        }
                                    }

                                    @Override
                                    public String getID() {
                                        return id;
                                    }
                                };
                            }
                            gui.setActiveListener(this);
                        }
                    }
                    ImGui.sameLine(0, 4);
                    // delete button
                    {
                        String text = "Delete";
                        // ImGui.setCursorPosX(groupMin.x + 160 - ImGui.calcTextSizeX(text));
                        if (ImGui.button(text + "###delete." + data)) {
                            removeList.add(data);
                        }
                    }

                    ImGui.sameLine();
                    ImGui.dummy(4, 0);

                    ImGui.popStyleVar(2);
                    ImGui.popFont();

                    ImGui.endGroup();
                }

                ImGui.dummy(0, 4);

                ImGui.endGroup();
                ImGui.popStyleVar();

                if (switchType) supplier.setHold(!hold);
            }
            index ++;
        }
        removeList.forEach(prop::removeOverrideData);
        return index;
    }

    @Override
    public void onInput(InputDevice device, int code, int action) {
        if (inputConsumer != null) {
            inputConsumer.onInput(device, code, action);
        }
    }
}
