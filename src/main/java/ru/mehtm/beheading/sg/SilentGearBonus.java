package ru.mehtm.beheading.sg;

/** Resolved Silent Gear contributions for one death. */
public record SilentGearBonus(int upgradeLevel, double traitBonus) {
    public static final SilentGearBonus ZERO = new SilentGearBonus(0, 0.0D);
}
