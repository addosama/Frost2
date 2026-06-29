package pub.frost.client.feature.overlay.impl;

import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import pub.frost.base.rendering.FontManager;
import pub.frost.client.feature.overlay.ClientOverlay;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.i18n.interfaces.Named;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.number.IntegerProperty;
import pub.frost.utils.ImTextRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@TranslationKey("modules.hud.props.effectdisplay.name")
public class EffectDisplayOverlay extends ClientOverlay implements Named {
    public EffectDisplayOverlay() {
        super("overlays.effectdisplay");
    }

    @PropertyGroupMain
    @TranslationKey("strings.enabled")
    @Property("enabled")
    public final BooleanProperty enabled = new BooleanProperty(true);

    @Property("MaxDisplay")
    public final IntegerProperty maxDisplay = new IntegerProperty(1, 12, 1, 6);

    @Property("ShowDuration")
    public final BooleanProperty showDuration = new BooleanProperty(true);

    @Property("ShowAmplifier")
    public final BooleanProperty showAmplifier = new BooleanProperty(true);

    @Property("Sidebar")
    public final BooleanProperty sidebar = new BooleanProperty(true);

    @Property("SidebarWidth")
    public final IntegerProperty sidebarWidth = new IntegerProperty(2, 8, 1, 3).setVisibilitySupplier(sidebar::get);

    @Property("SidebarGradient")
    public final BooleanProperty sidebarGradient = new BooleanProperty(true).setVisibilitySupplier(sidebar::get);

    @Property("Background")
    public final BooleanProperty background = new BooleanProperty(true);

    @Override
    protected void doRender(ImVec2 pos, ImVec2 normalizedOffset, ImDrawList draws, boolean input, float tickDelta) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        @SuppressWarnings("unchecked")
        Collection<PotionEffect> activeEffects = mc.thePlayer.getActivePotionEffects();
        if (activeEffects.isEmpty()) return;

        List<PotionEffect> sortedEffects = new ArrayList<>(activeEffects);
        sortedEffects.sort(Comparator.comparingInt(PotionEffect::getDuration).reversed());

        int count = Math.min(sortedEffects.size(), maxDisplay.get());
        int sidebarW = sidebar.get() ? sidebarWidth.get() : 0;
        int hPadding = 8, vPadding = 3;

        FontManager.pushFont(FontManager.INSTANCE.puhui14);
        float lineHeight = ImTextRenderer.getTextHeight() + vPadding * 2;

        float maxWidth = 0;
        for (int i = 0; i < count; i++) {
            PotionEffect effect = sortedEffects.get(i);
            float w = sidebarW + hPadding * 2;
            String name = I18n.format(effect.getEffectName());
            if (showAmplifier.get()) {
                int amp = effect.getAmplifier();
                if (amp >= 0) name += " " + toRoman(amp + 1);
            }
            w += ImTextRenderer.getTextWidth(name);
            if (showDuration.get()) {
                w += ImTextRenderer.getTextWidth(" ") + ImTextRenderer.getTextWidth(formatDuration(effect));
            }
            maxWidth = Math.max(maxWidth, w);
        }

        float yOffset = 0;
        for (int i = 0; i < count; i++) {
            PotionEffect effect = sortedEffects.get(i);
            String name = I18n.format(effect.getEffectName());
            if (showAmplifier.get()) {
                int amp = effect.getAmplifier();
                if (amp >= 0) name += " " + toRoman(amp + 1);
            }
            String duration = formatDuration(effect);

            renderEffect(
                    pos.plus(0, yOffset), draws,
                    name, duration, getPotionColor(effect),
                    sidebarW, maxWidth, lineHeight,
                    hPadding, vPadding, normalizedOffset
            );
            yOffset += lineHeight * normalizedOffset.y;
        }

        ImGui.popFont();
    }

    private void renderEffect(
            ImVec2 pos, ImDrawList draws,
            String name, String duration, int potionColor,
            int sidebarW, float rowWidth, float rowHeight,
            int hPadding, int vPadding, ImVec2 no
    ) {
        int bgColor = ImColor.rgba("#141933CC");
        int textColor = ImColor.rgba("#E5EAFFFF");

        if (background.get()) {
            draws.addRectFilled(
                    pos, pos.plus(rowWidth * no.x, rowHeight * no.y),
                    bgColor
            );
        }

        ImVec2 sidebarEnd = pos;
        if (sidebarW > 0) {
            sidebarEnd = pos.plus(sidebarW * no.x, 0);
            draws.addRectFilled(
                    pos, sidebarEnd.plus(0, rowHeight * no.y),
                    potionColor
            );
            if (sidebarGradient.get()) {
                int gradientStart = ImGui.getColorU32i(potionColor, 0.3f);
                int gradientEnd = ImGui.getColorU32i(potionColor, 0);
                draws.addRectFilledMultiColor(
                        sidebarEnd,
                        sidebarEnd.plus(6 * no.x, rowHeight * no.y),
                        gradientStart, gradientEnd, gradientEnd, gradientStart
                );
            }
        }

        // name text
        {
            float textX = sidebarEnd.x + no.x * hPadding;
            float textY = pos.y + no.y * vPadding;
            if (no.x < 0) textX -= ImTextRenderer.getTextWidth(name);
            if (no.y < 0) textY -= ImTextRenderer.getTextHeight();

            ImTextRenderer.drawText(draws, name, textX, textY, textColor);
        }

        // duration text
        if (showDuration.get()) {
            float durX = pos.x + no.x * (rowWidth - hPadding) - ImTextRenderer.getTextWidth(duration);
            float durY = pos.y + no.y * vPadding;
            if (no.y < 0) durY -= ImTextRenderer.getTextHeight();

            ImTextRenderer.drawText(draws, duration, durX, durY, textColor);
        }
    }

    private static int getPotionColor(PotionEffect effect) {
        Potion potion = Potion.potionTypes[effect.getPotionID()];
        return potion != null ? potion.getLiquidColor() : 0xFF4A4A4A;
    }

    private static String formatDuration(PotionEffect effect) {
        int ticks = effect.getDuration();
        if (ticks > 12000) return "∞";
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    private static final String[] ROMAN = {
        "", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
        "XI", "XII", "XIII", "XIV", "XV"
    };
    private static String toRoman(int n) {
        return n > 0 && n < ROMAN.length ? ROMAN[n] : String.valueOf(n);
    }

    @Override
    public boolean isVisible() {
        return enabled.get();
    }

    @Override
    public String getName() {
        return Named.super.getName();
    }
}
