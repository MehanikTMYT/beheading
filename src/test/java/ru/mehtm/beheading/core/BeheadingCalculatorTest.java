package ru.mehtm.beheading.core;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Executable contract tests for BeheadingCalculator; no Minecraft bootstrap, runs under `./gradlew test`. */
final class BeheadingCalculatorTest {
    private static final BeheadingConfig DEFAULT = BeheadingConfig.defaults();

    private static double chance(BeheadingConfig c, double base, int enchant, double trait, int upgrade) {
        return BeheadingCalculator.calculate(c, base, enchant, trait, upgrade).finalChance();
    }
    private static void equal(double expected, double actual) {
        if (Math.abs(expected - actual) > 1e-12 || !Double.isFinite(actual))
            throw new AssertionError("expected " + expected + ", actual " + actual);
    }
    private static void rejects(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("invalid input accepted");
    }
    private static BeheadingConfig config(double cap, StackingMode mode, boolean e, boolean sg, boolean t, boolean u) {
        return new BeheadingConfig(DEFAULT.enchantmentBonuses(), DEFAULT.upgradeBonuses(), cap, mode, e, sg, t, u);
    }

    @Test
    void zeroBaselineAndEnchantmentBonus() {
        equal(0, chance(DEFAULT, 0, 0, 0, 0));
        double[] ench = {0, .05, .10, .20};
        for (int e = 0; e < ench.length; e++) {
            equal(.025 + ench[e], chance(DEFAULT, .025, e, 0, 0));
        }
        // Looting-adjusted base is preserved and receives the bonus exactly once.
        equal(.255, chance(DEFAULT, .055, 3, 0, 0));
        equal(.25, chance(DEFAULT, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE));
        equal(1, chance(DEFAULT, 1, 3, .9, 4));
        equal(1, chance(DEFAULT, .95, 3, 0, 4));
        equal(.2, chance(config(1, StackingMode.ENCHANTMENT_ONLY, true, true, true, true), 0, 3, .8, 4));
        equal(0, chance(config(1, StackingMode.MAX, false, false, true, true), 0, 3, .8, 4));
    }

    @Test
    void gearBonusStackingModeMax() {
        double[] upgrades = {0, .10, .17, .22, .25};
        for (int u = 0; u < upgrades.length; u++) {
            equal(Math.max(.20, upgrades[u]), chance(DEFAULT, 0, 3, 0, u));
            equal(Math.max(.30, Math.max(.20, upgrades[u])), chance(DEFAULT, 0, 3, .30, u));
        }
        equal(.25, chance(config(1, StackingMode.MAX, true, true, true, true), 0, 3, .1, 4));
        equal(.8, chance(config(1, StackingMode.MAX, false, true, true, false), 0, 3, .8, 4));
        equal(.25, chance(config(1, StackingMode.MAX, false, true, false, true), 0, 3, .8, 4));
        // Trait flag off silently drops the trait bonus.
        equal(.8, chance(config(1, StackingMode.MAX, true, true, true, true), 0, 3, .8, 0));
        equal(.2, chance(config(1, StackingMode.MAX, true, true, true, true), 0, 3, .2, 0));
        // Silent Gear integration off: both trait and upgrade bonuses are disabled.
        equal(.2, chance(config(1, StackingMode.MAX, true, false, true, true), 0, 3, 0, 4));
        equal(.2, chance(config(1, StackingMode.MAX, true, false, true, true), 0, 3, 0, 0));
    }

    @Test
    void otherStackingModes() {
        equal(.4, chance(config(.4, StackingMode.ADD_CAPPED, true, true, true, true), .1, 3, .2, 4));
        equal(.65, chance(config(1, StackingMode.ADD_CAPPED, true, true, true, true), 0, 3, .2, 4));
        equal(.6, chance(config(.4, StackingMode.MAX, true, true, true, true), .6, 3, .2, 4));
        equal(.25, chance(config(1, StackingMode.GEAR_ONLY, true, true, true, true), 0, 3, .1, 4));
    }

    @Test
    void capNeverTruncatesBaseChance() {
        equal(.6, chance(config(.4, StackingMode.MAX, true, true, true, true), .6, 3, .2, 4));
        equal(0, chance(config(0, StackingMode.MAX, true, true, true, true), 0, 3, 1, 4));
        equal(.9, chance(config(.1, StackingMode.MAX, true, true, true, true), .9, 3, .2, 4));
    }

    @Test
    void invalidInputRejected() {
        for (double bad : new double[]{-1, 1.01, Double.NaN, Double.POSITIVE_INFINITY}) {
            rejects(() -> chance(DEFAULT, bad, 0, 0, 0));
            rejects(() -> chance(DEFAULT, 0, 0, bad, 0));
            rejects(() -> config(bad, StackingMode.MAX, true, true, true, true));
        }
        rejects(() -> chance(DEFAULT, 0, -1, 0, 0));
        rejects(() -> chance(DEFAULT, 0, 0, 0, -1));
        rejects(() -> new BeheadingConfig(List.of(.1), DEFAULT.upgradeBonuses(), 1, StackingMode.MAX, true, true, true, true));
        rejects(() -> new BeheadingConfig(List.of(.1, .2, Double.NaN), DEFAULT.upgradeBonuses(), 1, StackingMode.MAX, true, true, true, true));
        rejects(() -> new BeheadingConfig(DEFAULT.enchantmentBonuses(), List.of(), 1, StackingMode.MAX, true, true, true, true));
        rejects(() -> new BeheadingConfig(DEFAULT.enchantmentBonuses(), List.of(1.5, 0.0, 0.0, 0.0), 1, StackingMode.MAX, true, true, true, true));
    }

    @Test
    void configSnapshotIsImmutable() {
        ArrayList<Double> mutable = new ArrayList<>(DEFAULT.enchantmentBonuses());
        BeheadingConfig snapshot = new BeheadingConfig(mutable, DEFAULT.upgradeBonuses(), 1, StackingMode.MAX, true, true, true, true);
        mutable.set(0, .9);
        equal(.05, chance(snapshot, 0, 1, 0, 0));
    }

    @Test
    void boundaryGridExact() {
        // Exhaustive boundary grid: a bonus never removes base loot or exceeds 100%.
        for (int base = 0; base <= 100; base++) for (int cap = 0; cap <= 100; cap++) {
            double b = base / 100.0, c = cap / 100.0;
            double p = chance(config(c, StackingMode.MAX, true, true, true, true), b, 3, .1, 4);
            equal(Math.max(b, Math.min(c, b + .25)), p);
        }
    }
}
