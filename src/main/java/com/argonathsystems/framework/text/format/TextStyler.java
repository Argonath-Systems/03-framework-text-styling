package com.argonathsystems.framework.text.format;

import com.argonathsystems.framework.text.Component;
import com.argonathsystems.framework.text.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Central facade for converting objects and strings into styled text Components.
 * This is the main entry point for the text styling system as specified in SF-009 L2.2.
 * 
 * <h2>Design Goals:</h2>
 * <ul>
 *   <li>Unified API for all text styling operations</li>
 *   <li>Support for MiniMessage parsing</li>
 *   <li>Object-to-Component conversion via registered adapters</li>
 *   <li>Context-aware styling with themes</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * TextStyler styler = TextStyler.get();
 * 
 * // Parse MiniMessage format
 * Component message = styler.parse("<red>Error: </red><white>{message}</white>");
 * 
 * // Style an object (uses registered ObjectRenderer)
 * Component itemDisplay = styler.style(myItem);
 * 
 * // Style with context
 * StyleContext ctx = StyleContext.forViewer(playerId);
 * Component contextual = styler.style(myItem, ctx);
 * }</pre>
 */
public interface TextStyler {
    
    /**
     * Returns the default TextStyler instance.
     * 
     * @return The default styler
     */
    static TextStyler get() {
        return DefaultTextStyler.INSTANCE;
    }
    
    /**
     * Creates a new TextStyler with a custom adapter registry.
     * 
     * @param registry The adapter registry to use
     * @return A new TextStyler
     */
    static TextStyler create(@NotNull ObjectAdapterRegistry registry) {
        return new DefaultTextStyler(registry);
    }
    
    /**
     * Parses a MiniMessage-formatted string into a Component.
     * 
     * @param miniMessageFormat The MiniMessage format string
     * @return The parsed component
     */
    @NotNull Component parse(@NotNull String miniMessageFormat);
    
    /**
     * Styles any object using the registered ObjectRenderer.
     * 
     * @param entity The object to style
     * @return The styled component
     * @throws IllegalArgumentException if no renderer is registered for the type
     */
    @NotNull Component style(@NotNull Object entity);
    
    /**
     * Styles any object with additional context.
     * 
     * @param entity The object to style
     * @param context The styling context
     * @return The styled component
     * @throws IllegalArgumentException if no renderer is registered for the type
     */
    @NotNull Component style(@NotNull Object entity, @Nullable StyleContext context);
    
    /**
     * Gets the underlying adapter registry.
     * 
     * @return The ObjectAdapterRegistry used by this styler
     */
    @NotNull ObjectAdapterRegistry getAdapterRegistry();
}

/**
 * Default implementation of TextStyler.
 */
final class DefaultTextStyler implements TextStyler {
    
    static final DefaultTextStyler INSTANCE = new DefaultTextStyler(ObjectAdapterRegistry.get());
    
    private final ObjectAdapterRegistry registry;
    
    DefaultTextStyler(@NotNull ObjectAdapterRegistry registry) {
        this.registry = registry;
    }
    
    @Override
    public @NotNull Component parse(@NotNull String miniMessageFormat) {
        return MiniMessage.parse(miniMessageFormat);
    }
    
    @Override
    public @NotNull Component style(@NotNull Object entity) {
        return style(entity, null);
    }
    
    @Override
    public @NotNull Component style(@NotNull Object entity, @Nullable StyleContext context) {
        // If it's already a component, return it
        if (entity instanceof Component comp) {
            return comp;
        }
        
        // If it's a string, parse as MiniMessage
        if (entity instanceof String str) {
            return parse(str);
        }
        
        // Otherwise, use the registry
        return registry.render(entity, context);
    }
    
    @Override
    public @NotNull ObjectAdapterRegistry getAdapterRegistry() {
        return registry;
    }
}
