package ru.mehtm.beheading.core;

import java.util.List;
import java.util.Objects;

/** Pure calculation: the loot owner performs the single existing random draw. */
public final class BeheadingCalculator {
    private BeheadingCalculator() {}

    public record Result(double baseChance, double enchantmentBonus, double traitBonus,
            double upgradeBonus, double combinedBonus, double finalChance) {
        public double appliedBonus() { return finalChance - baseChance; }
    }

    /**
     * baseChance includes the provider's existing Looting and other adjustments.
     * traitBonus is resolved from current, applicable material traits by the adapter.
     * Levels above configured tiers saturate; negative levels are invalid input.
     */
    public static Result calculate(BeheadingConfig config, double baseChance,
            int enchantmentLevel, double traitBonus, int upgradeLevel) {
        Objects.requireNonNull(config, "config");
        BeheadingConfig.probability(baseChance, "baseChance");
        BeheadingConfig.probability(traitBonus, "traitBonus");
        if (enchantmentLevel < 0 || upgradeLevel < 0)
            throw new IllegalArgumentException("levels must be non-negative");
        double enchantment = config.enableEnchantments()
                ? tier(config.enchantmentBonuses(), enchantmentLevel) : 0;
        double trait = config.enableSilentGearIntegration() && config.enableMaterialTraits() ? traitBonus : 0;
        double upgrade = config.enableSilentGearIntegration() && config.enableUpgradeModules()
                ? tier(config.upgradeBonuses(), upgradeLevel) : 0;
        double gear = Math.max(trait, upgrade);
        double bonus = switch (config.stackingMode()) {
            case MAX -> Math.max(enchantment, gear);
            case ADD_CAPPED -> enchantment + trait + upgrade;
            case ENCHANTMENT_ONLY -> enchantment;
            case GEAR_ONLY -> gear;
        };
        // A bonus setting must never nerf an existing or guaranteed provider drop.
        double chance = Math.max(baseChance, Math.min(config.maximumHeadDropChance(), baseChance + bonus));
        return new Result(baseChance, enchantment, trait, upgrade, bonus, chance);
    }

    private static double tier(List<Double> bonuses, int level) {
        return level == 0 ? 0 : bonuses.get(Math.min(level, bonuses.size()) - 1);
    }
}
