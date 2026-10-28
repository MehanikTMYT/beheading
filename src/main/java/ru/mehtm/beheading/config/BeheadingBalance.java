package ru.mehtm.beheading.config;

import java.util.List;
import ru.mehtm.beheading.core.BeheadingConfig;
import ru.mehtm.beheading.core.StackingMode;

/**
 * Immutable server balance snapshot rebuilt whenever the config loads or reloads.
 * Loot modifiers always read this instance, never the config spec directly.
 */
public record BeheadingBalance(BeheadingConfig core, boolean requirePlayerKill, boolean allowProjectileKills,
        boolean allowAutomationKills, boolean enableVanillaHeads, boolean enableModdedHeads,
        List<String> mobBlacklist, List<String> mobWhitelist, double silentGearTraitBonusPerLevel) {

    public static final BeheadingBalance DEFAULT = new BeheadingBalance(
            BeheadingConfig.defaults(), true, false, false, true, true, List.of(), List.of(), 0.05D);
}
