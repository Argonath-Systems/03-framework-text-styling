package com.argonathsystems.framework.text.event;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an action to perform when text is clicked.
 * Supports various action types like running commands, opening URLs, etc.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Run a command
 * ClickAction confirm = ClickAction.runCommand("/quest accept tutorial");
 * 
 * // Suggest a command (fill chat input)
 * ClickAction suggest = ClickAction.suggestCommand("/msg ");
 * 
 * // Open a URL
 * ClickAction website = ClickAction.openUrl("https://example.com");
 * 
 * // Copy to clipboard
 * ClickAction copy = ClickAction.copyToClipboard("some-code");
 * }</pre>
 */
public record ClickAction(@NotNull Action action, @NotNull String value) {
    
    public ClickAction {
        Objects.requireNonNull(action, "action cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
    }
    
    /**
     * The type of click action.
     */
    public enum Action {
        /** Executes a command as if the player typed it */
        RUN_COMMAND,
        /** Fills the chat input with the command (without executing) */
        SUGGEST_COMMAND,
        /** Opens a URL in the player's browser */
        OPEN_URL,
        /** Copies the value to the player's clipboard */
        COPY_TO_CLIPBOARD,
        /** Changes the page in a book (value is page number) */
        CHANGE_PAGE
    }
    
    /**
     * Creates a click action that runs a command.
     * 
     * @param command The command to run (include the leading /)
     * @return A new ClickAction
     */
    public static ClickAction runCommand(@NotNull String command) {
        return new ClickAction(Action.RUN_COMMAND, command);
    }
    
    /**
     * Creates a click action that suggests a command (fills chat input).
     * 
     * @param command The command to suggest
     * @return A new ClickAction
     */
    public static ClickAction suggestCommand(@NotNull String command) {
        return new ClickAction(Action.SUGGEST_COMMAND, command);
    }
    
    /**
     * Creates a click action that opens a URL.
     * 
     * @param url The URL to open
     * @return A new ClickAction
     */
    public static ClickAction openUrl(@NotNull String url) {
        return new ClickAction(Action.OPEN_URL, url);
    }
    
    /**
     * Creates a click action that copies text to clipboard.
     * 
     * @param text The text to copy
     * @return A new ClickAction
     */
    public static ClickAction copyToClipboard(@NotNull String text) {
        return new ClickAction(Action.COPY_TO_CLIPBOARD, text);
    }
    
    /**
     * Creates a click action that changes the page in a book.
     * 
     * @param page The page number (1-indexed)
     * @return A new ClickAction
     */
    public static ClickAction changePage(int page) {
        return new ClickAction(Action.CHANGE_PAGE, String.valueOf(page));
    }
    
    @Override
    public String toString() {
        return "ClickAction{" + action + "=" + value + "}";
    }
}
