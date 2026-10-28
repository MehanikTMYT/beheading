package ru.mehtm.beheading.sg;

import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mehtm.beheading.config.BeheadingBalance;
import ru.mehtm.beheading.config.BeheadingConfigSpec;

/**
 * Runtime bridge for the optional Silent Gear integration.
 * The nested Impl references Silent Gear API classes and is only touched after
 * ModList.isLoaded("silentgear"), so the head roll stays safe when Silent Gear is absent.
 */
public final class SilentGearHeadBonus {
    private static final Logger LOGGER = LoggerFactory.getLogger(SilentGearHeadBonus.class);

    private SilentGearHeadBonus() {}

    public static SilentGearBonus resolve(ItemStack weapon, BeheadingBalance balance) {
        if (!balance.core().enableSilentGearIntegration()) {
            return SilentGearBonus.ZERO;
        }
        if (!net.neoforged.fml.ModList.get().isLoaded("silentgear")) {
            return SilentGearBonus.ZERO;
        }
        try {
            return Impl.resolve(weapon, balance);
        } catch (RuntimeException error) {
            LOGGER.warn("Silent Gear head bonus lookup failed, treating it as absent", error);
            return SilentGearBonus.ZERO;
        }
    }

    /** Loaded lazily; safe to reference Silent Gear API classes only inside this class. */
    private static final class Impl {
        static SilentGearBonus resolve(ItemStack weapon, BeheadingBalance balance) {
            var trait = net.silentchaos512.gear.api.util.DataResource.trait(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("beheading", "beheading"));
            int level = 0;
            if (balance.core().enableMaterialTraits()) {
                level = net.silentchaos512.gear.util.TraitHelper.getTraitLevel(weapon, trait);
            }
            double traitBonus = 0.0D;
            if (level > 0) {
                traitBonus = level * BeheadingConfigSpec.MATERIAL_TRAIT_BONUS_PER_LEVEL.get();
            }
            return new SilentGearBonus(0, traitBonus);
        }
    }
}
