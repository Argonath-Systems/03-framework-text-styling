package com.argonathsystems.framework.text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for reusable text styles (e.g. "rarity.epic", "ui.header").
 */
public class StyleRegistry {
    
    private static final StyleRegistry INSTANCE = new StyleRegistry();
    private final Map<String, Style> styles = new HashMap<>();

    public static StyleRegistry get() {
        return INSTANCE;
    }

    public void register(String key, Style style) {
        styles.put(key, style);
    }
    
    public Optional<Style> get(String key) {
        return Optional.ofNullable(styles.get(key));
    }
    
    public Style getOrEmpty(String key) {
        return styles.getOrDefault(key, Style.empty());
    }
}
