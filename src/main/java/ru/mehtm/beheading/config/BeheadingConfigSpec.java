package ru.mehtm.beheading.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;
import ru.mehtm.beheading.core.StackingMode;

/**
 * Server-side balance settings. Projected values from docs/beheading.md:
 * enchantment I-III = +5/+10/+20 pp, upgrade levels 1-4 = +10/+17/+22/+25 pp.
 * All probabilities are fractions in [0, 1].
 */
public final class BeheadingConfigSpec {
    private BeheadingConfigSpec() {}

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_ENCHANTMENTS;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_SILENTGEAR_INTEGRATION;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_MATERIAL_TRAITS;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_UPGRADE_MODULES;
    public static final ModConfigSpec.ConfigValue<Double> MATERIAL_TRAIT_BONUS_PER_LEVEL;
    public static final ModConfigSpec.ConfigValue<List<? extends Double>> ENCHANTMENT_BONUSES;
    public static final ModConfigSpec.ConfigValue<List<? extends Double>> UPGRADE_BONUSES;
    public static final ModConfigSpec.ConfigValue<Double> MAXIMUM_HEAD_DROP_CHANCE;
    public static final ModConfigSpec.ConfigValue<StackingMode> STACKING_MODE;
    public static final ModConfigSpec.ConfigValue<Boolean> REQUIRE_PLAYER_KILL;
    public static final ModConfigSpec.ConfigValue<Boolean> ALLOW_PROJECTILE_KILLS;
    public static final ModConfigSpec.ConfigValue<Boolean> ALLOW_AUTOMATION_KILLS;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_VANILLA_HEADS;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_MODDED_HEADS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOB_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MOB_WHITELIST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("sources");
        ENABLE_ENCHANTMENTS = builder.comment("Enable the beheading enchantment (I-III) as a bonus source.")
                .define("enableEnchantments", true);
        ENABLE_SILENTGEAR_INTEGRATION = builder.comment("Master switch for Silent Gear material trait and upgrade module bonuses.")
                .define("enableSilentGearIntegration", true);
        ENABLE_MATERIAL_TRAITS = builder.comment("Read the Silent Gear beheading material trait (only when materials actually define it).")
                .define("enableMaterialTraits", true);
        ENABLE_UPGRADE_MODULES = builder.comment("Read Silent Gear upgrade module levels as a bonus source.")
                .define("enableUpgradeModules", true);
        MATERIAL_TRAIT_BONUS_PER_LEVEL = builder
                .comment("Bonus added per level of the Silent Gear beheading material trait (levels come from materials, not from this mod).")
                .defineInRange("materialTraitBonusPerLevel", 0.05D, 0.0D, 1.0D);
        ENCHANTMENT_BONUSES = builder
                .comment("Bonus per enchantment level (index 0 = level I). Higher levels saturate to the last entry.", "All values are fractions of one: 0.05 = +5 percentage points.")
                .defineListAllowEmpty("enchantmentBonuses", List.of(0.05D, 0.10D, 0.20D),
                        element -> element instanceof Double d && d >= 0.0D && d <= 1.0D);
        UPGRADE_BONUSES = builder
                .comment("Bonus per upgrade level (index 0 = level 1). Higher levels saturate to the last entry.")
                .defineListAllowEmpty("upgradeBonuses", List.of(0.10D, 0.17D, 0.22D, 0.25D),
                        element -> element instanceof Double d && d >= 0.0D && d <= 1.0D);
        builder.pop();

        builder.push("limits");
        MAXIMUM_HEAD_DROP_CHANCE = builder
                .comment("Upper bound of the final head chance. Never reduces an existing or guaranteed provider drop.")
                .defineInRange("maximumHeadDropChance", 1.0D, 0.0D, 1.0D);
        STACKING_MODE = builder.comment("How bonus sources combine before the cap: MAX, ADD_CAPPED, ENCHANTMENT_ONLY, GEAR_ONLY.")
                .defineEnum("stackingMode", StackingMode.MAX);
        builder.pop();

        builder.push("killing");
        REQUIRE_PLAYER_KILL = builder.comment("Only deaths where the killing hit came from a player grant the bonus roll.")
                .define("requirePlayerKill", true);
        ALLOW_PROJECTILE_KILLS = builder.comment("When projectiles kill, the thrower's main-hand enchantment still counts.")
                .define("allowProjectileKills", false);
        ALLOW_AUTOMATION_KILLS = builder.comment("Allow non-player kills (mobs, dispensers, redstone) to grant the bonus roll.")
                .define("allowAutomationKills", false);
        builder.pop();

        builder.push("heads");
        ENABLE_VANILLA_HEADS = builder.comment("Roll bonus heads for vanilla (minecraft-namespace) mobs.")
                .define("enableVanillaHeads", true);
        ENABLE_MODDED_HEADS = builder.comment("Roll bonus heads for non-vanilla mobs registered in the heads data.")
                .define("enableModdedHeads", true);
        MOB_BLACKLIST = builder.comment("Entity IDs that never receive a bonus head.", "Entries are entity IDs with namespace, e.g. minecraft:creeper.")
                .defineListAllowEmpty("mobBlacklist", List.of(), element -> element instanceof String s && !s.isEmpty());
        MOB_WHITELIST = builder.comment("When non-empty, only these entity IDs may receive a bonus head.")
                .defineListAllowEmpty("mobWhitelist", List.of(), element -> element instanceof String s && !s.isEmpty());
        builder.pop();

        SPEC = builder.build();
    }
}
