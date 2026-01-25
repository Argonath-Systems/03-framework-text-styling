package com.argonathsystems.framework.text;

import com.argonathsystems.framework.text.event.ClickAction;
import com.argonathsystems.framework.text.event.HoverEvent;
import com.argonathsystems.framework.text.i18n.Locale;
import com.argonathsystems.framework.text.i18n.TranslationKey;
import com.argonathsystems.framework.text.placeholder.PlaceholderProcessor;
import com.argonathsystems.framework.text.placeholder.PlaceholderResolver;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Enhanced MiniMessage-style parser for converting formatted strings to Components.
 * 
 * <h2>Supported Tags:</h2>
 * <ul>
 *   <li><b>Colors:</b> {@code <red>}, {@code <blue>}, {@code <#FF5500>}</li>
 *   <li><b>Decorations:</b> {@code <bold>}, {@code <italic>}, {@code <underlined>}, {@code <strikethrough>}, {@code <obfuscated>}</li>
 *   <li><b>Reset:</b> {@code <reset>} or {@code <!reset>}</li>
 *   <li><b>Gradients:</b> {@code <gradient:red:gold>text</gradient>}</li>
 *   <li><b>Click:</b> {@code <click:run_command:/help>text</click>}</li>
 *   <li><b>Hover:</b> {@code <hover:show_text:Tooltip here>text</hover>}</li>
 *   <li><b>Translation:</b> {@code <lang:ui.button.confirm>}</li>
 *   <li><b>Closing Tags:</b> {@code </red>}, {@code </bold>}, etc.</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * Component message = MiniMessage.parse("<red>Hello</red> <gradient:gold:yellow>World</gradient>!");
 * 
 * // With placeholders
 * Component welcome = MiniMessage.parse("<green>Welcome, {player}!</green>", resolver);
 * }</pre>
 */
public final class MiniMessage {
    
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([^<>]+)>");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([0-9A-Fa-f]{6})$");
    
    private MiniMessage() {} // Utility class
    
    /**
     * Parses a MiniMessage-formatted string into a Component tree.
     * 
     * @param input The input string
     * @return The parsed component
     */
    public static Component parse(@NotNull String input) {
        return parse(input, PlaceholderResolver.empty());
    }
    
    /**
     * Parses a MiniMessage-formatted string with placeholder resolution.
     * 
     * @param input The input string
     * @param resolver The placeholder resolver
     * @return The parsed component
     */
    public static Component parse(@NotNull String input, @NotNull PlaceholderResolver resolver) {
        // First, resolve placeholders
        String processed = PlaceholderProcessor.process(input, resolver);
        return parseInternal(processed);
    }
    
    /**
     * Parses a MiniMessage-formatted string with placeholder resolution and viewer context.
     * 
     * @param input The input string
     * @param resolver The placeholder resolver
     * @param viewerId The viewer's UUID as string
     * @return The parsed component
     */
    public static Component parse(@NotNull String input, @NotNull PlaceholderResolver resolver,
                                   @NotNull String viewerId) {
        String processed = PlaceholderProcessor.process(input, resolver, viewerId);
        return parseInternal(processed);
    }
    
    private static Component parseInternal(@NotNull String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        
        TextComponent root = new TextComponent("");
        Deque<StyleState> styleStack = new ArrayDeque<>();
        styleStack.push(new StyleState(Style.empty(), null, null));
        
        Matcher matcher = TAG_PATTERN.matcher(input);
        int lastEnd = 0;
        
        while (matcher.find()) {
            // Add text before the tag
            String textPart = input.substring(lastEnd, matcher.start());
            if (!textPart.isEmpty()) {
                StyleState currentState = styleStack.peek();
                root.append(createStyledText(textPart, currentState));
            }
            
            String isClosing = matcher.group(1);
            String tagContent = matcher.group(2).toLowerCase();
            
            if (!isClosing.isEmpty()) {
                // Closing tag - pop style if it matches
                if (!styleStack.isEmpty() && styleStack.size() > 1) {
                    String topTag = styleStack.peek().tagName();
                    if (topTag != null && topTag.equals(getBaseTag(tagContent))) {
                        styleStack.pop();
                    }
                }
            } else {
                // Opening tag
                StyleState newState = parseTag(tagContent, styleStack.peek());
                if (newState != null) {
                    styleStack.push(newState);
                } else if (tagContent.equals("reset")) {
                    // Reset clears the stack
                    styleStack.clear();
                    styleStack.push(new StyleState(Style.empty(), null, null));
                }
            }
            
            lastEnd = matcher.end();
        }
        
        // Add remaining text
        if (lastEnd < input.length()) {
            String textPart = input.substring(lastEnd);
            StyleState currentState = styleStack.peek();
            root.append(createStyledText(textPart, currentState));
        }
        
        return root;
    }
    
    private static String getBaseTag(String tagContent) {
        int colonIndex = tagContent.indexOf(':');
        return colonIndex > 0 ? tagContent.substring(0, colonIndex) : tagContent;
    }
    
    private static TextComponent createStyledText(String text, StyleState state) {
        // Handle gradient
        if (state.gradient() != null) {
            return state.gradient().applyAsComponent(text);
        }
        
        TextComponent component = new TextComponent(text);
        component.style(state.style());
        return component;
    }
    
    private static StyleState parseTag(String tagContent, StyleState currentState) {
        String[] parts = tagContent.split(":", -1);
        String tagName = parts[0];
        
        // Decorations
        switch (tagName) {
            case "bold", "b" -> {
                return new StyleState(
                    currentState.style().merge(Style.builder().bold(true).build()),
                    "bold", null);
            }
            case "italic", "i", "em" -> {
                return new StyleState(
                    currentState.style().merge(Style.builder().italic(true).build()),
                    "italic", null);
            }
            case "underlined", "u" -> {
                return new StyleState(
                    currentState.style().merge(Style.builder().underlined(true).build()),
                    "underlined", null);
            }
            case "strikethrough", "st" -> {
                return new StyleState(
                    currentState.style().merge(Style.builder().strikethrough(true).build()),
                    "strikethrough", null);
            }
            case "obfuscated", "obf" -> {
                return new StyleState(
                    currentState.style().merge(Style.builder().obfuscated(true).build()),
                    "obfuscated", null);
            }
        }
        
        // Gradient: <gradient:color1:color2[:color3...]>
        if (tagName.equals("gradient") && parts.length >= 3) {
            List<TextColor> colors = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                TextColor color = parseColor(parts[i]);
                if (color != null) {
                    colors.add(color);
                }
            }
            if (colors.size() >= 2) {
                return new StyleState(currentState.style(), "gradient", Gradient.of(colors));
            }
        }
        
        // Click action: <click:action:value>
        if (tagName.equals("click") && parts.length >= 3) {
            ClickAction action = parseClickAction(parts[1], parts[2]);
            if (action != null) {
                return new StyleState(
                    currentState.style().merge(Style.builder().clickAction(action).build()),
                    "click", null);
            }
        }
        
        // Hover event: <hover:show_text:content>
        if (tagName.equals("hover") && parts.length >= 3) {
            HoverEvent event = parseHoverEvent(parts[1], parts[2]);
            if (event != null) {
                return new StyleState(
                    currentState.style().merge(Style.builder().hoverEvent(event).build()),
                    "hover", null);
            }
        }
        
        // Color (named or hex)
        TextColor color = parseColor(tagName);
        if (color != null) {
            return new StyleState(
                currentState.style().merge(Style.of(color)),
                tagName, null);
        }
        
        return null; // Unknown tag
    }
    
    private static TextColor parseColor(String colorStr) {
        // Check hex
        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(colorStr);
        if (hexMatcher.matches()) {
            return TextColor.fromHex(colorStr);
        }
        if (colorStr.startsWith("#") && colorStr.length() == 7) {
            try {
                return TextColor.fromHex(colorStr);
            } catch (Exception e) {
                return null;
            }
        }
        
        // Named colors
        return switch (colorStr.toLowerCase()) {
            case "black" -> TextColor.BLACK;
            case "dark_blue" -> TextColor.of(0x0000AA);
            case "dark_green" -> TextColor.of(0x00AA00);
            case "dark_aqua" -> TextColor.of(0x00AAAA);
            case "dark_red" -> TextColor.of(0xAA0000);
            case "dark_purple" -> TextColor.of(0xAA00AA);
            case "gold" -> TextColor.GOLD;
            case "gray", "grey" -> TextColor.GRAY;
            case "dark_gray", "dark_grey" -> TextColor.of(0x555555);
            case "blue" -> TextColor.BLUE;
            case "green" -> TextColor.GREEN;
            case "aqua" -> TextColor.of(0x55FFFF);
            case "red" -> TextColor.RED;
            case "light_purple" -> TextColor.of(0xFF55FF);
            case "yellow" -> TextColor.of(0xFFFF55);
            case "white" -> TextColor.WHITE;
            default -> null;
        };
    }
    
    private static ClickAction parseClickAction(String actionType, String value) {
        return switch (actionType.toLowerCase()) {
            case "run_command" -> ClickAction.runCommand(value);
            case "suggest_command" -> ClickAction.suggestCommand(value);
            case "open_url" -> ClickAction.openUrl(value);
            case "copy_to_clipboard" -> ClickAction.copyToClipboard(value);
            default -> null;
        };
    }
    
    private static HoverEvent parseHoverEvent(String actionType, String value) {
        return switch (actionType.toLowerCase()) {
            case "show_text" -> HoverEvent.showText(value);
            case "show_item" -> HoverEvent.showItem(value);
            default -> null;
        };
    }
    
    /**
     * Escapes special characters in a string so they are not parsed as tags.
     * 
     * @param input The input string
     * @return The escaped string
     */
    public static String escape(@NotNull String input) {
        return input.replace("<", "\\<").replace(">", "\\>");
    }
    
    /**
     * Unescapes a previously escaped string.
     * 
     * @param input The escaped string
     * @return The unescaped string
     */
    public static String unescape(@NotNull String input) {
        return input.replace("\\<", "<").replace("\\>", ">");
    }
    
    /**
     * Strips all formatting tags from a string, returning plain text.
     * 
     * @param input The input string with tags
     * @return Plain text without tags
     */
    public static String stripTags(@NotNull String input) {
        return TAG_PATTERN.matcher(input).replaceAll("");
    }
    
    // Internal record for tracking style state during parsing
    private record StyleState(Style style, String tagName, Gradient gradient) {}
}
