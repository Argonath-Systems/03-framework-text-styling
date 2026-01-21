package com.argonathsystems.framework.text.placeholder;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for resolving placeholders in text.
 * Implementations provide values for placeholder keys like "player_name", "balance", etc.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * PlaceholderResolver resolver = context -> switch (context.key()) {
 *     case "player_name" -> Optional.of("Steve");
 *     case "balance" -> Optional.of("1,000");
 *     default -> Optional.empty();
 * };
 * 
 * String result = PlaceholderProcessor.process("Hello, {player_name}!", resolver);
 * // Result: "Hello, Steve!"
 * }</pre>
 */
@FunctionalInterface
public interface PlaceholderResolver {
    
    /**
     * Resolves a placeholder key to its value.
     * 
     * @param context The placeholder context with key and optional parameters
     * @return The resolved value, or empty if this resolver doesn't handle the key
     */
    Optional<String> resolve(@NotNull PlaceholderContext context);
    
    /**
     * Creates a resolver that chains multiple resolvers together.
     * The first resolver to return a non-empty value wins.
     * 
     * @param resolvers The resolvers to chain
     * @return A combined resolver
     */
    static PlaceholderResolver chain(PlaceholderResolver... resolvers) {
        return context -> {
            for (PlaceholderResolver resolver : resolvers) {
                Optional<String> result = resolver.resolve(context);
                if (result.isPresent()) {
                    return result;
                }
            }
            return Optional.empty();
        };
    }
    
    /**
     * Creates a resolver that returns a fixed value for a specific key.
     * 
     * @param key The placeholder key
     * @param value The value to return
     * @return A single-key resolver
     */
    static PlaceholderResolver fixed(@NotNull String key, @NotNull String value) {
        return context -> context.key().equals(key) ? Optional.of(value) : Optional.empty();
    }
    
    /**
     * Returns an empty resolver that resolves nothing.
     */
    static PlaceholderResolver empty() {
        return context -> Optional.empty();
    }
}
