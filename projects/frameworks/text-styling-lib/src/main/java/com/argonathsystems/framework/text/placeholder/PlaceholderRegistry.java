package com.argonathsystems.framework.text.placeholder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

/**
 * Registry for placeholder resolvers organized by prefix/namespace.
 * Allows multiple mods to register their own placeholders without conflicts.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * PlaceholderRegistry registry = PlaceholderRegistry.get();
 * 
 * // Register resolvers
 * registry.register("player", ctx -> switch (ctx.key()) {
 *     case "player_name" -> Optional.of(getPlayerName(ctx.viewerId()));
 *     case "player_level" -> Optional.of(String.valueOf(getLevel(ctx.viewerId())));
 *     default -> Optional.empty();
 * });
 * 
 * registry.register("server", ctx -> switch (ctx.key()) {
 *     case "server_name" -> Optional.of("My Server");
 *     case "server_online" -> Optional.of(String.valueOf(getOnlineCount()));
 *     default -> Optional.empty();
 * });
 * 
 * // Use combined resolver
 * String result = PlaceholderProcessor.process(
 *     "Hello {player_name} on {server_name}!",
 *     registry.combined()
 * );
 * }</pre>
 */
public class PlaceholderRegistry {
    
    private static final PlaceholderRegistry INSTANCE = new PlaceholderRegistry();
    
    private final Map<String, PlaceholderResolver> resolvers;
    
    public PlaceholderRegistry() {
        this.resolvers = new ConcurrentHashMap<>();
    }
    
    /**
     * Returns the global placeholder registry instance.
     */
    public static PlaceholderRegistry get() {
        return INSTANCE;
    }
    
    /**
     * Registers a placeholder resolver with a namespace prefix.
     * Placeholders matching "{prefix_*}" will be routed to this resolver.
     * 
     * @param prefix The namespace prefix (e.g., "player", "server", "quest")
     * @param resolver The resolver for this namespace
     */
    public void register(@NotNull String prefix, @NotNull PlaceholderResolver resolver) {
        resolvers.put(prefix, resolver);
    }
    
    /**
     * Unregisters a placeholder resolver.
     * 
     * @param prefix The namespace prefix to remove
     */
    public void unregister(@NotNull String prefix) {
        resolvers.remove(prefix);
    }
    
    /**
     * Gets the resolver for a specific prefix.
     * 
     * @param prefix The namespace prefix
     * @return The resolver, or empty if not registered
     */
    public Optional<PlaceholderResolver> get(@NotNull String prefix) {
        return Optional.ofNullable(resolvers.get(prefix));
    }
    
    /**
     * Returns a combined resolver that checks all registered resolvers.
     * For a placeholder like "player_name", it will:
     * 1. Try to find a resolver for "player" prefix
     * 2. Fall back to trying all resolvers
     * 
     * @return A combined placeholder resolver
     */
    public PlaceholderResolver combined() {
        return context -> {
            String key = context.key();
            
            // Try prefix-based routing
            int underscoreIndex = key.indexOf('_');
            if (underscoreIndex > 0) {
                String prefix = key.substring(0, underscoreIndex);
                PlaceholderResolver prefixResolver = resolvers.get(prefix);
                if (prefixResolver != null) {
                    Optional<String> result = prefixResolver.resolve(context);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }
            
            // Fall back to trying all resolvers
            for (PlaceholderResolver resolver : resolvers.values()) {
                Optional<String> result = resolver.resolve(context);
                if (result.isPresent()) {
                    return result;
                }
            }
            
            return Optional.empty();
        };
    }
    
    /**
     * Clears all registered resolvers.
     */
    public void clear() {
        resolvers.clear();
    }
    
    /**
     * Returns the number of registered resolvers.
     */
    public int size() {
        return resolvers.size();
    }
}
