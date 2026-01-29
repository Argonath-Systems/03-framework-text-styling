package com.argonathsystems.framework.text.format;

import com.argonathsystems.framework.text.Component;
import com.argonathsystems.framework.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for ObjectRenderers that convert domain objects to styled Components.
 * Supports class hierarchy lookup - if no exact renderer is found, superclasses
 * and interfaces are searched.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * ObjectAdapterRegistry registry = ObjectAdapterRegistry.get();
 * 
 * // Register renderers
 * registry.register(Item.class, itemRenderer);
 * registry.register(Player.class, playerRenderer);
 * 
 * // Render objects
 * Component itemText = registry.render(myItem);
 * Component playerText = registry.render(player, context);
 * }</pre>
 */
public final class ObjectAdapterRegistry {
    
    private static final ObjectAdapterRegistry INSTANCE = new ObjectAdapterRegistry();
    
    private final Map<Class<?>, ObjectRenderer<?>> renderers;
    
    public ObjectAdapterRegistry() {
        this.renderers = new ConcurrentHashMap<>();
        registerDefaults();
    }
    
    /**
     * Returns the global registry instance.
     * 
     * @return The singleton registry
     */
    public static ObjectAdapterRegistry get() {
        return INSTANCE;
    }
    
    /**
     * Registers a renderer for a specific type.
     * 
     * @param type The class to register
     * @param renderer The renderer to use for this type
     * @param <T> The type parameter
     */
    public <T> void register(@NotNull Class<T> type, @NotNull ObjectRenderer<T> renderer) {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(renderer, "renderer cannot be null");
        renderers.put(type, renderer);
    }
    
    /**
     * Unregisters a renderer for a type.
     * 
     * @param type The class to unregister
     */
    public void unregister(@NotNull Class<?> type) {
        renderers.remove(type);
    }
    
    /**
     * Checks if a renderer is registered for a type.
     * 
     * @param type The class to check
     * @return true if a renderer exists
     */
    public boolean hasRenderer(@NotNull Class<?> type) {
        return findRenderer(type).isPresent();
    }
    
    /**
     * Renders an object using the registered renderer.
     * 
     * @param target The object to render
     * @return The rendered component
     * @throws IllegalArgumentException if no renderer is found
     */
    @SuppressWarnings("unchecked")
    public @NotNull Component render(@NotNull Object target) {
        return render(target, null);
    }
    
    /**
     * Renders an object with styling context.
     * 
     * @param target The object to render
     * @param context Optional styling context
     * @return The rendered component
     * @throws IllegalArgumentException if no renderer is found
     */
    @SuppressWarnings("unchecked")
    public @NotNull Component render(@NotNull Object target, @Nullable StyleContext context) {
        Objects.requireNonNull(target, "target cannot be null");
        
        ObjectRenderer<Object> renderer = (ObjectRenderer<Object>) findRenderer(target.getClass())
            .orElseThrow(() -> new IllegalArgumentException(
                "No renderer registered for type: " + target.getClass().getName()));
        
        return renderer.render(target, context);
    }
    
    /**
     * Tries to render an object, returning empty if no renderer is found.
     * 
     * @param target The object to render
     * @return Optional containing the component, or empty
     */
    @SuppressWarnings("unchecked")
    public Optional<Component> tryRender(@NotNull Object target) {
        return tryRender(target, null);
    }
    
    /**
     * Tries to render an object with context, returning empty if no renderer is found.
     * 
     * @param target The object to render
     * @param context Optional styling context
     * @return Optional containing the component, or empty
     */
    @SuppressWarnings("unchecked")
    public Optional<Component> tryRender(@NotNull Object target, @Nullable StyleContext context) {
        Objects.requireNonNull(target, "target cannot be null");
        
        return findRenderer(target.getClass())
            .map(renderer -> ((ObjectRenderer<Object>) renderer).render(target, context));
    }
    
    /**
     * Finds a renderer for a class, searching the class hierarchy.
     */
    private Optional<ObjectRenderer<?>> findRenderer(Class<?> type) {
        // Direct lookup
        ObjectRenderer<?> renderer = renderers.get(type);
        if (renderer != null) {
            return Optional.of(renderer);
        }
        
        // Search superclasses
        Class<?> current = type.getSuperclass();
        while (current != null && current != Object.class) {
            renderer = renderers.get(current);
            if (renderer != null) {
                return Optional.of(renderer);
            }
            current = current.getSuperclass();
        }
        
        // Search interfaces
        for (Class<?> iface : type.getInterfaces()) {
            renderer = renderers.get(iface);
            if (renderer != null) {
                return Optional.of(renderer);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Registers default renderers for common types.
     */
    private void registerDefaults() {
        // String renderer
        register(String.class, (s, ctx) -> new TextComponent(s));
        
        // Number renderers
        register(Number.class, (n, ctx) -> new TextComponent(n.toString()));
        register(Integer.class, (i, ctx) -> new TextComponent(i.toString()));
        register(Long.class, (l, ctx) -> new TextComponent(l.toString()));
        register(Double.class, (d, ctx) -> new TextComponent(String.format("%.2f", d)));
        register(Float.class, (f, ctx) -> new TextComponent(String.format("%.2f", f)));
        
        // Boolean renderer
        register(Boolean.class, (b, ctx) -> new TextComponent(b.toString()));
        
        // Component pass-through (already a component)
        register(Component.class, (c, ctx) -> c);
    }
    
    /**
     * Clears all registered renderers and re-registers defaults.
     */
    public void reset() {
        renderers.clear();
        registerDefaults();
    }
}
