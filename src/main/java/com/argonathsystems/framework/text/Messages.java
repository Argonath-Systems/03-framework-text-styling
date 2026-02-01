package com.argonathsystems.framework.text;

import com.argonathsystems.framework.text.serialization.LegacySerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for creating common styled messages.
 * 
 * <p>Provides a fluent API for constructing styled messages without
 * using legacy § color codes directly. This is the recommended way
 * to create messages in the Argonath framework.
 * 
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Error message
 * Component error = Messages.error("Command failed!");
 * 
 * // Success message
 * Component success = Messages.success("Quest completed!");
 * 
 * // Info message
 * Component info = Messages.info("Use /help for more commands.");
 * 
 * // Labeled message
 * Component labeled = Messages.labeled("Player", "Steve", TextColor.GOLD);
 * 
 * // Title with content
 * Component header = Messages.header("NPC List");
 * 
 * // Complex message with builder
 * Component complex = Messages.builder()
 *     .gold().bold().text("=== ").reset()
 *     .gold().text("Quest Log").reset()
 *     .gold().bold().text(" ===")
 *     .build();
 * }</pre>
 * 
 * <h2>Specification Reference</h2>
 * <ul>
 *   <li>SF-ARCHITECTURE-000: Library-first approach</li>
 *   <li>SF-TEXT-001: Text styling framework</li>
 * </ul>
 * 
 * @author Argonath Systems Team
 * @version 1.0.0
 * @since 2026-02-01
 */
public final class Messages {
    
    private Messages() {} // Utility class
    
    // ==================== Quick Message Types ====================
    
    /**
     * Create an error message (red text).
     * 
     * @param message The error message
     * @return Styled error component
     */
    public static Component error(@NotNull String message) {
        return Component.text(message, TextColor.RED);
    }
    
    /**
     * Create a success message (green text).
     * 
     * @param message The success message
     * @return Styled success component
     */
    public static Component success(@NotNull String message) {
        return Component.text(message, TextColor.GREEN);
    }
    
    /**
     * Create an info message (gray text).
     * 
     * @param message The info message
     * @return Styled info component
     */
    public static Component info(@NotNull String message) {
        return Component.text(message, TextColor.GRAY);
    }
    
    /**
     * Create a warning message (yellow text).
     * 
     * @param message The warning message
     * @return Styled warning component
     */
    public static Component warning(@NotNull String message) {
        return Component.text(message, TextColor.YELLOW);
    }
    
    /**
     * Create a highlight message (gold text).
     * 
     * @param message The message to highlight
     * @return Styled highlight component
     */
    public static Component highlight(@NotNull String message) {
        return Component.text(message, TextColor.GOLD);
    }
    
    /**
     * Create a muted message (dark gray text).
     * 
     * @param message The muted message
     * @return Styled muted component
     */
    public static Component muted(@NotNull String message) {
        return Component.text(message, TextColor.DARK_GRAY);
    }
    
    // ==================== Structured Messages ====================
    
    /**
     * Create a header/title message (gold, bold, with decorators).
     * 
     * @param title The header title
     * @return Styled header component
     */
    public static Component header(@NotNull String title) {
        return builder()
            .gold().bold().text("=== ")
            .gold().text(title)
            .gold().bold().text(" ===")
            .build();
    }
    
    /**
     * Create a subheader message (yellow, with single dash decorators).
     * 
     * @param title The subheader title
     * @return Styled subheader component
     */
    public static Component subheader(@NotNull String title) {
        return builder()
            .yellow().text("- ")
            .yellow().text(title)
            .yellow().text(" -")
            .build();
    }
    
    /**
     * Create a labeled value message (e.g., "Name: Steve").
     * 
     * @param label The label (e.g., "Name")
     * @param value The value (e.g., "Steve")
     * @return Styled labeled component
     */
    public static Component labeled(@NotNull String label, @NotNull String value) {
        return labeled(label, value, TextColor.WHITE);
    }
    
    /**
     * Create a labeled value message with custom value color.
     * 
     * @param label The label (gray)
     * @param value The value
     * @param valueColor The color for the value
     * @return Styled labeled component
     */
    public static Component labeled(@NotNull String label, @NotNull String value, @NotNull TextColor valueColor) {
        return builder()
            .gray().text(label + ": ")
            .color(valueColor).text(value)
            .build();
    }
    
    /**
     * Create a bullet point item.
     * 
     * @param text The item text
     * @return Styled bullet item component
     */
    public static Component bullet(@NotNull String text) {
        return bullet(text, TextColor.WHITE);
    }
    
    /**
     * Create a bullet point item with custom color.
     * 
     * @param text The item text
     * @param color The text color
     * @return Styled bullet item component
     */
    public static Component bullet(@NotNull String text, @NotNull TextColor color) {
        return builder()
            .gray().text("• ")
            .color(color).text(text)
            .build();
    }
    
    /**
     * Create a command hint message.
     * Example: "/help <command>"
     * 
     * @param command The command (without leading /)
     * @param description Brief description
     * @return Styled command hint component
     */
    public static Component commandHint(@NotNull String command, @NotNull String description) {
        return builder()
            .yellow().text("/" + command + " ")
            .gray().text("- " + description)
            .build();
    }
    
    /**
     * Create a simple command hint (just the command text).
     * Use this for help text where the command is followed by description on same line.
     * 
     * @param text The command or hint text
     * @return Styled command hint component in yellow
     */
    public static Component hint(@NotNull String text) {
        return builder()
            .yellow().text(text)
            .build();
    }
    
    /**
     * Create a status indicator.
     * 
     * @param label The status label
     * @param isPositive True for green check, false for red X
     * @return Styled status component
     */
    public static Component status(@NotNull String label, boolean isPositive) {
        return builder()
            .gray().text(label + ": ")
            .color(isPositive ? TextColor.GREEN : TextColor.RED)
            .text(isPositive ? "✓ Yes" : "✗ No")
            .build();
    }
    
    /**
     * Create a progress indicator.
     * 
     * @param current Current progress value
     * @param max Maximum progress value
     * @return Styled progress component (e.g., "(3/10)")
     */
    public static Component progress(int current, int max) {
        TextColor color;
        if (current >= max) {
            color = TextColor.GREEN;
        } else if (current > 0) {
            color = TextColor.YELLOW;
        } else {
            color = TextColor.GRAY;
        }
        return Component.text("(" + current + "/" + max + ")", color);
    }
    
    // ==================== Legacy Compatibility ====================
    
    /**
     * Parse a legacy § color code string into a Component.
     * 
     * <p><b>Note:</b> This is provided for backward compatibility.
     * New code should use the builder or factory methods instead.
     * 
     * @param legacyString String containing § color codes
     * @return Parsed component
     */
    public static Component fromLegacy(@NotNull String legacyString) {
        return LegacySerializer.get().deserialize(legacyString);
    }
    
    /**
     * Serialize a Component to legacy § color code format.
     * 
     * <p>Use this when you need to send a Component through APIs
     * that only accept String (like CommandSender.sendMessage).
     * 
     * @param component The component to serialize
     * @return String with § color codes
     */
    public static String toLegacy(@NotNull Component component) {
        return LegacySerializer.get().serialize(component);
    }
    
    /**
     * Parse a MiniMessage-formatted string into a Component.
     * 
     * @param miniMessage String with MiniMessage tags (e.g., "&lt;red&gt;Error&lt;/red&gt;")
     * @return Parsed component
     */
    public static Component parse(@NotNull String miniMessage) {
        return MiniMessage.parse(miniMessage);
    }
    
    /**
     * Convert a Component to legacy format for use with CommandSender.
     * 
     * <p>This is a convenience method for the common pattern:
     * <pre>{@code
     * sender.sendMessage(Messages.toLegacy(Messages.error("Something failed")));
     * }</pre>
     * 
     * <p>Can be simplified to:
     * <pre>{@code
     * sender.sendMessage(Messages.legacy(Messages.error("Something failed")));
     * }</pre>
     * 
     * @param component The component to convert
     * @return Legacy-formatted string
     */
    public static String legacy(@NotNull Component component) {
        return toLegacy(component);
    }
    
    // ==================== Builder ====================
    
    /**
     * Create a new message builder for complex styled messages.
     * 
     * @return New MessageBuilder instance
     */
    public static MessageBuilder builder() {
        return new MessageBuilder();
    }
    
    /**
     * Fluent builder for constructing complex styled messages.
     */
    public static class MessageBuilder {
        private Component root = Component.empty();
        private Style.Builder currentStyle = Style.builder();
        
        private MessageBuilder() {}
        
        // Color methods
        public MessageBuilder color(TextColor color) {
            currentStyle.color(color);
            return this;
        }
        
        public MessageBuilder black() { return color(TextColor.BLACK); }
        public MessageBuilder darkBlue() { return color(TextColor.DARK_BLUE); }
        public MessageBuilder darkGreen() { return color(TextColor.DARK_GREEN); }
        public MessageBuilder darkAqua() { return color(TextColor.DARK_AQUA); }
        public MessageBuilder darkRed() { return color(TextColor.DARK_RED); }
        public MessageBuilder darkPurple() { return color(TextColor.DARK_PURPLE); }
        public MessageBuilder gold() { return color(TextColor.GOLD); }
        public MessageBuilder gray() { return color(TextColor.GRAY); }
        public MessageBuilder darkGray() { return color(TextColor.DARK_GRAY); }
        public MessageBuilder blue() { return color(TextColor.BLUE); }
        public MessageBuilder green() { return color(TextColor.GREEN); }
        public MessageBuilder aqua() { return color(TextColor.AQUA); }
        public MessageBuilder red() { return color(TextColor.RED); }
        public MessageBuilder lightPurple() { return color(TextColor.LIGHT_PURPLE); }
        public MessageBuilder yellow() { return color(TextColor.YELLOW); }
        public MessageBuilder white() { return color(TextColor.WHITE); }
        
        // Decoration methods
        public MessageBuilder bold() {
            currentStyle.bold(true);
            return this;
        }
        
        public MessageBuilder italic() {
            currentStyle.italic(true);
            return this;
        }
        
        public MessageBuilder underlined() {
            currentStyle.underlined(true);
            return this;
        }
        
        public MessageBuilder strikethrough() {
            currentStyle.strikethrough(true);
            return this;
        }
        
        /**
         * Reset all styling for subsequent text.
         */
        public MessageBuilder reset() {
            currentStyle = Style.builder();
            return this;
        }
        
        /**
         * Append text with current style.
         * 
         * @param text The text to append
         * @return This builder
         */
        public MessageBuilder text(String text) {
            Component child = Component.text(text, currentStyle.build());
            root.append(child);
            // Reset decorations but keep color
            TextColor color = currentStyle.build().color();
            currentStyle = Style.builder();
            if (color != null) {
                currentStyle.color(color);
            }
            return this;
        }
        
        /**
         * Append another component.
         * 
         * @param component The component to append
         * @return This builder
         */
        public MessageBuilder append(Component component) {
            root.append(component);
            return this;
        }
        
        /**
         * Append styled text.
         * 
         * @param text The text to append
         * @param color The color to use
         * @return This builder
         */
        public MessageBuilder append(String text, TextColor color) {
            root.append(Component.text(text, Style.of(color)));
            return this;
        }
        
        /**
         * Append a space.
         * 
         * @return This builder
         */
        public MessageBuilder space() {
            return text(" ");
        }
        
        /**
         * Append a newline.
         * 
         * @return This builder
         */
        public MessageBuilder newline() {
            return text("\n");
        }
        
        /**
         * Build the final component.
         * 
         * @return The constructed component
         */
        public Component build() {
            return root;
        }
    }
}
