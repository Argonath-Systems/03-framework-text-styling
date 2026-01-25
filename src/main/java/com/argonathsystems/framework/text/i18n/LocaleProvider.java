package com.argonathsystems.framework.text.i18n;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for providing locale information for players or contexts.
 * Implementations should be provided by the adapter layer to access
 * player preferences or client settings.
 * 
 * <p>This is a platform-agnostic interface - no Hytale imports allowed.</p>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * LocaleProvider provider = ...; // Injected or obtained from context
 * 
 * // Get locale for a player
 * Locale playerLocale = provider.getLocale(playerId);
 * 
 * // Use for translation
 * String message = TranslationRegistry.get()
 *     .translateOrKey(TranslationKey.of("ui.welcome"), playerLocale);
 * }</pre>
 */
@FunctionalInterface
public interface LocaleProvider {
    
    /**
     * Gets the locale for a specific player.
     * 
     * @param playerId The player's unique identifier
     * @return The player's preferred locale
     */
    @NotNull Locale getLocale(@NotNull UUID playerId);
    
    /**
     * Creates a provider that always returns the default locale.
     * 
     * @return A static locale provider
     */
    static LocaleProvider defaultLocale() {
        return playerId -> Locale.DEFAULT;
    }
    
    /**
     * Creates a provider that always returns a specific locale.
     * 
     * @param locale The locale to return
     * @return A static locale provider
     */
    static LocaleProvider fixed(@NotNull Locale locale) {
        return playerId -> locale;
    }
}
