package net.xhugo.cobblemonpcsortplus.config;

import java.util.Locale;

/** Final ordering applied to every occupied box after Sort all. */
public enum BoxSortMode {
    NAME,
    POKEDEX,
    LEVEL,
    NONE;

    public BoxSortMode next() {
        BoxSortMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    public String serializedName() {
        return name();
    }

    public String translationSuffix() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static BoxSortMode from(String value) {
        if (value == null || value.isBlank()) return NAME;
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NAME;
        }
    }
}
