package com.argonathsystems.framework.text.format;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Context object passed to ObjectRenderers to provide additional
 * styling information and viewer context.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * StyleContext context = StyleContext.builder()
 *     .viewerId("player-uuid")
 *     .theme("dark")
 *     .put("showDetails", true)
 *     .build();
 * 
 * Component rendered = renderer.render(item, context);
 * }</pre>
 */
public final class StyleContext {
    
    private final String viewerId;
    private final String themeName;
    private final Map<String, ContextValue> properties;
    
    private StyleContext(@Nullable String viewerId, @Nullable String themeName,
                         @NotNull Map<String, ContextValue> properties) {
        this.viewerId = viewerId;
        this.themeName = themeName;
        this.properties = Map.copyOf(properties);
    }
    
    /**
     * Creates a new builder for StyleContext.
     * 
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates an empty context.
     * 
     * @return An empty StyleContext
     */
    public static StyleContext empty() {
        return new Builder().build();
    }
    
    /**
     * Creates a context with just a viewer ID.
     * 
     * @param viewerId The viewer's UUID as string
     * @return A new StyleContext
     */
    public static StyleContext forViewer(@NotNull String viewerId) {
        return new Builder().viewerId(viewerId).build();
    }
    
    /**
     * Returns the viewer's ID (typically UUID as string).
     * 
     * @return The viewer ID, or null if not set
     */
    public @Nullable String viewerId() {
        return viewerId;
    }
    
    /**
     * Returns the theme name to use for styling.
     * 
     * @return The theme name, or null for default
     */
    public @Nullable String themeName() {
        return themeName;
    }
    
    /**
     * Gets a property value by key.
     * 
     * @param key The property key
     * @return Optional containing the value, or empty
     */
    public Optional<ContextValue> get(@NotNull String key) {
        return Optional.ofNullable(properties.get(key));
    }
    
    /**
     * Gets a string property.
     * 
     * @param key The property key
     * @return Optional containing the string value
     */
    public Optional<String> getString(@NotNull String key) {
        return get(key).flatMap(ContextValue::asString);
    }
    
    /**
     * Gets a boolean property.
     * 
     * @param key The property key
     * @return Optional containing the boolean value
     */
    public Optional<Boolean> getBoolean(@NotNull String key) {
        return get(key).flatMap(ContextValue::asBoolean);
    }
    
    /**
     * Gets a long property.
     * 
     * @param key The property key
     * @return Optional containing the long value
     */
    public Optional<Long> getLong(@NotNull String key) {
        return get(key).flatMap(ContextValue::asLong);
    }
    
    /**
     * Gets a double property.
     * 
     * @param key The property key
     * @return Optional containing the double value
     */
    public Optional<Double> getDouble(@NotNull String key) {
        return get(key).flatMap(ContextValue::asDouble);
    }
    
    /**
     * Checks if a property exists.
     * 
     * @param key The property key
     * @return true if the property exists
     */
    public boolean has(@NotNull String key) {
        return properties.containsKey(key);
    }
    
    // =========================================================================
    // Type-safe Context Values
    // =========================================================================
    
    /**
     * A type-safe context value. Replaces raw Object usage.
     */
    public sealed interface ContextValue permits
            StringContextValue, LongContextValue, DoubleContextValue, BoolContextValue {
        
        Optional<String> asString();
        Optional<Long> asLong();
        Optional<Double> asDouble();
        Optional<Boolean> asBoolean();
        
        static ContextValue of(String value) { return new StringContextValue(value); }
        static ContextValue of(long value) { return new LongContextValue(value); }
        static ContextValue of(double value) { return new DoubleContextValue(value); }
        static ContextValue of(boolean value) { return new BoolContextValue(value); }
    }
    
    record StringContextValue(String value) implements ContextValue {
        @Override public Optional<String> asString() { return Optional.of(value); }
        @Override public Optional<Long> asLong() { 
            try { return Optional.of(Long.parseLong(value)); } 
            catch (NumberFormatException e) { return Optional.empty(); }
        }
        @Override public Optional<Double> asDouble() { 
            try { return Optional.of(Double.parseDouble(value)); } 
            catch (NumberFormatException e) { return Optional.empty(); }
        }
        @Override public Optional<Boolean> asBoolean() { return Optional.of(Boolean.parseBoolean(value)); }
    }
    
    record LongContextValue(long value) implements ContextValue {
        @Override public Optional<String> asString() { return Optional.of(String.valueOf(value)); }
        @Override public Optional<Long> asLong() { return Optional.of(value); }
        @Override public Optional<Double> asDouble() { return Optional.of((double) value); }
        @Override public Optional<Boolean> asBoolean() { return Optional.of(value != 0); }
    }
    
    record DoubleContextValue(double value) implements ContextValue {
        @Override public Optional<String> asString() { return Optional.of(String.valueOf(value)); }
        @Override public Optional<Long> asLong() { return Optional.of((long) value); }
        @Override public Optional<Double> asDouble() { return Optional.of(value); }
        @Override public Optional<Boolean> asBoolean() { return Optional.of(value != 0.0); }
    }
    
    record BoolContextValue(boolean value) implements ContextValue {
        @Override public Optional<String> asString() { return Optional.of(String.valueOf(value)); }
        @Override public Optional<Long> asLong() { return Optional.of(value ? 1L : 0L); }
        @Override public Optional<Double> asDouble() { return Optional.of(value ? 1.0 : 0.0); }
        @Override public Optional<Boolean> asBoolean() { return Optional.of(value); }
    }
    
    // =========================================================================
    // Builder
    // =========================================================================
    
    public static final class Builder {
        private String viewerId;
        private String themeName;
        private final Map<String, ContextValue> properties = new HashMap<>();
        
        private Builder() {}
        
        public Builder viewerId(@Nullable String viewerId) {
            this.viewerId = viewerId;
            return this;
        }
        
        public Builder theme(@Nullable String themeName) {
            this.themeName = themeName;
            return this;
        }
        
        public Builder put(@NotNull String key, @NotNull String value) {
            properties.put(key, ContextValue.of(value));
            return this;
        }
        
        public Builder put(@NotNull String key, long value) {
            properties.put(key, ContextValue.of(value));
            return this;
        }
        
        public Builder put(@NotNull String key, double value) {
            properties.put(key, ContextValue.of(value));
            return this;
        }
        
        public Builder put(@NotNull String key, boolean value) {
            properties.put(key, ContextValue.of(value));
            return this;
        }
        
        public StyleContext build() {
            return new StyleContext(viewerId, themeName, properties);
        }
    }
}
