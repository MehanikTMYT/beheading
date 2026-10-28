package ru.mehtm.beheading;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import ru.mehtm.beheading.config.BeheadingConfigSpec;
import ru.mehtm.beheading.loot.HeadChanceRoller;
import ru.mehtm.beheading.loot.MobHeadLootModifier;
import ru.mehtm.beheading.loot.MobHeads;

/**
 * Beheading — single bonus head-drop roll per death.
 * The enchantment is data-driven (data/beheading/enchantment/beheading.json); the global loot
 * modifier performs the one allowed random draw through BeheadingCalculator.
 */
@Mod(Beheading.MOD_ID)
public final class Beheading {
    public static final String MOD_ID = "beheading";

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<MobHeadLootModifier>> MOB_HEADS =
            LOOT_MODIFIERS.register("mob_heads", MobHeadLootModifier.CODEC);

    public Beheading(IEventBus modBus, ModContainer container) {
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, BeheadingConfigSpec.SPEC);
        LOOT_MODIFIERS.register(modBus);

        // The balance starts as DEFAULT: the config file loads later (SERVER configs load with
        // the server), so reading values here would just throw and warn. ModConfigEvent.Loading
        // and Reloading build the real snapshot as soon as the config is available.
        modBus.addListener(this::onConfigLoad);
        modBus.addListener(this::onConfigReload);

        NeoForge.EVENT_BUS.addListener(this::onServerReloadListeners);
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == BeheadingConfigSpec.SPEC) {
            HeadChanceRoller.refreshBalance();
        }
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == BeheadingConfigSpec.SPEC) {
            HeadChanceRoller.refreshBalance();
        }
    }

    private void onServerReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(MOD_ID, "mob_heads"), MobHeads.reloadListener());
    }
}
