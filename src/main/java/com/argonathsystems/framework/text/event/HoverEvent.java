package com.argonathsystems.framework.text.event;

import com.argonathsystems.framework.text.Component;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents content to show when text is hovered.
 * Supports text tooltips, item previews, and entity information.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Simple text tooltip
 * HoverEvent tooltip = HoverEvent.showText(Component.text("Click to accept quest"));
 * 
 * // Item preview (ID-based for platform agnostic)
 * HoverEvent itemPreview = HoverEvent.showItem("iron_sword", 1, "nbt-data-here");
 * 
 * // Entity info
 * HoverEvent entityInfo = HoverEvent.showEntity("zombie", entityUuid, "Boss Zombie");
 * }</pre>
 */
public sealed interface HoverEvent {
    
    /**
     * The type of hover event.
     */
    Action action();
    
    /**
     * The type of hover action.
     */
    enum Action {
        /** Shows a text component as tooltip */
        SHOW_TEXT,
        /** Shows an item preview */
        SHOW_ITEM,
        /** Shows entity information */
        SHOW_ENTITY
    }
    
    /**
     * Creates a hover event that shows text.
     * 
     * @param text The text component to show
     * @return A new HoverEvent
     */
    static HoverEvent showText(@NotNull Component text) {
        return new ShowText(text);
    }
    
    /**
     * Creates a hover event that shows text from a string.
     * 
     * @param text The text to show
     * @return A new HoverEvent
     */
    static HoverEvent showText(@NotNull String text) {
        return new ShowText(Component.text(text));
    }
    
    /**
     * Creates a hover event that shows an item preview.
     * 
     * @param itemId The item identifier
     * @param count The stack count
     * @param nbtData Optional NBT/component data (can be null)
     * @return A new HoverEvent
     */
    static HoverEvent showItem(@NotNull String itemId, int count, String nbtData) {
        return new ShowItem(itemId, count, nbtData);
    }
    
    /**
     * Creates a hover event that shows an item preview (single item, no NBT).
     * 
     * @param itemId The item identifier
     * @return A new HoverEvent
     */
    static HoverEvent showItem(@NotNull String itemId) {
        return new ShowItem(itemId, 1, null);
    }
    
    /**
     * Creates a hover event that shows entity information.
     * 
     * @param entityType The entity type identifier
     * @param entityId The entity's UUID as string
     * @param displayName Optional display name (can be null)
     * @return A new HoverEvent
     */
    static HoverEvent showEntity(@NotNull String entityType, @NotNull String entityId, String displayName) {
        return new ShowEntity(entityType, entityId, displayName);
    }
    
    // ==================== Implementation Records ====================
    
    /**
     * Hover event that shows a text tooltip.
     */
    record ShowText(@NotNull Component text) implements HoverEvent {
        public ShowText {
            Objects.requireNonNull(text, "text cannot be null");
        }
        
        @Override
        public Action action() {
            return Action.SHOW_TEXT;
        }
    }
    
    /**
     * Hover event that shows an item preview.
     */
    record ShowItem(@NotNull String itemId, int count, String nbtData) implements HoverEvent {
        public ShowItem {
            Objects.requireNonNull(itemId, "itemId cannot be null");
            if (count < 1) {
                throw new IllegalArgumentException("count must be at least 1");
            }
        }
        
        @Override
        public Action action() {
            return Action.SHOW_ITEM;
        }
    }
    
    /**
     * Hover event that shows entity information.
     */
    record ShowEntity(@NotNull String entityType, @NotNull String entityId, String displayName) implements HoverEvent {
        public ShowEntity {
            Objects.requireNonNull(entityType, "entityType cannot be null");
            Objects.requireNonNull(entityId, "entityId cannot be null");
        }
        
        @Override
        public Action action() {
            return Action.SHOW_ENTITY;
        }
    }
}
