package com.argonathsystems.framework.text.placeholder;

import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context provided to a PlaceholderResolver when resolving a placeholder.
 * Contains the key and optional parameters from the placeholder syntax.
 * 
 * <p>Placeholder format: {@code {key}} or {@code {key:param1:param2}}</p>
 * 
 * @param key The placeholder key (e.g., "player_name", "balance")
 * @param params Optional parameters split by colons
 * @param viewerId The UUID of the viewer (if available) as a String
 */
public record PlaceholderContext(
    @NotNull String key,
    @NotNull List<String> params,
    @Nullable String viewerId
) {
    public PlaceholderContext {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(params, "params cannot be null");
    }
    
    /**
     * Creates a simple context with just a key.
     */
    public static PlaceholderContext of(@NotNull String key) {
        return new PlaceholderContext(key, List.of(), null);
    }
    
    /**
     * Creates a context with a key and viewer ID.
     */
    public static PlaceholderContext of(@NotNull String key, @Nullable String viewerId) {
        return new PlaceholderContext(key, List.of(), viewerId);
    }
    
    /**
     * Creates a context with a key and parameters.
     */
    public static PlaceholderContext of(@NotNull String key, @NotNull List<String> params) {
        return new PlaceholderContext(key, params, null);
    }
    
    /**
     * Parses a placeholder string like "key" or "key:param1:param2".
     */
    public static PlaceholderContext parse(@NotNull String placeholder, @Nullable String viewerId) {
        if (placeholder.contains(":")) {
            String[] parts = placeholder.split(":", -1);
            String key = parts[0];
            List<String> params = List.of(parts).subList(1, parts.length);
            return new PlaceholderContext(key, params, viewerId);
        }
        return new PlaceholderContext(placeholder, List.of(), viewerId);
    }
    
    /**
     * Gets the first parameter, if any.
     */
    public @Nullable String param(int index) {
        return index < params.size() ? params.get(index) : null;
    }
    
    /**
     * Gets a parameter with a default value.
     */
    public @NotNull String param(int index, @NotNull String defaultValue) {
        return index < params.size() ? params.get(index) : defaultValue;
    }
    
    /**
     * Checks if parameters are present.
     */
    public boolean hasParams() {
        return !params.isEmpty();
    }
}
