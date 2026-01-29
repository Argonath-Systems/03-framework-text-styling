package com.argonathsystems.framework.text.i18n;

import com.argonathsystems.framework.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Type-safe representation of a translation argument.
 * Replaces raw {@code Object} usage in translation placeholders.
 * 
 * <h2>Supported Argument Types:</h2>
 * <ul>
 *   <li>{@link StringArg} - Simple string values</li>
 *   <li>{@link NumberArg} - Numeric values (for formatting)</li>
 *   <li>{@link ComponentArg} - Nested components</li>
 *   <li>{@link DateArg} - Date/time values (as epoch millis)</li>
 *   <li>{@link PluralArg} - Numeric values with plural form selection</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * TranslatableComponent.of("ui.welcome", 
 *     TranslationArgument.string(playerName),
 *     TranslationArgument.number(level));
 * }</pre>
 */
public sealed interface TranslationArgument permits
        TranslationArgument.StringArg,
        TranslationArgument.NumberArg,
        TranslationArgument.ComponentArg,
        TranslationArgument.DateArg,
        TranslationArgument.PluralArg {

    /**
     * Converts the argument to a string for substitution.
     * 
     * @return The string representation of this argument
     */
    @NotNull String asString();

    /**
     * Creates a string argument.
     * 
     * @param value The string value
     * @return A new StringArg
     */
    static TranslationArgument string(@NotNull String value) {
        return new StringArg(value);
    }

    /**
     * Creates a number argument.
     * 
     * @param value The numeric value
     * @return A new NumberArg
     */
    static TranslationArgument number(long value) {
        return new NumberArg(value);
    }

    /**
     * Creates a number argument from a double.
     * 
     * @param value The numeric value
     * @return A new NumberArg
     */
    static TranslationArgument number(double value) {
        return new NumberArg(value);
    }

    /**
     * Creates a component argument for nested text.
     * 
     * @param component The component to embed
     * @return A new ComponentArg
     */
    static TranslationArgument component(@NotNull Component component) {
        return new ComponentArg(component);
    }

    /**
     * Creates a date argument from epoch milliseconds.
     * 
     * @param epochMillis The epoch time in milliseconds
     * @return A new DateArg
     */
    static TranslationArgument date(long epochMillis) {
        return new DateArg(epochMillis);
    }

    /**
     * Creates a plural argument for count-based pluralization.
     * 
     * @param count The count for plural selection
     * @return A new PluralArg
     */
    static TranslationArgument plural(long count) {
        return new PluralArg(count);
    }

    // =========================================================================
    // Sealed Implementations
    // =========================================================================

    /**
     * A simple string argument.
     */
    record StringArg(@NotNull String value) implements TranslationArgument {
        public StringArg {
            Objects.requireNonNull(value, "value cannot be null");
        }

        @Override
        public @NotNull String asString() {
            return value;
        }
    }

    /**
     * A numeric argument supporting both integer and floating-point values.
     */
    record NumberArg(@NotNull Number value) implements TranslationArgument {
        public NumberArg {
            Objects.requireNonNull(value, "value cannot be null");
        }

        public NumberArg(long value) {
            this((Number) value);
        }

        public NumberArg(double value) {
            this((Number) value);
        }

        @Override
        public @NotNull String asString() {
            // Format nicely - integers without decimals, doubles with precision
            if (value instanceof Long || value instanceof Integer) {
                return String.valueOf(value.longValue());
            }
            double d = value.doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }

        public long asLong() {
            return value.longValue();
        }

        public double asDouble() {
            return value.doubleValue();
        }
    }

    /**
     * A component argument for embedding styled text.
     */
    record ComponentArg(@NotNull Component component) implements TranslationArgument {
        public ComponentArg {
            Objects.requireNonNull(component, "component cannot be null");
        }

        @Override
        public @NotNull String asString() {
            // Fallback to plain text extraction
            return extractPlainText(component);
        }

        private static String extractPlainText(Component comp) {
            StringBuilder sb = new StringBuilder();
            if (comp instanceof com.argonathsystems.framework.text.TextComponent tc) {
                sb.append(tc.content());
            }
            for (Component child : comp.children()) {
                sb.append(extractPlainText(child));
            }
            return sb.toString();
        }
    }

    /**
     * A date/time argument.
     */
    record DateArg(long epochMillis) implements TranslationArgument {
        @Override
        public @NotNull String asString() {
            // Default ISO-8601 format - can be customized by formatters
            return java.time.Instant.ofEpochMilli(epochMillis).toString();
        }

        public java.time.Instant asInstant() {
            return java.time.Instant.ofEpochMilli(epochMillis);
        }
    }

    /**
     * A plural argument for count-based text selection.
     * Used with PluralResolver for language-specific plural forms.
     */
    record PluralArg(long count) implements TranslationArgument {
        @Override
        public @NotNull String asString() {
            return String.valueOf(count);
        }
    }
}
