package ru.mehtm.beheading.core;

import java.util.List;
import java.util.Objects;

/** Immutable server balance snapshot; probabilities use fractions, not percentages. */
public record BeheadingConfig(List<Double> enchantmentBonuses, List<Double> upgradeBonuses,
        double maximumHeadDropChance, StackingMode stackingMode,
        boolean enableEnchantments, boolean enableSilentGearIntegration,
        boolean enableMaterialTraits, boolean enableUpgradeModules) {
    public BeheadingConfig {
        enchantmentBonuses = validated(enchantmentBonuses, 3, "enchantmentBonuses");
        upgradeBonuses = validated(upgradeBonuses, 4, "upgradeBonuses");
        probability(maximumHeadDropChance, "maximumHeadDropChance");
        Objects.requireNonNull(stackingMode, "stackingMode");
    }

    public static BeheadingConfig defaults() {
        return new BeheadingConfig(List.of(.05, .10, .20), List.of(.10, .17, .22, .25),
                1.0, StackingMode.MAX, true, true, true, true);
    }

    private static List<Double> validated(List<Double> values, int size, String name) {
        Objects.requireNonNull(values, name);
        if (values.size() != size) throw new IllegalArgumentException(name + " requires " + size + " levels");
        List<Double> copy = List.copyOf(values);
        for (double value : copy) probability(value, name);
        return copy;
    }

    static void probability(double value, String name) {
        if (!Double.isFinite(value) || value < 0 || value > 1)
            throw new IllegalArgumentException(name + " must be finite and within [0, 1]");
    }
}
