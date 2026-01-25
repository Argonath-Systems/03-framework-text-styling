package com.argonathsystems.framework.text.placeholder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Processes text containing placeholders and replaces them with resolved values.
 * 
 * <h2>Placeholder Formats:</h2>
 * <ul>
 *   <li>{@code {key}} - Simple placeholder</li>
 *   <li>{@code {key:param}} - Placeholder with parameter</li>
 *   <li>{@code {key:param1:param2}} - Placeholder with multiple parameters</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * PlaceholderResolver resolver = context -> switch (context.key()) {
 *     case "player" -> Optional.of(playerName);
 *     case "server" -> Optional.of(serverName);
 *     default -> Optional.empty();
 * };
 * 
 * String result = PlaceholderProcessor.process(
 *     "Welcome to {server}, {player}!", 
 *     resolver
 * );
 * }</pre>
 */
public final class PlaceholderProcessor {
    
    /** Pattern for placeholders: {key} or {key:params} */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");
    
    /** Pattern for % style placeholders: %key% */
    private static final Pattern PERCENT_PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    
    private PlaceholderProcessor() {} // Utility class
    
    /**
     * Processes a string, replacing all {placeholder} tokens with resolved values.
     * Unresolved placeholders are left as-is.
     * 
     * @param input The input string with placeholders
     * @param resolver The resolver to use
     * @return The processed string
     */
    public static String process(@NotNull String input, @NotNull PlaceholderResolver resolver) {
        return process(input, resolver, null, false);
    }
    
    /**
     * Processes a string with a viewer context.
     * 
     * @param input The input string with placeholders
     * @param resolver The resolver to use
     * @param viewerId The viewer's UUID as string (for player-specific placeholders)
     * @return The processed string
     */
    public static String process(@NotNull String input, @NotNull PlaceholderResolver resolver, 
                                  @Nullable String viewerId) {
        return process(input, resolver, viewerId, false);
    }
    
    /**
     * Processes a string, optionally removing unresolved placeholders.
     * 
     * @param input The input string with placeholders
     * @param resolver The resolver to use
     * @param viewerId The viewer's UUID as string
     * @param removeUnresolved If true, unresolved placeholders are removed; otherwise kept
     * @return The processed string
     */
    public static String process(@NotNull String input, @NotNull PlaceholderResolver resolver,
                                  @Nullable String viewerId, boolean removeUnresolved) {
        if (input == null) {
            return "";
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(input);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String placeholderContent = matcher.group(1);
            PlaceholderContext context = PlaceholderContext.parse(placeholderContent, viewerId);
            
            String replacement = resolver.resolve(context)
                .orElse(removeUnresolved ? "" : matcher.group(0));
            
            // Escape replacement string for appendReplacement
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Processes a string using %placeholder% syntax (PAPI-style).
     * 
     * @param input The input string with placeholders
     * @param resolver The resolver to use
     * @return The processed string
     */
    public static String processPercent(@NotNull String input, @NotNull PlaceholderResolver resolver) {
        return processPercent(input, resolver, null);
    }
    
    /**
     * Processes a string using %placeholder% syntax with viewer context.
     * 
     * @param input The input string with placeholders
     * @param resolver The resolver to use
     * @param viewerId The viewer's UUID as string
     * @return The processed string
     */
    public static String processPercent(@NotNull String input, @NotNull PlaceholderResolver resolver,
                                         @Nullable String viewerId) {
        Matcher matcher = PERCENT_PLACEHOLDER_PATTERN.matcher(input);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String placeholderContent = matcher.group(1);
            PlaceholderContext context = PlaceholderContext.parse(placeholderContent, viewerId);
            
            String replacement = resolver.resolve(context)
                .orElse(matcher.group(0));
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Checks if a string contains any placeholders.
     * 
     * @param input The string to check
     * @return true if the string contains {placeholder} syntax
     */
    public static boolean containsPlaceholders(@NotNull String input) {
        return PLACEHOLDER_PATTERN.matcher(input).find();
    }
    
    /**
     * Checks if a string contains any percent-style placeholders.
     * 
     * @param input The string to check
     * @return true if the string contains %placeholder% syntax
     */
    public static boolean containsPercentPlaceholders(@NotNull String input) {
        return PERCENT_PLACEHOLDER_PATTERN.matcher(input).find();
    }
}
