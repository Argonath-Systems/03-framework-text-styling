package com.argonathsystems.framework.text.format;

import com.argonathsystems.framework.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Functional interface for rendering domain objects as styled text Components.
 * Each ObjectRenderer handles a specific type of object.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Define a renderer for Items
 * ObjectRenderer<Item> itemRenderer = (item, context) -> {
 *     TextColor color = getRarityColor(item.rarity());
 *     return Component.text(item.displayName(), color);
 * };
 * 
 * // Register in the registry
 * ObjectAdapterRegistry.get().register(Item.class, itemRenderer);
 * }</pre>
 * 
 * @param <T> The type of object this renderer handles
 */
@FunctionalInterface
public interface ObjectRenderer<T> {
    
    /**
     * Renders an object as a styled text component.
     * 
     * @param target The object to render
     * @param context Optional styling context (can be null)
     * @return The rendered component
     */
    @NotNull Component render(@NotNull T target, @Nullable StyleContext context);
    
    /**
     * Renders an object without additional context.
     * 
     * @param target The object to render
     * @return The rendered component
     */
    default @NotNull Component render(@NotNull T target) {
        return render(target, null);
    }
}
