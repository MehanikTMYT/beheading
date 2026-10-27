package ru.mehtm.beheading.core;

/** How independent bonus sources combine before the configured chance limit. */
public enum StackingMode {
    MAX,
    ADD_CAPPED,
    ENCHANTMENT_ONLY,
    GEAR_ONLY
}
