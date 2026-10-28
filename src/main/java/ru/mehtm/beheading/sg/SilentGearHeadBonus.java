package ru.mehtm.beheading.sg;

import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mehtm.beheading.config.BeheadingBalance;

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
            var core = balance.core();

            // Upgrade modules: the level declared by the applied Beheading upgrade part(s).
            // Read from the installed part itself, not from the merged trait list: Silent
            // Gear's trait merge doubles single entries and clamps them, which would blur
            // the four configured tiers. The distinct trait id also guarantees the module
            // bonus can never double count with the material trait bonus.
            int upgradeLevel = 0;
            if (core.enableUpgradeModules()) {
                upgradeLevel = maxDeclaredUpgradeLevel(weapon);
            }

            // Material trait: read the live, merged level (matches the gear tooltip);
            // contributions come from gear materials only, because Beheading upgrade parts
            // declare the separate upgrade trait.
            double traitBonus = 0.0D;
            if (core.enableMaterialTraits()) {
                var materialTrait = net.silentchaos512.gear.api.util.DataResource.trait(SilentGearTraitIds.MATERIAL);
                int materialLevel = net.silentchaos512.gear.util.TraitHelper.getTraitLevel(weapon, materialTrait);
                if (materialLevel > 0) {
                    traitBonus = materialLevel * balance.silentGearTraitBonusPerLevel();
                }
            }

            return new SilentGearBonus(upgradeLevel, traitBonus);
        }

        /**
         * Highest beheading upgrade-trait level declared by any misc-upgrade part actually
         * installed on the gear. Different tier parts saturate through max; the smithing
         * recipe already rejects a duplicate of the same part, so one tier cannot be applied
         * twice on the same item.
         */
        private static int maxDeclaredUpgradeLevel(ItemStack weapon) {
            if (!net.silentchaos512.gear.util.GearHelper.isGear(weapon)) {
                return 0;
            }
            var gearType = net.silentchaos512.gear.util.GearHelper.getType(weapon);
            var key = net.silentchaos512.gear.api.util.PropertyKey.of(
                    net.silentchaos512.gear.setup.gear.GearProperties.TRAITS.get(), gearType);

            int best = 0;
            for (net.silentchaos512.gear.gear.part.PartInstance part : net.silentchaos512.gear.util.GearData.getConstruction(weapon).parts()) {
                if (!part.isValid() || !part.isType(net.silentchaos512.gear.setup.gear.PartTypes.MISC_UPGRADE)) {
                    continue;
                }
                if (!(part.get() instanceof net.silentchaos512.gear.gear.part.CoreGearPart corePart)) {
                    continue;
                }
                var modifiers = corePart.getPropertyModifiers(
                        part, net.silentchaos512.gear.setup.gear.PartTypes.MISC_UPGRADE.get(), key);
                for (net.silentchaos512.gear.api.property.TraitListPropertyValue value : modifiers) {
                    for (net.silentchaos512.gear.api.traits.TraitInstance instance : value.value()) {
                        if (instance.isValid() && SilentGearTraitIds.UPGRADE.equals(instance.getTraitId())) {
                            best = Math.max(best, instance.getLevel());
                        }
                    }
                }
            }
            return best;
        }
    }
}
