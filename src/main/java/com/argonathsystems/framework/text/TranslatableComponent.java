package com.argonathsystems.framework.text;

import com.argonathsystems.framework.text.i18n.Locale;
import com.argonathsystems.framework.text.i18n.TranslationArgument;
import com.argonathsystems.framework.text.i18n.TranslationKey;
import com.argonathsystems.framework.text.i18n.TranslationRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A component that represents translatable text.
 * The actual text is resolved at render time based on the viewer's locale.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Simple translatable text
 * Component message = TranslatableComponent.of("ui.button.confirm");
 * 
 * // With type-safe arguments
 * Component welcome = TranslatableComponent.of("ui.welcome", 
 *     TranslationArgument.string(playerName),
 *     TranslationArgument.number(level));
 * 
 * // With component arguments
 * Component complex = TranslatableComponent.of("chat.whisper",
 *     TranslationArgument.component(senderName),
 *     TranslationArgument.string(message));
 * 
 * // With style
 * Component styled = TranslatableComponent.of("ui.error.generic")
 *     .style(Style.of(TextColor.RED));
 * }</pre>
 */
public class TranslatableComponent implements Component {
    
    private final TranslationKey key;
    private final List<TranslationArgument> arguments;
    private Style style;
    private final List<Component> children;
    private String fallback;
    
    private TranslatableComponent(@NotNull TranslationKey key, @NotNull List<TranslationArgument> arguments) {
        this.key = Objects.requireNonNull(key, "key cannot be null");
        this.arguments = new ArrayList<>(arguments);
        this.style = Style.empty();
        this.children = new ArrayList<>();
        this.fallback = null;
    }
    
    /**
     * Creates a translatable component with the given key.
     * Uses the default "core" namespace.
     * 
     * @param key The translation key
     * @return A new TranslatableComponent
     */
    public static TranslatableComponent of(@NotNull String key) {
        return new TranslatableComponent(TranslationKey.of(key), List.of());
    }
    
    /**
     * Creates a translatable component with the given key and type-safe arguments.
     * Arguments replace placeholders like {0}, {1}, etc.
     * 
     * @param key The translation key
     * @param args The placeholder arguments
     * @return A new TranslatableComponent
     */
    public static TranslatableComponent of(@NotNull String key, TranslationArgument... args) {
        return new TranslatableComponent(TranslationKey.of(key), Arrays.asList(args));
    }
    
    /**
     * Creates a translatable component with a namespaced key.
     * 
     * @param namespace The namespace (e.g., "quest-mod")
     * @param key The translation key
     * @return A new TranslatableComponent
     */
    public static TranslatableComponent of(@NotNull String namespace, @NotNull String key) {
        return new TranslatableComponent(TranslationKey.of(namespace, key), List.of());
    }
    
    /**
     * Creates a translatable component with a namespaced key and arguments.
     * 
     * @param namespace The namespace
     * @param key The translation key
     * @param args The placeholder arguments
     * @return A new TranslatableComponent
     */
    public static TranslatableComponent of(@NotNull String namespace, @NotNull String key, TranslationArgument... args) {
        return new TranslatableComponent(TranslationKey.of(namespace, key), Arrays.asList(args));
    }
    
    /**
     * Creates a translatable component from a TranslationKey.
     * 
     * @param translationKey The translation key
     * @return A new TranslatableComponent
     */
    public static TranslatableComponent of(@NotNull TranslationKey translationKey) {
        return new TranslatableComponent(translationKey, List.of());
    }
    
    /**
     * Creates a translatable component from a TranslationKey with arguments.
     * 
     * @param translationKey The translation key
     * @param args The placeholder arguments
     * @return A new TranslatableComponent
     */
    public static TranslatableComponent of(@NotNull TranslationKey translationKey, TranslationArgument... args) {
        return new TranslatableComponent(translationKey, Arrays.asList(args));
    }
    
    // =========================================================================
    // Convenience factory methods for common argument types
    // =========================================================================
    
    /**
     * Creates a translatable component with string arguments (convenience method).
     * 
     * @param key The translation key
     * @param stringArgs The string arguments
     * @return A new TranslatableComponent
     */
    public static TranslatableComponent withStrings(@NotNull String key, String... stringArgs) {
        List<TranslationArgument> args = Arrays.stream(stringArgs)
            .map(TranslationArgument::string)
            .toList();
        return new TranslatableComponent(TranslationKey.of(key), args);
    }
    
    /**
     * Returns the translation key.
     * 
     * @return The translation key
     */
    public TranslationKey key() {
        return key;
    }
    
    /**
     * Returns the arguments for placeholder substitution.
     * 
     * @return The arguments list (immutable copy)
     */
    public List<TranslationArgument> arguments() {
        return List.copyOf(arguments);
    }
    
    /**
     * Adds a type-safe argument for placeholder substitution.
     * 
     * @param arg The argument to add
     * @return This component for chaining
     */
    public TranslatableComponent argument(@NotNull TranslationArgument arg) {
        Objects.requireNonNull(arg, "arg cannot be null");
        this.arguments.add(arg);
        return this;
    }
    
    /**
     * Adds a string argument (convenience method).
     * 
     * @param value The string value
     * @return This component for chaining
     */
    public TranslatableComponent argumentString(@NotNull String value) {
        return argument(TranslationArgument.string(value));
    }
    
    /**
     * Adds a number argument (convenience method).
     * 
     * @param value The numeric value
     * @return This component for chaining
     */
    public TranslatableComponent argumentNumber(long value) {
        return argument(TranslationArgument.number(value));
    }
    
    /**
     * Adds a component argument (convenience method).
     * 
     * @param component The component to embed
     * @return This component for chaining
     */
    public TranslatableComponent argumentComponent(@NotNull Component component) {
        return argument(TranslationArgument.component(component));
    }
    
    /**
     * Sets a fallback string to use if translation is not found.
     * 
     * @param fallback The fallback string
     * @return This component for chaining
     */
    public TranslatableComponent fallback(String fallback) {
        this.fallback = fallback;
        return this;
    }
    
    /**
     * Returns the fallback string.
     * 
     * @return The fallback string, or null if not set
     */
    public String fallback() {
        return fallback;
    }
    
    @Override
    public @NotNull Style style() {
        return style;
    }
    
    /**
     * Sets the style for this component.
     * 
     * @param style The style to apply
     * @return This component for chaining
     */
    public TranslatableComponent style(Style style) {
        this.style = Objects.requireNonNull(style, "style cannot be null");
        return this;
    }
    
    @Override
    public @NotNull List<Component> children() {
        return children;
    }
    
    /**
     * Resolves this translatable component to a concrete TextComponent
     * for the given locale.
     * 
     * @param locale The locale to use for translation
     * @return A resolved TextComponent with the translated text
     */
    public TextComponent resolve(@NotNull Locale locale) {
        return resolve(locale, TranslationRegistry.get());
    }
    
    /**
     * Resolves this translatable component using a specific translation registry.
     * 
     * @param locale The locale to use for translation
     * @param registry The translation registry to use
     * @return A resolved TextComponent with the translated text
     */
    public TextComponent resolve(@NotNull Locale locale, @NotNull TranslationRegistry registry) {
        // Convert TranslationArguments to string array for formatting
        String[] stringArgs = arguments.stream()
            .map(TranslationArgument::asString)
            .toArray(String[]::new);
        
        String translated = registry.translate(key, locale, stringArgs)
            .orElseGet(() -> fallback != null ? fallback : key.key());
        
        TextComponent result = new TextComponent(translated);
        result.style(style);
        
        // Resolve child components
        for (Component child : children) {
            if (child instanceof TranslatableComponent tc) {
                result.append(tc.resolve(locale, registry));
            } else {
                result.append(child);
            }
        }
        
        return result;
    }
    
    /**
     * Gets an argument as a ComponentArg if present and applicable.
     * Used by serializers that need to handle embedded components specially.
     * 
     * @param index The argument index
     * @return The component argument, or null if not a ComponentArg
     */
    public Component getComponentArgument(int index) {
        if (index < 0 || index >= arguments.size()) {
            return null;
        }
        TranslationArgument arg = arguments.get(index);
        if (arg instanceof TranslationArgument.ComponentArg ca) {
            return ca.component();
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "TranslatableComponent{key=" + key + ", arguments=" + arguments + 
               ", fallback=" + fallback + ", style=" + style + ", children=" + children + "}";
    }
}
