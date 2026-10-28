package ru.mehtm.beheading.loot;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import ru.mehtm.beheading.config.BeheadingBalance;
import ru.mehtm.beheading.config.BeheadingConfigSpec;
import ru.mehtm.beheading.core.BeheadingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Holds the current immutable balance snapshot and rebuilds it from the server config. */
public final class HeadChanceRoller {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeadChanceRoller.class);

    private static volatile BeheadingBalance balance = BeheadingBalance.DEFAULT;

    private HeadChanceRoller() {}

    public static BeheadingBalance balance() {
        return balance;
    }

    public static void refreshBalance() {
        balance = read();
    }

    private static BeheadingBalance read() {
        try {
            List<Double> enchantmentBonuses = readFractions(BeheadingConfigSpec.ENCHANTMENT_BONUSES.get(), 3);
            List<Double> upgradeBonuses = readFractions(BeheadingConfigSpec.UPGRADE_BONUSES.get(), 4);
            BeheadingConfig core = new BeheadingConfig(
                    enchantmentBonuses,
                    upgradeBonuses,
                    BeheadingConfigSpec.MAXIMUM_HEAD_DROP_CHANCE.get(),
                    BeheadingConfigSpec.STACKING_MODE.get(),
                    BeheadingConfigSpec.ENABLE_ENCHANTMENTS.get(),
                    BeheadingConfigSpec.ENABLE_SILENTGEAR_INTEGRATION.get(),
                    BeheadingConfigSpec.ENABLE_MATERIAL_TRAITS.get(),
                    BeheadingConfigSpec.ENABLE_UPGRADE_MODULES.get());
            return new BeheadingBalance(
                    core,
                    BeheadingConfigSpec.REQUIRE_PLAYER_KILL.get(),
                    BeheadingConfigSpec.ALLOW_PROJECTILE_KILLS.get(),
                    BeheadingConfigSpec.ALLOW_AUTOMATION_KILLS.get(),
                    BeheadingConfigSpec.ENABLE_VANILLA_HEADS.get(),
                    BeheadingConfigSpec.ENABLE_MODDED_HEADS.get(),
                    prefixed(BeheadingConfigSpec.MOB_BLACKLIST.get()),
                    prefixed(BeheadingConfigSpec.MOB_WHITELIST.get()));
        } catch (RuntimeException error) {
            // Broken config values must fall back to defaults, never break loot generation.
            LOGGER.warn("Invalid Beheading config, using defaults", error);
            return BeheadingBalance.DEFAULT;
        }
    }

    private static List<Double> readFractions(List<? extends Double> values, int size) {
        List<Double> copy = new ArrayList<>(values);
        if (copy.size() != size) {
            throw new IllegalArgumentException("expected " + size + " fractions, found " + copy.size());
        }
        for (double value : copy) {
            if (!Double.isFinite(value) || value < 0 || value > 1) {
                throw new IllegalArgumentException("fraction out of [0,1]: " + value);
            }
        }
        return copy;
    }

    private static List<String> prefixed(List<? extends String> values) {
        List<String> copy = new ArrayList<>();
        for (String value : values) {
            copy.add(Identifier.tryParse(value) != null ? value : "minecraft:" + value);
        }
        return copy;
    }
}
