package com.argonathsystems.framework.text.theme;

import com.argonathsystems.framework.text.Style;
import com.argonathsystems.framework.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A complete theme containing a color palette and pre-defined styles.
 * Themes can be used to apply consistent styling across an application.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * Theme darkTheme = Theme.builder("dark")
 *     .palette(darkPalette)
 *     .style("header", Style.builder().bold(true).color(TextColor.GOLD).build())
 *     .style("body", Style.of(TextColor.GRAY))
 *     .style("error", Style.builder().color(TextColor.RED).bold(true).build())
 *     .build();
 * 
 * Style headerStyle = darkTheme.style("header");
 * TextColor primaryColor = darkTheme.color("primary");
 * }</pre>
 */
public final class Theme {
    
    private final String name;
    private final Palette palette;
    private final Map<String, Style> styles;
    private final ThemeMetadata metadata;
    
    private Theme(@NotNull String name, @NotNull Palette palette,
                  @NotNull Map<String, Style> styles, @NotNull ThemeMetadata metadata) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.palette = Objects.requireNonNull(palette, "palette cannot be null");
        this.styles = Map.copyOf(styles);
        this.metadata = Objects.requireNonNull(metadata, "metadata cannot be null");
    }
    
    /**
     * Creates a new theme builder.
     * 
     * @param name The theme name
     * @return A new builder
     */
    public static Builder builder(@NotNull String name) {
        return new Builder(name);
    }
    
    /**
     * Creates a default theme with standard colors and styles.
     * 
     * @return The default theme
     */
    public static Theme defaults() {
        Palette defaultPalette = Palette.defaults();
        
        return builder("default")
            .palette(defaultPalette)
            .style("header", Style.builder()
                .color(defaultPalette.get("primary"))
                .bold(true)
                .build())
            .style("body", Style.builder()
                .color(TextColor.WHITE)
                .build())
            .style("muted", Style.builder()
                .color(defaultPalette.get("muted"))
                .italic(true)
                .build())
            .style("error", Style.builder()
                .color(defaultPalette.get("danger"))
                .bold(true)
                .build())
            .style("success", Style.builder()
                .color(defaultPalette.get("success"))
                .build())
            .style("warning", Style.builder()
                .color(defaultPalette.get("warning"))
                .build())
            .style("link", Style.builder()
                .color(defaultPalette.get("info"))
                .underlined(true)
                .build())
            .build();
    }
    
    /**
     * Returns the theme name.
     * 
     * @return The theme name
     */
    public String name() {
        return name;
    }
    
    /**
     * Returns the color palette for this theme.
     * 
     * @return The palette
     */
    public Palette palette() {
        return palette;
    }
    
    /**
     * Returns the theme metadata.
     * 
     * @return The metadata
     */
    public ThemeMetadata metadata() {
        return metadata;
    }
    
    /**
     * Gets a color from the palette by semantic key.
     * 
     * @param key The color key
     * @return The color, or null if not found
     */
    public @Nullable TextColor color(@NotNull String key) {
        return palette.get(key);
    }
    
    /**
     * Gets a color from the palette, with a fallback.
     * 
     * @param key The color key
     * @param fallback The fallback color
     * @return The color or fallback
     */
    public @NotNull TextColor colorOrDefault(@NotNull String key, @NotNull TextColor fallback) {
        return palette.getOrDefault(key, fallback);
    }
    
    /**
     * Gets a style by name.
     * 
     * @param name The style name
     * @return The style, or null if not found
     */
    public @Nullable Style style(@NotNull String name) {
        return styles.get(name);
    }
    
    /**
     * Gets a style by name, wrapped in Optional.
     * 
     * @param name The style name
     * @return Optional containing the style
     */
    public Optional<Style> styleOptional(@NotNull String name) {
        return Optional.ofNullable(styles.get(name));
    }
    
    /**
     * Gets a style by name, with a fallback.
     * 
     * @param name The style name
     * @param fallback The fallback style
     * @return The style or fallback
     */
    public @NotNull Style styleOrDefault(@NotNull String name, @NotNull Style fallback) {
        return styles.getOrDefault(name, fallback);
    }
    
    /**
     * Checks if a style exists.
     * 
     * @param name The style name
     * @return true if the style exists
     */
    public boolean hasStyle(@NotNull String name) {
        return styles.containsKey(name);
    }
    
    /**
     * Returns all style names.
     * 
     * @return Iterable of style names
     */
    public Iterable<String> styleNames() {
        return styles.keySet();
    }
    
    // =========================================================================
    // Metadata
    // =========================================================================
    
    /**
     * Metadata about a theme (author, version, description).
     */
    public record ThemeMetadata(
        @Nullable String author,
        @Nullable String version,
        @Nullable String description,
        boolean isDarkTheme
    ) {
        public static ThemeMetadata empty() {
            return new ThemeMetadata(null, null, null, false);
        }
        
        public static ThemeMetadata of(String author, String version, String description, boolean isDark) {
            return new ThemeMetadata(author, version, description, isDark);
        }
    }
    
    // =========================================================================
    // Builder
    // =========================================================================
    
    public static final class Builder {
        private final String name;
        private Palette palette = Palette.defaults();
        private final Map<String, Style> styles = new HashMap<>();
        private String author;
        private String version = "1.0.0";
        private String description;
        private boolean isDarkTheme = false;
        
        private Builder(String name) {
            this.name = name;
        }
        
        public Builder palette(@NotNull Palette palette) {
            this.palette = palette;
            return this;
        }
        
        public Builder style(@NotNull String name, @NotNull Style style) {
            styles.put(name, style);
            return this;
        }
        
        public Builder author(@Nullable String author) {
            this.author = author;
            return this;
        }
        
        public Builder version(@Nullable String version) {
            this.version = version;
            return this;
        }
        
        public Builder description(@Nullable String description) {
            this.description = description;
            return this;
        }
        
        public Builder darkTheme(boolean isDark) {
            this.isDarkTheme = isDark;
            return this;
        }
        
        public Theme build() {
            ThemeMetadata metadata = new ThemeMetadata(author, version, description, isDarkTheme);
            return new Theme(name, palette, styles, metadata);
        }
    }
}
