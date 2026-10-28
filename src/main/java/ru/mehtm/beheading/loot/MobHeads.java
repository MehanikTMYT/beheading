package ru.mehtm.beheading.loot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import com.mojang.serialization.Codec;

/**
 * Mob -> head item map, loaded from {@code data/<any namespace>/heads/*.json}.
 * The default file lives in the mod jar; other mods and datapacks add or override
 * entries through the same directory. Missing entries mean "no head" — the roll never
 * invents items. Values include every head item to block double drops from other sources.
 */
public final class MobHeads {
    private static final Codec<Map<Identifier, Identifier>> CODEC =
            Codec.unboundedMap(Identifier.CODEC, Identifier.CODEC);

    private static volatile Map<Identifier, Identifier> heads = Map.of();
    private static volatile Set<Identifier> headItems = Set.of();

    private MobHeads() {}

    /** @return the configured head item id for the mob, or null when the mob has no head entry. */
    public static Identifier headItemOf(EntityType<?> mob) {
        return heads.get(BuiltInRegistries.ENTITY_TYPE.getKey(mob));
    }

    /** @return true when the loot already contains any configured head item — no second roll is allowed. */
    public static boolean alreadyDroppedHead(java.util.List<net.minecraft.world.item.ItemStack> loot) {
        for (var stack : loot) {
            if (stack != null && !stack.isEmpty() && headItems.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHeadItem(Item item) {
        return headItems.contains(BuiltInRegistries.ITEM.getKey(item));
    }

    public static PreparableReloadListener reloadListener() {
        // prepare() is inherited: it scans heads/*.json into Map<Identifier, Map<Identifier, Identifier>>.
        return new SimpleJsonResourceReloadListener<Map<Identifier, Identifier>>(CODEC, FileToIdConverter.json("heads")) {
            @Override
            protected void apply(Map<Identifier, Map<Identifier, Identifier>> files,
                    ResourceManager manager, ProfilerFiller profiler) {
                Map<Identifier, Identifier> merged = new HashMap<>();
                Set<Identifier> items = new HashSet<>();
                for (var perFile : files.values()) {
                    for (var entry : perFile.entrySet()) {
                        if (entry.getValue() != null && BuiltInRegistries.ITEM.getOptional(entry.getValue()).isPresent()) {
                            merged.put(entry.getKey(), entry.getValue());
                            items.add(entry.getValue());
                        }
                    }
                }
                heads = Map.copyOf(merged);
                headItems = Set.copyOf(items);
            }
        };
    }
}
