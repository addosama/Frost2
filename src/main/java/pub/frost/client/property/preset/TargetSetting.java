package pub.frost.client.property.preset;

import pub.frost.base.wrapping.Wrappers;
import pub.frost.client.feature.module.impl.utility.Teams;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupHead;
import pub.frost.client.property.annotations.PropertyGroupMain;
import pub.frost.client.property.impl.bool.BooleanProperty;
import pub.frost.client.property.impl.bool.MultipleBooleanProperty;
import pub.frost.utils.targeting.EnumEntityTarget;

import java.util.function.Supplier;

public class TargetSetting {
    @TranslationKey("strings.target.targets")
    @PropertyGroupHead("targets")
    private final Supplier<Boolean> visibility;

    @PropertyGroupMain
    @TranslationKey("strings.target.targets")
    @Property(value = "Targets")
    public final MultipleBooleanProperty<EnumEntityTarget> targets = new MultipleBooleanProperty<>(EnumEntityTarget.class);
    @TranslationKey("strings.target.invisiblecheck")
    @Property("InvisibleCheck")
    public final BooleanProperty invisibleCheck = new BooleanProperty(false);
    @TranslationKey("strings.target.teamcheck")
    @Property("TeamCheck")
    public final BooleanProperty teamCheck = new BooleanProperty(false);
    @TranslationKey("strings.target.botcheck")
    @Property(value = "BotCheck", endGroup = true)
    public final BooleanProperty botCheck = new BooleanProperty(false);

    private final boolean shouldDoInvisibleCheck, shouldDoTeamCheck, shouldDoBotCheck;

    public TargetSetting(
            Supplier<Boolean> groupVisibility,
            boolean enableInvisibleCheck,
            boolean enableTeamCheck,
            boolean enableBotCheck
    ) {
        this.visibility = groupVisibility == null? () -> true : groupVisibility;
        this.shouldDoInvisibleCheck = enableInvisibleCheck;
        this.shouldDoTeamCheck = enableTeamCheck;
        this.shouldDoBotCheck = enableBotCheck;
        if (!enableInvisibleCheck) invisibleCheck.setVisibilitySupplier(() -> false);
        if (!enableTeamCheck) teamCheck.setVisibilitySupplier(() -> false);
        if (!enableBotCheck) botCheck.setVisibilitySupplier(() -> false);
    }

    public TargetSetting() {
        this(
                () -> true,
                true, true, true
        );
    }

    public boolean isTarget(Object entity) {
        if (shouldDoInvisibleCheck && invisibleCheck.get() && Wrappers.Entity.isInvisible(entity)) return false;
        if (shouldDoTeamCheck && teamCheck.get() && Teams.isTeammate(entity)) return false;
        // if (shouldDoBotCheck && botCheck.get() && false) return false;
        for (EnumEntityTarget target : targets.getEnabled()) {
            if (target.isTarget(entity.getClass())) return true;
        }
        return false;
    }
}
