package ru.mehtm.beheading.sg;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import ru.mehtm.beheading.Beheading;

/**
 * The four Silent Gear upgrade-module items consumed at the gear smithing table.
 * Each item maps to one Silent Gear part (data/beheading/silentgear_parts) that declares the
 * {@code beheading:beheading_upgrade} trait at this tier; the smithing recipe of Silent Gear
 * already rejects a duplicate of the same part, so the modules are consumed one at a time.
 */
public final class BeheadingUpgradeItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Beheading.MOD_ID);

    public static final DeferredItem<net.minecraft.world.item.Item> UPGRADE_1 =
            ITEMS.registerItem("beheading_upgrade_1", net.minecraft.world.item.Item::new);
    public static final DeferredItem<net.minecraft.world.item.Item> UPGRADE_2 =
            ITEMS.registerItem("beheading_upgrade_2", net.minecraft.world.item.Item::new);
    public static final DeferredItem<net.minecraft.world.item.Item> UPGRADE_3 =
            ITEMS.registerItem("beheading_upgrade_3", net.minecraft.world.item.Item::new);
    public static final DeferredItem<net.minecraft.world.item.Item> UPGRADE_4 =
            ITEMS.registerItem("beheading_upgrade_4", net.minecraft.world.item.Item::new);

    private BeheadingUpgradeItems() {}
}
