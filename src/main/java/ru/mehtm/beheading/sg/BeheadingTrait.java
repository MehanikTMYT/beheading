package ru.mehtm.beheading.sg;

import net.silentchaos512.gear.api.traits.TraitInstance;
import ru.mehtm.beheading.config.BeheadingBalance;
import ru.mehtm.beheading.core.BeheadingCalculator;
import ru.mehtm.beheading.loot.HeadChanceRoller;

public class BeheadingTrait {
    public static double getTraitBonus(TraitInstance trait) {
        int level = trait.getLevel();
        int[] levels = BeheadingBalance.UPGRADE_LEVELS;
        // Уровень trait соответствует индексу в массиве уровней BeheadingBalance (0..3)
        // Уровень уровня апгрейда (1-based)
        int upgradeLevel = Math.min(level, levels.length);
        if (upgradeLevel <= 0) return 0.0;
        
        // Значение бонуса из таблицы
        return levels[upgradeLevel - 1] / 100.0;
    }
}
