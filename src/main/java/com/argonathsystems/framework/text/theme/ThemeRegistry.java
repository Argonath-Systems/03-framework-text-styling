package com.argonathsystems.framework.text.theme;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Global registry for themes with hot-swap support.
 * Allows runtime theme changes and listeners for theme switch events.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * ThemeRegistry registry = ThemeRegistry.get();
 * 
 * // Register themes
 * registry.register(lightTheme);
 * registry.register(darkTheme);
 * 
 * // Set active theme
 * registry.setActive("dark");
 * 
 * // Get current theme
 * Theme current = registry.active();
 * 
 * // Listen for theme changes
 * registry.onThemeChange(theme -> {
 *     System.out.println("Theme changed to: " + theme.name());
 * });
 * }</pre>
 */
public final class ThemeRegistry {
    
    private static final ThemeRegistry INSTANCE = new ThemeRegistry();
    
    private final Map<String, Theme> themes;
    private final AtomicReference<Theme> activeTheme;
    private final Map<String, Consumer<Theme>> listeners;
    
    public ThemeRegistry() {
        this.themes = new ConcurrentHashMap<>();
        this.activeTheme = new AtomicReference<>(Theme.defaults());
        this.listeners = new ConcurrentHashMap<>();
        
        // Register the default theme
        register(Theme.defaults());
    }
    
    /**
     * Returns the global registry instance.
     * 
     * @return The singleton registry
     */
    public static ThemeRegistry get() {
        return INSTANCE;
    }
    
    /**
     * Registers a theme.
     * 
     * @param theme The theme to register
     */
    public void register(@NotNull Theme theme) {
        Objects.requireNonNull(theme, "theme cannot be null");
        themes.put(theme.name(), theme);
    }
    
    /**
     * Unregisters a theme by name.
     * Cannot unregister the currently active theme.
     * 
     * @param name The theme name
     * @return true if the theme was removed
     * @throws IllegalStateException if trying to unregister the active theme
     */
    public boolean unregister(@NotNull String name) {
        Theme active = activeTheme.get();
        if (active != null && active.name().equals(name)) {
            throw new IllegalStateException("Cannot unregister the active theme: " + name);
        }
        return themes.remove(name) != null;
    }
    
    /**
     * Gets a theme by name.
     * 
     * @param name The theme name
     * @return The theme, or null if not found
     */
    public @Nullable Theme get(@NotNull String name) {
        return themes.get(name);
    }
    
    /**
     * Gets a theme by name, wrapped in Optional.
     * 
     * @param name The theme name
     * @return Optional containing the theme
     */
    public Optional<Theme> getOptional(@NotNull String name) {
        return Optional.ofNullable(themes.get(name));
    }
    
    /**
     * Checks if a theme is registered.
     * 
     * @param name The theme name
     * @return true if the theme exists
     */
    public boolean has(@NotNull String name) {
        return themes.containsKey(name);
    }
    
    /**
     * Returns all registered theme names.
     * 
     * @return Iterable of theme names
     */
    public Iterable<String> themeNames() {
        return themes.keySet();
    }
    
    /**
     * Returns the currently active theme.
     * 
     * @return The active theme
     */
    public @NotNull Theme active() {
        return activeTheme.get();
    }
    
    /**
     * Sets the active theme by name.
     * Notifies all listeners of the change.
     * 
     * @param name The theme name
     * @throws IllegalArgumentException if the theme is not registered
     */
    public void setActive(@NotNull String name) {
        Theme theme = themes.get(name);
        if (theme == null) {
            throw new IllegalArgumentException("Theme not registered: " + name);
        }
        
        Theme previous = activeTheme.getAndSet(theme);
        
        // Notify listeners if theme actually changed
        if (previous == null || !previous.name().equals(theme.name())) {
            notifyListeners(theme);
        }
    }
    
    /**
     * Sets the active theme directly.
     * Also registers the theme if not already registered.
     * 
     * @param theme The theme to activate
     */
    public void setActive(@NotNull Theme theme) {
        Objects.requireNonNull(theme, "theme cannot be null");
        
        if (!themes.containsKey(theme.name())) {
            register(theme);
        }
        
        Theme previous = activeTheme.getAndSet(theme);
        
        if (previous == null || !previous.name().equals(theme.name())) {
            notifyListeners(theme);
        }
    }
    
    /**
     * Registers a listener for theme changes.
     * 
     * @param listenerId Unique identifier for this listener
     * @param listener The callback to invoke on theme change
     */
    public void onThemeChange(@NotNull String listenerId, @NotNull Consumer<Theme> listener) {
        listeners.put(listenerId, listener);
    }
    
    /**
     * Registers a listener with an auto-generated ID.
     * 
     * @param listener The callback to invoke on theme change
     * @return The generated listener ID
     */
    public String onThemeChange(@NotNull Consumer<Theme> listener) {
        String id = "listener-" + System.nanoTime();
        listeners.put(id, listener);
        return id;
    }
    
    /**
     * Removes a theme change listener.
     * 
     * @param listenerId The listener ID to remove
     * @return true if the listener was removed
     */
    public boolean removeListener(@NotNull String listenerId) {
        return listeners.remove(listenerId) != null;
    }
    
    /**
     * Clears all listeners.
     */
    public void clearListeners() {
        listeners.clear();
    }
    
    private void notifyListeners(Theme newTheme) {
        for (Consumer<Theme> listener : listeners.values()) {
            try {
                listener.accept(newTheme);
            } catch (Exception e) {
                // Log but don't propagate listener exceptions
                System.err.println("Theme change listener threw exception: " + e.getMessage());
            }
        }
    }
    
    /**
     * Resets the registry to its initial state with only the default theme.
     */
    public void reset() {
        themes.clear();
        listeners.clear();
        Theme defaultTheme = Theme.defaults();
        register(defaultTheme);
        activeTheme.set(defaultTheme);
    }
}
