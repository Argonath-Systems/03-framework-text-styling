package com.argonathsystems.framework.text.i18n;

import com.argonathsystems.framework.text.i18n.PluralResolver.PluralCategory;
import com.argonathsystems.framework.text.i18n.PluralResolver.PluralForms;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats messages using ICU-like message syntax for complex i18n scenarios.
 * Supports plurals, select (gender), number formatting, and date formatting.
 * 
 * <h2>Supported Syntax:</h2>
 * 
 * <h3>Simple Argument:</h3>
 * <pre>{@code
 * "Hello, {name}!"
 * }</pre>
 * 
 * <h3>Plural Selection:</h3>
 * <pre>{@code
 * "You have {count, plural, one{# item} other{# items}} in your cart."
 * }</pre>
 * 
 * <h3>Select (Gender/Choice):</h3>
 * <pre>{@code
 * "{gender, select, male{He} female{She} other{They}} liked your post."
 * }</pre>
 * 
 * <h3>Number Formatting:</h3>
 * <pre>{@code
 * "Total: {price, number, currency}"
 * "Score: {score, number, percent}"
 * }</pre>
 * 
 * <h3>Date Formatting:</h3>
 * <pre>{@code
 * "Joined on {date, date, medium}"
 * "Event at {time, time, short}"
 * }</pre>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * IcuFormatter formatter = new IcuFormatter(Locale.ENGLISH);
 * 
 * Map<String, Object> args = Map.of(
 *     "name", "Alice",
 *     "count", 5
 * );
 * 
 * String result = formatter.format(
 *     "Hello {name}, you have {count, plural, one{# message} other{# messages}}.",
 *     args
 * );
 * // Result: "Hello Alice, you have 5 messages."
 * }</pre>
 */
public final class IcuFormatter {
    
    // Pattern to match {argName} or {argName, type, style}
    private static final Pattern ARGUMENT_PATTERN = 
            Pattern.compile("\\{\\s*([^},\\s]+)\\s*(?:,\\s*([^},]+)\\s*(?:,\\s*(.+?))?)?\\}");
    
    // Pattern to match plural/select clauses like "one{text} other{text}"
    private static final Pattern PLURAL_CLAUSE_PATTERN = 
            Pattern.compile("(\\w+)\\{([^}]*)\\}");
    
    private final Locale locale;
    private final PluralResolver pluralResolver;
    
    /**
     * Creates a formatter for the given locale.
     * 
     * @param locale The locale for formatting
     */
    public IcuFormatter(@NotNull Locale locale) {
        this.locale = Objects.requireNonNull(locale, "locale cannot be null");
        this.pluralResolver = PluralResolver.get();
    }
    
    /**
     * Creates a formatter for the default locale.
     * 
     * @return A new formatter
     */
    public static IcuFormatter create() {
        return new IcuFormatter(Locale.DEFAULT);
    }
    
    /**
     * Creates a formatter for the given locale.
     * 
     * @param locale The locale
     * @return A new formatter
     */
    public static IcuFormatter forLocale(@NotNull Locale locale) {
        return new IcuFormatter(locale);
    }
    
    /**
     * Formats a message with the given arguments.
     * 
     * @param pattern The ICU message pattern
     * @param arguments Map of argument name to value
     * @return The formatted message
     */
    public @NotNull String format(@NotNull String pattern, @NotNull Map<String, Object> arguments) {
        Objects.requireNonNull(pattern, "pattern cannot be null");
        Objects.requireNonNull(arguments, "arguments cannot be null");
        
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        
        Matcher matcher = ARGUMENT_PATTERN.matcher(pattern);
        while (matcher.find()) {
            // Append text before this argument
            result.append(pattern, lastEnd, matcher.start());
            
            String argName = matcher.group(1);
            String type = matcher.group(2);
            String style = matcher.group(3);
            
            Object value = arguments.get(argName);
            String replacement = formatArgument(argName, value, type, style, arguments);
            result.append(replacement);
            
            lastEnd = matcher.end();
        }
        
        // Append remaining text
        result.append(pattern, lastEnd, pattern.length());
        
        return result.toString();
    }
    
    /**
     * Convenience method to format with varargs pairs of key, value.
     * 
     * @param pattern The ICU message pattern
     * @param keyValuePairs Alternating keys and values
     * @return The formatted message
     */
    public @NotNull String format(@NotNull String pattern, @NotNull Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("keyValuePairs must have even number of elements");
        }
        
        Map<String, Object> arguments = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            String key = String.valueOf(keyValuePairs[i]);
            Object value = keyValuePairs[i + 1];
            arguments.put(key, value);
        }
        
        return format(pattern, arguments);
    }
    
    private String formatArgument(@NotNull String argName, @Nullable Object value, 
                                  @Nullable String type, @Nullable String style,
                                  @NotNull Map<String, Object> allArgs) {
        if (value == null) {
            return "{" + argName + "}"; // Keep placeholder if value missing
        }
        
        if (type == null || type.isBlank()) {
            // Simple substitution
            return String.valueOf(value);
        }
        
        type = type.trim().toLowerCase();
        
        return switch (type) {
            case "plural" -> formatPlural(value, style);
            case "select" -> formatSelect(value, style);
            case "number" -> formatNumber(value, style);
            case "date" -> formatDate(value, style);
            case "time" -> formatTime(value, style);
            default -> String.valueOf(value);
        };
    }
    
    private String formatPlural(@NotNull Object value, @Nullable String style) {
        if (!(value instanceof Number number)) {
            return String.valueOf(value);
        }
        
        if (style == null || style.isBlank()) {
            return String.valueOf(value);
        }
        
        // Parse plural clauses: one{text} other{texts}
        Map<String, String> clauses = parseClauses(style);
        PluralCategory category = pluralResolver.resolve(locale, number);
        
        String categoryName = category.name().toLowerCase();
        String template = clauses.getOrDefault(categoryName, clauses.get("other"));
        
        if (template == null) {
            return String.valueOf(value);
        }
        
        // Replace # with the number
        return template.replace("#", formatNumberSimple(number));
    }
    
    private String formatSelect(@NotNull Object value, @Nullable String style) {
        String key = String.valueOf(value).toLowerCase();
        
        if (style == null || style.isBlank()) {
            return String.valueOf(value);
        }
        
        Map<String, String> clauses = parseClauses(style);
        return clauses.getOrDefault(key, clauses.getOrDefault("other", String.valueOf(value)));
    }
    
    private String formatNumber(@NotNull Object value, @Nullable String style) {
        if (!(value instanceof Number number)) {
            return String.valueOf(value);
        }
        
        if (style == null || style.isBlank()) {
            return formatNumberSimple(number);
        }
        
        style = style.trim().toLowerCase();
        
        return switch (style) {
            case "integer" -> String.valueOf(number.longValue());
            case "percent" -> formatPercent(number);
            case "currency" -> formatCurrency(number);
            case "decimal" -> formatDecimal(number);
            default -> {
                // Assume custom pattern
                try {
                    DecimalFormat df = new DecimalFormat(style, getDecimalSymbols());
                    yield df.format(number);
                } catch (IllegalArgumentException e) {
                    yield formatNumberSimple(number);
                }
            }
        };
    }
    
    private String formatDate(@NotNull Object value, @Nullable String style) {
        if (!(value instanceof TemporalAccessor temporal)) {
            return String.valueOf(value);
        }
        
        DateTimeFormatter formatter = getDateFormatter(style, false);
        return formatter.format(temporal);
    }
    
    private String formatTime(@NotNull Object value, @Nullable String style) {
        if (!(value instanceof TemporalAccessor temporal)) {
            return String.valueOf(value);
        }
        
        DateTimeFormatter formatter = getDateFormatter(style, true);
        return formatter.format(temporal);
    }
    
    private Map<String, String> parseClauses(String style) {
        Map<String, String> clauses = new HashMap<>();
        
        Matcher matcher = PLURAL_CLAUSE_PATTERN.matcher(style);
        while (matcher.find()) {
            String key = matcher.group(1).toLowerCase();
            String text = matcher.group(2);
            clauses.put(key, text);
        }
        
        return clauses;
    }
    
    private String formatNumberSimple(Number number) {
        if (number instanceof Long || number instanceof Integer || number instanceof Short || number instanceof Byte) {
            return String.valueOf(number.longValue());
        }
        if (number.doubleValue() == Math.floor(number.doubleValue())) {
            return String.valueOf(number.longValue());
        }
        return String.format("%.2f", number.doubleValue());
    }
    
    private String formatPercent(Number number) {
        double value = number.doubleValue();
        return String.format("%.0f%%", value * 100);
    }
    
    private String formatCurrency(Number number) {
        // Simple currency format - in production would use locale-specific symbols
        return String.format("$%.2f", number.doubleValue());
    }
    
    private String formatDecimal(Number number) {
        DecimalFormat df = new DecimalFormat("#,##0.##", getDecimalSymbols());
        return df.format(number);
    }
    
    private DecimalFormatSymbols getDecimalSymbols() {
        // Get locale-appropriate decimal symbols
        java.util.Locale javaLocale = java.util.Locale.forLanguageTag(locale.language());
        return DecimalFormatSymbols.getInstance(javaLocale);
    }
    
    private DateTimeFormatter getDateFormatter(@Nullable String style, boolean timeOnly) {
        if (style == null || style.isBlank()) {
            style = "medium";
        }
        
        style = style.trim().toLowerCase();
        
        return switch (style) {
            case "short" -> timeOnly ? DateTimeFormatter.ofPattern("HH:mm") 
                                      : DateTimeFormatter.ofPattern("MM/dd/yy");
            case "medium" -> timeOnly ? DateTimeFormatter.ofPattern("HH:mm:ss")
                                       : DateTimeFormatter.ofPattern("MMM d, yyyy");
            case "long" -> timeOnly ? DateTimeFormatter.ofPattern("HH:mm:ss z")
                                     : DateTimeFormatter.ofPattern("MMMM d, yyyy");
            case "full" -> timeOnly ? DateTimeFormatter.ofPattern("HH:mm:ss zzzz")
                                     : DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            default -> {
                // Assume custom pattern
                try {
                    yield DateTimeFormatter.ofPattern(style);
                } catch (IllegalArgumentException e) {
                    yield timeOnly ? DateTimeFormatter.ofPattern("HH:mm:ss")
                                   : DateTimeFormatter.ofPattern("MMM d, yyyy");
                }
            }
        };
    }
    
    /**
     * Builder for creating formatted messages fluently.
     * 
     * @return A new MessageBuilder
     */
    public MessageBuilder message() {
        return new MessageBuilder(this);
    }
    
    /**
     * Fluent builder for ICU messages.
     */
    public static final class MessageBuilder {
        private final IcuFormatter formatter;
        private String pattern;
        private final Map<String, Object> arguments = new HashMap<>();
        
        private MessageBuilder(IcuFormatter formatter) {
            this.formatter = formatter;
        }
        
        /**
         * Sets the message pattern.
         * 
         * @param pattern The ICU message pattern
         * @return this builder
         */
        public MessageBuilder pattern(@NotNull String pattern) {
            this.pattern = pattern;
            return this;
        }
        
        /**
         * Adds an argument.
         * 
         * @param name The argument name
         * @param value The argument value
         * @return this builder
         */
        public MessageBuilder arg(@NotNull String name, @Nullable Object value) {
            arguments.put(name, value);
            return this;
        }
        
        /**
         * Adds multiple arguments from a map.
         * 
         * @param args Map of arguments
         * @return this builder
         */
        public MessageBuilder args(@NotNull Map<String, Object> args) {
            arguments.putAll(args);
            return this;
        }
        
        /**
         * Formats and returns the message.
         * 
         * @return The formatted message
         */
        public @NotNull String format() {
            if (pattern == null) {
                throw new IllegalStateException("Pattern must be set before formatting");
            }
            return formatter.format(pattern, arguments);
        }
        
        @Override
        public String toString() {
            return format();
        }
    }
}
