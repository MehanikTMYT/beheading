package ru.mehtm.beheading.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import ru.mehtm.beheading.Beheading;

/** Resource keys of the data-driven content shipped by Beheading. */
public final class BeheadingKeys {
    private BeheadingKeys() {}

    public static final ResourceKey<Enchantment> ENCHANTMENT =
            ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(Beheading.MOD_ID, "beheading"));

    public static Identifier enchantmentId() {
        return ENCHANTMENT.identifier();
    }
}
