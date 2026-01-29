package com.argonathsystems.framework.text;

import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a color in the text styling system.
 * Can be a hex value or a standard name.
 */
public record TextColor(int value, @Nullable String name) {
    
    // Standard Colors
    public static final TextColor WHITE = new TextColor(0xFFFFFF, "white");
    public static final TextColor BLACK = new TextColor(0x000000, "black");
    public static final TextColor RED = new TextColor(0xFF5555, "red");
    public static final TextColor GREEN = new TextColor(0x55FF55, "green");
    public static final TextColor BLUE = new TextColor(0x5555FF, "blue");
    public static final TextColor GOLD = new TextColor(0xFFAA00, "gold");
    public static final TextColor GRAY = new TextColor(0xAAAAAA, "gray");
    public static final TextColor YELLOW = new TextColor(0xFFFF55, "yellow");
    public static final TextColor AQUA = new TextColor(0x55FFFF, "aqua");
    public static final TextColor DARK_RED = new TextColor(0xAA0000, "dark_red");
    public static final TextColor DARK_GREEN = new TextColor(0x00AA00, "dark_green");
    public static final TextColor DARK_BLUE = new TextColor(0x0000AA, "dark_blue");
    public static final TextColor DARK_AQUA = new TextColor(0x00AAAA, "dark_aqua");
    public static final TextColor DARK_GRAY = new TextColor(0x555555, "dark_gray");
    public static final TextColor LIGHT_PURPLE = new TextColor(0xFF55FF, "light_purple");
    public static final TextColor DARK_PURPLE = new TextColor(0xAA00AA, "dark_purple");
    
    private static final Map<String, TextColor> NAMED_COLORS = Map.ofEntries(
        Map.entry("white", WHITE),
        Map.entry("black", BLACK),
        Map.entry("red", RED),
        Map.entry("green", GREEN),
        Map.entry("blue", BLUE),
        Map.entry("gold", GOLD),
        Map.entry("gray", GRAY),
        Map.entry("grey", GRAY),
        Map.entry("yellow", YELLOW),
        Map.entry("aqua", AQUA),
        Map.entry("dark_red", DARK_RED),
        Map.entry("dark_green", DARK_GREEN),
        Map.entry("dark_blue", DARK_BLUE),
        Map.entry("dark_aqua", DARK_AQUA),
        Map.entry("dark_gray", DARK_GRAY),
        Map.entry("dark_grey", DARK_GRAY),
        Map.entry("light_purple", LIGHT_PURPLE),
        Map.entry("dark_purple", DARK_PURPLE)
    );
    
    public static TextColor of(int rgb) {
        return new TextColor(rgb, null);
    }

    public static TextColor fromHex(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        return of(Integer.parseInt(hex, 16));
    }
    
    /**
     * Looks up a named color by name.
     * 
     * @param colorName The color name (e.g., "red", "gold", "dark_purple")
     * @return The TextColor if found, or null
     */
    public static @Nullable TextColor named(String colorName) {
        if (colorName == null) {
            return null;
        }
        return NAMED_COLORS.get(colorName.toLowerCase().replace('-', '_'));
    }
    
    public String toHex() {
        return String.format("#%06X", (0xFFFFFF & value));
    }
}
