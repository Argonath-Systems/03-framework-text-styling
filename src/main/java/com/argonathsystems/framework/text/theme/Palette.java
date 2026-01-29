package com.argonathsystems.framework.text.theme;

import com.argonathsystems.framework.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A named color palette containing semantic color definitions.
 * Palettes allow defining colors by semantic meaning (e.g., "rarity.legendary")
 * rather than raw hex values.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * Palette argonath = Palette.builder("argonath")
 *     .color("primary", TextColor.GOLD)
 *     .color("secondary", TextColor.of(0x3498db))
 *     .color("success", TextColor.GREEN)
 *     .color("danger", TextColor.RED)
 *     .color("rarity.common", TextColor.WHITE)
 *     .color("rarity.uncommon", TextColor.GREEN)
 *     .color("rarity.rare", TextColor.BLUE)
 *     .color("rarity.epic", TextColor.of(0xa335ee))
 *     .color("rarity.legendary", TextColor.of(0xff8000))
 *     .build();
 * 
 * TextColor legendary = argonath.get("rarity.legendary");
 * }</pre>
 */
public final class Palette {
    
    private final String name;
    private final Map<String, TextColor> colors;
    
    private Palette(@NotNull String name, @NotNull Map<String, TextColor> colors) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.colors = Map.copyOf(colors);
    }
    
    /**
     * Creates a new palette builder.
     * 
     * @param name The palette name
     * @return A new builder
     */
    public static Builder builder(@NotNull String name) {
        return new Builder(name);
    }
    
    /**
     * Creates an empty palette with no colors.
     * 
     * @param name The palette name
     * @return An empty palette
     */
    public static Palette empty(@NotNull String name) {
        return new Palette(name, Map.of());
    }
    
    /**
     * Creates a default palette with standard semantic colors.
     * 
     * @return The default palette
     */
    public static Palette defaults() {
        return builder("default")
            .color("primary", TextColor.GOLD)
            .color("secondary", TextColor.GRAY)
            .color("success", TextColor.GREEN)
            .color("danger", TextColor.RED)
            .color("warning", new TextColor(0xFFAA00, "gold"))
            .color("info", TextColor.BLUE)
            .color("muted", TextColor.GRAY)
            .color("rarity.common", TextColor.WHITE)
            .color("rarity.uncommon", TextColor.GREEN)
            .color("rarity.rare", TextColor.BLUE)
            .color("rarity.epic", new TextColor(0xa335ee, "epic"))
            .color("rarity.legendary", new TextColor(0xff8000, "legendary"))
            .build();
    }
    
    /**
     * Returns the palette name.
     * 
     * @return The name
     */
    public String name() {
        return name;
    }
    
    /**
     * Gets a color by semantic key.
     * 
     * @param key The color key (e.g., "rarity.legendary")
     * @return The color, or null if not found
     */
    public @Nullable TextColor get(@NotNull String key) {
        return colors.get(key);
    }
    
    /**
     * Gets a color by semantic key, wrapped in Optional.
     * 
     * @param key The color key
     * @return Optional containing the color
     */
    public Optional<TextColor> getOptional(@NotNull String key) {
        return Optional.ofNullable(colors.get(key));
    }
    
    /**
     * Gets a color by semantic key, with a fallback.
     * 
     * @param key The color key
     * @param fallback The fallback color if not found
     * @return The color or fallback
     */
    public @NotNull TextColor getOrDefault(@NotNull String key, @NotNull TextColor fallback) {
        return colors.getOrDefault(key, fallback);
    }
    
    /**
     * Checks if a color key exists in this palette.
     * 
     * @param key The color key
     * @return true if the key exists
     */
    public boolean has(@NotNull String key) {
        return colors.containsKey(key);
    }
    
    /**
     * Returns all color keys in this palette.
     * 
     * @return Iterable of color keys
     */
    public Iterable<String> keys() {
        return colors.keySet();
    }
    
    /**
     * Creates a new palette that extends this one with additional colors.
     * 
     * @param additionalColors Colors to add/override
     * @return A new extended palette
     */
    public Palette extend(@NotNull Map<String, TextColor> additionalColors) {
        Map<String, TextColor> merged = new HashMap<>(this.colors);
        merged.putAll(additionalColors);
        return new Palette(this.name + "-extended", merged);
    }
    
    // =========================================================================
    // Builder
    // =========================================================================
    
    public static final class Builder {
        private final String name;
        private final Map<String, TextColor> colors = new HashMap<>();
        
        private Builder(String name) {
            this.name = name;
        }
        
        public Builder color(@NotNull String key, @NotNull TextColor color) {
            colors.put(key, color);
            return this;
        }
        
        public Builder color(@NotNull String key, int rgb) {
            colors.put(key, TextColor.of(rgb));
            return this;
        }
        
        public Builder color(@NotNull String key, @NotNull String hex) {
            colors.put(key, TextColor.fromHex(hex));
            return this;
        }
        
        public Builder copyFrom(@NotNull Palette other) {
            for (String key : other.keys()) {
                TextColor color = other.get(key);
                if (color != null) {
                    colors.put(key, color);
                }
            }
            return this;
        }
        
        public Palette build() {
            return new Palette(name, colors);
        }
    }
}
