package com.argonathsystems.framework.text;

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
    
    public static TextColor of(int rgb) {
        return new TextColor(rgb, null);
    }

    public static TextColor fromHex(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        return of(Integer.parseInt(hex, 16));
    }
    
    public String toHex() {
        return String.format("#%06X", (0xFFFFFF & value));
    }
}
