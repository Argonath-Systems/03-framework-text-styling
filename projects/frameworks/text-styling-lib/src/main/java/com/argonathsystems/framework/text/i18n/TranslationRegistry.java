package com.argonathsystems.framework.text.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registry for translation strings organized by namespace and locale.
 * Supports loading translations from properties files and provides
 * fallback behavior (locale with country → language only → default locale).
 * 
 * <p>Thread-safe for concurrent access.</p>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * TranslationRegistry registry = TranslationRegistry.get();
 * 
 * // Register translations programmatically
 * registry.register("core", Locale.ENGLISH, "ui.button.confirm", "Confirm");
 * registry.register("core", Locale.FRENCH, "ui.button.confirm", "Confirmer");
 * 
 * // Load from properties file
 * registry.loadFromProperties("my-mod", Locale.ENGLISH, 
 *     getClass().getResourceAsStream("/lang/en.properties"));
 * 
 * // Retrieve translations
 * String text = registry.translate(TranslationKey.of("ui.button.confirm"), Locale.FRENCH)
 *     .orElse("Confirm");
 * }</pre>
 */
public class TranslationRegistry {
    
    private static final TranslationRegistry INSTANCE = new TranslationRegistry();
    
    // Map structure: namespace → locale → key → value
    private final Map<String, Map<Locale, Map<String, String>>> translations;
    
    public TranslationRegistry() {
        this.translations = new ConcurrentHashMap<>();
    }
    
    /**
     * Returns the global translation registry instance.
     * 
     * @return The singleton registry
     */
    public static TranslationRegistry get() {
        return INSTANCE;
    }
    
    /**
     * Registers a single translation.
     * 
     * @param namespace The mod/plugin namespace
     * @param locale The locale
     * @param key The translation key
     * @param value The translated string
     */
    public void register(@NotNull String namespace, @NotNull Locale locale, 
                         @NotNull String key, @NotNull String value) {
        translations
            .computeIfAbsent(namespace, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(locale, k -> new ConcurrentHashMap<>())
            .put(key, value);
    }
    
    /**
     * Registers a batch of translations.
     * 
     * @param namespace The mod/plugin namespace
     * @param locale The locale
     * @param entries Map of key → value pairs
     */
    public void registerAll(@NotNull String namespace, @NotNull Locale locale,
                            @NotNull Map<String, String> entries) {
        Map<String, String> localeMap = translations
            .computeIfAbsent(namespace, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(locale, k -> new ConcurrentHashMap<>());
        localeMap.putAll(entries);
    }
    
    /**
     * Loads translations from a properties file.
     * 
     * @param namespace The mod/plugin namespace
     * @param locale The locale these translations belong to
     * @param inputStream The input stream to read from
     * @throws IOException If reading fails
     */
    public void loadFromProperties(@NotNull String namespace, @NotNull Locale locale,
                                   @NotNull InputStream inputStream) throws IOException {
        Properties props = new Properties();
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        
        Map<String, String> entries = new HashMap<>();
        for (String key : props.stringPropertyNames()) {
            entries.put(key, props.getProperty(key));
        }
        registerAll(namespace, locale, entries);
    }
    
    /**
     * Translates a key for the given locale with fallback behavior.
     * Fallback order: specific locale → language-only locale → default locale
     * 
     * @param key The translation key
     * @param locale The desired locale
     * @return The translated string, or empty if not found
     */
    public Optional<String> translate(@NotNull TranslationKey key, @NotNull Locale locale) {
        Map<Locale, Map<String, String>> namespaceMap = translations.get(key.namespace());
        if (namespaceMap == null) {
            return Optional.empty();
        }
        
        // Try exact locale
        String result = translateFromLocale(namespaceMap, locale, key.key());
        if (result != null) {
            return Optional.of(result);
        }
        
        // Try parent locale (language only)
        Locale parent = locale.parent();
        if (parent != null) {
            result = translateFromLocale(namespaceMap, parent, key.key());
            if (result != null) {
                return Optional.of(result);
            }
        }
        
        // Try default locale
        if (!locale.equals(Locale.DEFAULT)) {
            result = translateFromLocale(namespaceMap, Locale.DEFAULT, key.key());
            if (result != null) {
                return Optional.of(result);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Translates a key with placeholder substitution.
     * Placeholders use the format {0}, {1}, etc.
     * 
     * @param key The translation key
     * @param locale The desired locale
     * @param args The arguments to substitute
     * @return The translated and formatted string, or empty if not found
     */
    public Optional<String> translate(@NotNull TranslationKey key, @NotNull Locale locale, 
                                      Object... args) {
        return translate(key, locale).map(template -> {
            String result = template;
            for (int i = 0; i < args.length; i++) {
                result = result.replace("{" + i + "}", String.valueOf(args[i]));
            }
            return result;
        });
    }
    
    /**
     * Translates a key, returning the key itself if not found.
     * 
     * @param key The translation key
     * @param locale The desired locale
     * @return The translated string, or the key if not found
     */
    public String translateOrKey(@NotNull TranslationKey key, @NotNull Locale locale) {
        return translate(key, locale).orElse(key.key());
    }
    
    /**
     * Translates a key with placeholders, returning the key if not found.
     * 
     * @param key The translation key
     * @param locale The desired locale
     * @param args The arguments to substitute
     * @return The translated string, or the key if not found
     */
    public String translateOrKey(@NotNull TranslationKey key, @NotNull Locale locale,
                                 Object... args) {
        return translate(key, locale, args).orElse(key.key());
    }
    
    /**
     * Checks if a translation exists for the given key and locale.
     * 
     * @param key The translation key
     * @param locale The locale to check
     * @return true if a translation exists (including fallbacks)
     */
    public boolean hasTranslation(@NotNull TranslationKey key, @NotNull Locale locale) {
        return translate(key, locale).isPresent();
    }
    
    /**
     * Returns all registered translations for a namespace and locale.
     * 
     * @param namespace The namespace
     * @param locale The locale
     * @return Unmodifiable map of translations
     */
    public Map<String, String> getTranslations(@NotNull String namespace, @NotNull Locale locale) {
        Map<Locale, Map<String, String>> namespaceMap = translations.get(namespace);
        if (namespaceMap == null) {
            return Collections.emptyMap();
        }
        Map<String, String> localeMap = namespaceMap.get(locale);
        if (localeMap == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(localeMap);
    }
    
    /**
     * Clears all translations for a namespace.
     * 
     * @param namespace The namespace to clear
     */
    public void clearNamespace(@NotNull String namespace) {
        translations.remove(namespace);
    }
    
    /**
     * Clears all translations.
     */
    public void clearAll() {
        translations.clear();
    }
    
    @Nullable
    private String translateFromLocale(Map<Locale, Map<String, String>> namespaceMap,
                                       Locale locale, String key) {
        Map<String, String> localeMap = namespaceMap.get(locale);
        if (localeMap != null) {
            return localeMap.get(key);
        }
        return null;
    }
}
