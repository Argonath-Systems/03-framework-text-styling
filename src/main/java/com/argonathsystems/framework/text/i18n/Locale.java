package com.argonathsystems.framework.text.i18n;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a locale for translations.
 * Uses standard language codes (ISO 639-1) with optional country codes (ISO 3166-1).
 * 
 * Examples: "en", "en_US", "fr", "fr_CA", "de", "es", "pt_BR"
 */
public record Locale(@NotNull String language, @NotNull String country) {
    
    // Common locales
    public static final Locale ENGLISH = new Locale("en", "");
    public static final Locale ENGLISH_US = new Locale("en", "US");
    public static final Locale ENGLISH_UK = new Locale("en", "GB");
    public static final Locale FRENCH = new Locale("fr", "");
    public static final Locale FRENCH_CA = new Locale("fr", "CA");
    public static final Locale GERMAN = new Locale("de", "");
    public static final Locale SPANISH = new Locale("es", "");
    public static final Locale PORTUGUESE_BR = new Locale("pt", "BR");
    public static final Locale JAPANESE = new Locale("ja", "");
    public static final Locale KOREAN = new Locale("ko", "");
    public static final Locale CHINESE_CN = new Locale("zh", "CN");
    public static final Locale RUSSIAN = new Locale("ru", "");
    
    /** Default locale used when no specific locale is set */
    public static final Locale DEFAULT = ENGLISH;
    
    public Locale {
        Objects.requireNonNull(language, "language cannot be null");
        Objects.requireNonNull(country, "country cannot be null");
        if (language.isBlank()) {
            throw new IllegalArgumentException("language cannot be blank");
        }
    }
    
    /**
     * Creates a locale with just a language code.
     * 
     * @param language The language code (e.g., "en", "fr")
     * @return A new Locale
     */
    public static Locale of(@NotNull String language) {
        return new Locale(language.toLowerCase(), "");
    }
    
    /**
     * Creates a locale with language and country codes.
     * 
     * @param language The language code (e.g., "en", "fr")
     * @param country The country code (e.g., "US", "CA")
     * @return A new Locale
     */
    public static Locale of(@NotNull String language, @NotNull String country) {
        return new Locale(language.toLowerCase(), country.toUpperCase());
    }
    
    /**
     * Parses a locale string in the format "language" or "language_country".
     * 
     * @param localeString The locale string to parse
     * @return A new Locale
     */
    public static Locale parse(@NotNull String localeString) {
        Objects.requireNonNull(localeString, "localeString cannot be null");
        int underscoreIndex = localeString.indexOf('_');
        if (underscoreIndex > 0) {
            String language = localeString.substring(0, underscoreIndex);
            String country = localeString.substring(underscoreIndex + 1);
            return new Locale(language.toLowerCase(), country.toUpperCase());
        }
        return new Locale(localeString.toLowerCase(), "");
    }
    
    /**
     * Returns the locale code in standard format.
     * 
     * @return The locale code (e.g., "en_US" or "en")
     */
    public String code() {
        if (country.isEmpty()) {
            return language;
        }
        return language + "_" + country;
    }
    
    /**
     * Returns a parent locale (without country) for fallback.
     * 
     * @return The parent locale, or null if this is already a language-only locale
     */
    public Locale parent() {
        if (country.isEmpty()) {
            return null;
        }
        return new Locale(language, "");
    }
    
    /**
     * Converts to Java Locale.
     * 
     * @return The equivalent java.util.Locale
     */
    public java.util.Locale toJavaLocale() {
        if (country.isEmpty()) {
            return java.util.Locale.of(language);
        }
        return java.util.Locale.of(language, country);
    }
    
    /**
     * Creates from Java Locale.
     * 
     * @param javaLocale The Java locale
     * @return A new Locale
     */
    public static Locale fromJavaLocale(@NotNull java.util.Locale javaLocale) {
        return new Locale(javaLocale.getLanguage(), javaLocale.getCountry());
    }
    
    @Override
    public String toString() {
        return code();
    }
}
