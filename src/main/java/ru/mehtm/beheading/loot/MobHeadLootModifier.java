package ru.mehtm.beheading.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import ru.mehtm.beheading.config.BeheadingBalance;
import ru.mehtm.beheading.core.BeheadingCalculator;
import ru.mehtm.beheading.data.BeheadingKeys;

/**
 * The single bonus roll. Applied through NeoForge global loot modifiers with default priority;
 * when another source already produced any configured head item the roll is skipped so that
 * existing, guaranteed or modded head drops are never duplicated or reduced.
 */
public final class MobHeadLootModifier extends LootModifier {
    public static final Supplier<MapCodec<MobHeadLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst ->
                    codecStart(inst).apply(inst, MobHeadLootModifier::new)));

    public MobHeadLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BeheadingBalance balance = HeadChanceRoller.balance();
        Entity victim = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (!(victim instanceof LivingEntity mob)) {
            return generatedLoot;
        }

        Identifier mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        Identifier headItem = MobHeads.headItemOf(mob.getType());
        if (headItem == null) {
            // No configured mapping — this category never invents a random item.
            return generatedLoot;
        }

        if (balance.mobBlacklist().contains(mobId.toString())
                || (!balance.mobWhitelist().isEmpty() && !balance.mobWhitelist().contains(mobId.toString()))) {
            return generatedLoot;
        }

        boolean vanillaMob = "minecraft".equals(mobId.getNamespace());
        if ((vanillaMob && !balance.enableVanillaHeads()) || (!vanillaMob && !balance.enableModdedHeads())) {
            return generatedLoot;
        }

        // Never stack a second head roll on top of an existing or guaranteed provider drop.
        if (MobHeads.alreadyDroppedHead(generatedLoot)) {
            return generatedLoot;
        }

        Player player = context.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER);
        Entity killer = context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);
        if (balance.requirePlayerKill() && player == null && !(killer instanceof Player)) {
            return generatedLoot;
        }
        if (!balance.requirePlayerKill() && killer == null && player == null && !balance.allowAutomationKills()) {
            return generatedLoot;
        }

        ItemStack weapon;
        if (player != null) {
            weapon = player.getMainHandItem();
        } else if (killer instanceof LivingEntity attacker) {
            weapon = attacker.getMainHandItem();
        } else {
            return generatedLoot;
        }
        if (weapon.isEmpty()) {
            return generatedLoot;
        }

        var enchantment = context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(BeheadingKeys.ENCHANTMENT);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantment, weapon);

        var silentGear = ru.mehtm.beheading.sg.SilentGearHeadBonus.resolve(weapon, balance);
        int upgradeLevel = silentGear.upgradeLevel();
        double traitBonus = silentGear.traitBonus();
        if (enchantmentLevel == 0 && upgradeLevel == 0 && traitBonus == 0.0D) {
            return generatedLoot; // No configured bonus source applies.
        }

        // baseChance stays 0 here: any existing provider drop has already been excluded above,
        // so the calculator only turns the configured bonus into one additional roll.
        var result = BeheadingCalculator.calculate(balance.core(), 0.0D, enchantmentLevel, traitBonus, upgradeLevel);
        if (result.finalChance() <= 0.0D) {
            return generatedLoot;
        }

        if (context.getRandom().nextFloat() < result.finalChance()) {
            BuiltInRegistries.ITEM.getOptional(headItem).ifPresent(item ->
                    generatedLoot.add(new ItemStack(item)));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
