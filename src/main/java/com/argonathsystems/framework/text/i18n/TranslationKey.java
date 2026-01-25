package com.argonathsystems.framework.text.i18n;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a translation key that maps to localized strings.
 * Translation keys follow the format: "namespace.category.key"
 * Examples: "ui.button.confirm", "quest.tutorial.welcome", "item.legendary_sword.name"
 * 
 * @param namespace The mod/plugin namespace (e.g., "core", "quest-mod")
 * @param key The full key path (e.g., "ui.button.confirm")
 */
public record TranslationKey(@NotNull String namespace, @NotNull String key) {
    
    public TranslationKey {
        Objects.requireNonNull(namespace, "namespace cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        if (namespace.isBlank()) {
            throw new IllegalArgumentException("namespace cannot be blank");
        }
        if (key.isBlank()) {
            throw new IllegalArgumentException("key cannot be blank");
        }
    }
    
    /**
     * Creates a translation key with the default namespace "core".
     * 
     * @param key The translation key path
     * @return A new TranslationKey
     */
    public static TranslationKey of(@NotNull String key) {
        return new TranslationKey("core", key);
    }
    
    /**
     * Creates a translation key with a specific namespace.
     * 
     * @param namespace The namespace
     * @param key The translation key path
     * @return A new TranslationKey
     */
    public static TranslationKey of(@NotNull String namespace, @NotNull String key) {
        return new TranslationKey(namespace, key);
    }
    
    /**
     * Parses a translation key from a string.
     * Format: "namespace:key" or just "key" (uses default namespace "core")
     * 
     * @param keyString The key string to parse
     * @return A new TranslationKey
     */
    public static TranslationKey parse(@NotNull String keyString) {
        Objects.requireNonNull(keyString, "keyString cannot be null");
        int colonIndex = keyString.indexOf(':');
        if (colonIndex > 0) {
            String namespace = keyString.substring(0, colonIndex);
            String key = keyString.substring(colonIndex + 1);
            return new TranslationKey(namespace, key);
        }
        return new TranslationKey("core", keyString);
    }
    
    /**
     * Returns the full key in the format "namespace:key".
     * 
     * @return The full key string
     */
    public String fullKey() {
        return namespace + ":" + key;
    }
    
    @Override
    public String toString() {
        return fullKey();
    }
}
