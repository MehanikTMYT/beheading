package ru.mehtm.beheading.sg;

import net.minecraft.resources.Identifier;
import ru.mehtm.beheading.Beheading;

/** Resource ids of the two Silent Gear traits used by the head-bonus adapter. */
public final class SilentGearTraitIds {
    /**
     * Material trait: levels come from gear materials (SG merges them with its own doctrine),
     * bonus is {@code level * materialTraitBonusPerLevel}.
     */
    public static final Identifier MATERIAL = Identifier.fromNamespaceAndPath(Beheading.MOD_ID, "beheading");

    /**
     * Upgrade module trait: declared by Beheading's own Silent Gear upgrade parts.
     * The level is read from the applied part itself (1..4), never through the merged
     * trait math, so the four configured tiers stay exact and never stack with the
     * material trait bonus.
     */
    public static final Identifier UPGRADE = Identifier.fromNamespaceAndPath(Beheading.MOD_ID, "beheading_upgrade");

    private SilentGearTraitIds() {}
}
