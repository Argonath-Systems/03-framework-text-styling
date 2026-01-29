package com.argonathsystems.framework.text.i18n;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Resolves plural forms based on language-specific rules.
 * Different languages have different plural rules - English has 2 forms (singular/plural),
 * Russian has 3, Arabic has 6, etc.
 * 
 * <h2>Plural Categories:</h2>
 * <ul>
 *   <li>{@code zero} - Used for zero quantity in some languages</li>
 *   <li>{@code one} - Singular form</li>
 *   <li>{@code two} - Dual form (Arabic, Welsh, etc.)</li>
 *   <li>{@code few} - Paucal form (Russian, Polish, etc.)</li>
 *   <li>{@code many} - Many form (Russian, Arabic, etc.)</li>
 *   <li>{@code other} - General plural, fallback</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * PluralResolver resolver = PluralResolver.get();
 * 
 * // Get plural category for a number in a locale
 * PluralCategory category = resolver.resolve(Locale.ENGLISH, 5);
 * // Returns PluralCategory.OTHER (English plural)
 * 
 * PluralCategory singular = resolver.resolve(Locale.ENGLISH, 1);
 * // Returns PluralCategory.ONE (English singular)
 * 
 * // Select the right translation form
 * String form = switch (category) {
 *     case ONE -> "apple";
 *     case OTHER -> "apples";
 *     default -> "apples";
 * };
 * }</pre>
 */
public final class PluralResolver {
    
    private static final PluralResolver INSTANCE = new PluralResolver();
    
    private final Map<String, Function<Number, PluralCategory>> rules;
    
    public PluralResolver() {
        this.rules = new ConcurrentHashMap<>();
        registerDefaultRules();
    }
    
    /**
     * Returns the global resolver instance.
     * 
     * @return The singleton resolver
     */
    public static PluralResolver get() {
        return INSTANCE;
    }
    
    /**
     * Plural categories as defined by Unicode CLDR.
     */
    public enum PluralCategory {
        /** Used for zero in languages that have a specific zero form */
        ZERO,
        /** Singular - typically for 1 in most languages */
        ONE,
        /** Dual form - for 2 in languages like Arabic, Welsh */
        TWO,
        /** Few - for small numbers in Slavic languages, etc. */
        FEW,
        /** Many - for larger numbers in some languages */
        MANY,
        /** General plural form, used as fallback */
        OTHER
    }
    
    /**
     * Resolves the plural category for a given number in a locale.
     * 
     * @param locale The locale
     * @param number The number to resolve
     * @return The plural category
     */
    public @NotNull PluralCategory resolve(@NotNull Locale locale, @NotNull Number number) {
        Objects.requireNonNull(locale, "locale cannot be null");
        Objects.requireNonNull(number, "number cannot be null");
        
        // Try exact locale
        Function<Number, PluralCategory> rule = rules.get(locale.language());
        if (rule != null) {
            return rule.apply(number);
        }
        
        // Fall back to English rules
        return defaultRule(number);
    }
    
    /**
     * Registers a custom plural rule for a language.
     * 
     * @param languageCode The ISO 639-1 language code
     * @param rule Function that determines plural category from a number
     */
    public void registerRule(@NotNull String languageCode, 
                             @NotNull Function<Number, PluralCategory> rule) {
        rules.put(languageCode.toLowerCase(), rule);
    }
    
    /**
     * Checks if a custom rule is registered for a language.
     * 
     * @param languageCode The language code
     * @return true if a rule exists
     */
    public boolean hasRule(@NotNull String languageCode) {
        return rules.containsKey(languageCode.toLowerCase());
    }
    
    private void registerDefaultRules() {
        // English, German, Spanish, Italian, Portuguese, etc.
        // 1 = one, everything else = other
        rules.put("en", this::westGermanic);
        rules.put("de", this::westGermanic);
        rules.put("es", this::westGermanic);
        rules.put("it", this::westGermanic);
        rules.put("pt", this::westGermanic);
        rules.put("nl", this::westGermanic);
        rules.put("sv", this::westGermanic);
        rules.put("no", this::westGermanic);
        rules.put("da", this::westGermanic);
        
        // French: 0 and 1 are singular
        rules.put("fr", this::french);
        
        // Russian, Ukrainian, Belarusian
        rules.put("ru", this::slavicEast);
        rules.put("uk", this::slavicEast);
        rules.put("be", this::slavicEast);
        
        // Polish, Czech, Slovak
        rules.put("pl", this::slavicWest);
        rules.put("cs", this::slavicWest);
        rules.put("sk", this::slavicWest);
        
        // Arabic - complex system
        rules.put("ar", this::arabic);
        
        // Japanese, Korean, Chinese, Vietnamese - no grammatical number
        // All numbers use the same form
        rules.put("ja", n -> PluralCategory.OTHER);
        rules.put("ko", n -> PluralCategory.OTHER);
        rules.put("zh", n -> PluralCategory.OTHER);
        rules.put("vi", n -> PluralCategory.OTHER);
    }
    
    private PluralCategory defaultRule(Number number) {
        return westGermanic(number);
    }
    
    private PluralCategory westGermanic(Number number) {
        // 1 = one, everything else = other
        double n = number.doubleValue();
        int i = number.intValue();
        
        // Check if it's an integer 1
        if (n == Math.floor(n) && i == 1) {
            return PluralCategory.ONE;
        }
        return PluralCategory.OTHER;
    }
    
    private PluralCategory french(Number number) {
        // 0 and 1 are singular in French
        double n = number.doubleValue();
        int i = number.intValue();
        
        if (n == Math.floor(n) && (i == 0 || i == 1)) {
            return PluralCategory.ONE;
        }
        return PluralCategory.OTHER;
    }
    
    private PluralCategory slavicEast(Number number) {
        // Russian/Ukrainian/Belarusian rules:
        // 1, 21, 31, ... = one
        // 2-4, 22-24, 32-34, ... = few  
        // 5-20, 25-30, ... = many
        int n = Math.abs(number.intValue());
        int mod10 = n % 10;
        int mod100 = n % 100;
        
        if (mod10 == 1 && mod100 != 11) {
            return PluralCategory.ONE;
        }
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) {
            return PluralCategory.FEW;
        }
        if (mod10 == 0 || (mod10 >= 5 && mod10 <= 9) || (mod100 >= 11 && mod100 <= 14)) {
            return PluralCategory.MANY;
        }
        return PluralCategory.OTHER;
    }
    
    private PluralCategory slavicWest(Number number) {
        // Polish/Czech/Slovak rules (simplified):
        // 1 = one
        // 2-4 = few
        // 5+ and 0 = many
        int n = Math.abs(number.intValue());
        int mod10 = n % 10;
        int mod100 = n % 100;
        
        if (n == 1) {
            return PluralCategory.ONE;
        }
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) {
            return PluralCategory.FEW;
        }
        return PluralCategory.MANY;
    }
    
    private PluralCategory arabic(Number number) {
        // Arabic has 6 plural forms
        int n = Math.abs(number.intValue());
        int mod100 = n % 100;
        
        if (n == 0) {
            return PluralCategory.ZERO;
        }
        if (n == 1) {
            return PluralCategory.ONE;
        }
        if (n == 2) {
            return PluralCategory.TWO;
        }
        if (mod100 >= 3 && mod100 <= 10) {
            return PluralCategory.FEW;
        }
        if (mod100 >= 11 && mod100 <= 99) {
            return PluralCategory.MANY;
        }
        return PluralCategory.OTHER;
    }
    
    /**
     * Helper record for plural form selections.
     * Maps each plural category to a translated string.
     * 
     * @param zero Form for zero (may be null if not applicable)
     * @param one Singular form
     * @param two Dual form (may be null if not applicable)
     * @param few Few form (may be null if not applicable)
     * @param many Many form (may be null if not applicable)
     * @param other General plural form (fallback)
     */
    public record PluralForms(
            String zero,
            String one,
            String two,
            String few,
            String many,
            String other
    ) {
        
        /**
         * Creates plural forms for simple languages (singular/plural only).
         * 
         * @param singular The singular form
         * @param plural The plural form
         * @return A PluralForms instance
         */
        public static PluralForms simple(String singular, String plural) {
            return new PluralForms(plural, singular, plural, plural, plural, plural);
        }
        
        /**
         * Selects the appropriate form for a category.
         * Falls back to 'other' if the specific form is null.
         * 
         * @param category The plural category
         * @return The selected form
         */
        public @NotNull String select(@NotNull PluralCategory category) {
            return switch (category) {
                case ZERO -> zero != null ? zero : other;
                case ONE -> one != null ? one : other;
                case TWO -> two != null ? two : other;
                case FEW -> few != null ? few : other;
                case MANY -> many != null ? many : other;
                case OTHER -> other;
            };
        }
    }
}
