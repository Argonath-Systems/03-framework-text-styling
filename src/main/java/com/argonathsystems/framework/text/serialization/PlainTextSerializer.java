package com.argonathsystems.framework.text.serialization;

import com.argonathsystems.framework.text.Component;
import com.argonathsystems.framework.text.TextComponent;
import com.argonathsystems.framework.text.TranslatableComponent;
import com.argonathsystems.framework.text.i18n.Locale;
import com.argonathsystems.framework.text.i18n.TranslationRegistry;
import com.argonathsystems.framework.text.render.ComponentVisitor;
import com.argonathsystems.framework.text.render.ScoreComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Serializer that strips all formatting and returns plain text.
 * Useful for search indexing, logging, and accessibility.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * PlainTextSerializer serializer = PlainTextSerializer.get();
 * 
 * Component styled = MiniMessage.parse("<red>Hello</red> <bold>World</bold>!");
 * String plain = serializer.serialize(styled);
 * // Result: "Hello World!"
 * }</pre>
 */
public final class PlainTextSerializer implements ComponentVisitor<String> {
    
    private static final PlainTextSerializer INSTANCE = new PlainTextSerializer(
        Locale.ENGLISH, TranslationRegistry.get()
    );
    
    private final Locale locale;
    private final TranslationRegistry translationRegistry;
    
    private PlainTextSerializer(@NotNull Locale locale, @NotNull TranslationRegistry registry) {
        this.locale = locale;
        this.translationRegistry = registry;
    }
    
    /**
     * Returns the default serializer using English locale.
     * 
     * @return The default PlainTextSerializer
     */
    public static PlainTextSerializer get() {
        return INSTANCE;
    }
    
    /**
     * Creates a serializer for a specific locale.
     * Used to resolve TranslatableComponents properly.
     * 
     * @param locale The locale for translation resolution
     * @return A new PlainTextSerializer
     */
    public static PlainTextSerializer forLocale(@NotNull Locale locale) {
        return new PlainTextSerializer(locale, TranslationRegistry.get());
    }
    
    /**
     * Creates a serializer with custom locale and translation registry.
     * 
     * @param locale The locale for translation resolution
     * @param registry The translation registry to use
     * @return A new PlainTextSerializer
     */
    public static PlainTextSerializer create(@NotNull Locale locale, @NotNull TranslationRegistry registry) {
        return new PlainTextSerializer(locale, registry);
    }
    
    /**
     * Serializes a component to plain text.
     * 
     * @param component The component to serialize
     * @return The plain text content
     */
    public @NotNull String serialize(@NotNull Component component) {
        return ComponentVisitor.visitChildren(component, this, (a, b) -> a + b);
    }
    
    @Override
    public String visitText(@NotNull TextComponent component) {
        return component.content();
    }
    
    @Override
    public String visitTranslatable(@NotNull TranslatableComponent component) {
        // Resolve the translation to get the actual text
        TextComponent resolved = component.resolve(locale, translationRegistry);
        return resolved.content();
    }
    
    @Override
    public String visitScore(@NotNull ScoreComponent component) {
        // For scores, return fallback or placeholder
        if (component.fallback() != null) {
            return component.fallback();
        }
        return "[" + component.objective() + "]";
    }
    
    @Override
    public String visitUnknown(@NotNull Component component) {
        // Try to extract text from children
        StringBuilder sb = new StringBuilder();
        for (Component child : component.children()) {
            sb.append(serialize(child));
        }
        return sb.toString();
    }
}
