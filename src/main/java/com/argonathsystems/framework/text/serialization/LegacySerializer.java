package com.argonathsystems.framework.text.serialization;

import com.argonathsystems.framework.text.Component;
import com.argonathsystems.framework.text.Style;
import com.argonathsystems.framework.text.TextColor;
import com.argonathsystems.framework.text.TextComponent;
import com.argonathsystems.framework.text.TranslatableComponent;
import com.argonathsystems.framework.text.i18n.Locale;
import com.argonathsystems.framework.text.i18n.TranslationRegistry;
import com.argonathsystems.framework.text.render.ComponentVisitor;
import com.argonathsystems.framework.text.render.ScoreComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Serializer that converts Components to legacy Minecraft §-code format.
 * Compatible with legacy chat systems and older Minecraft versions.
 * 
 * <h2>Supported Codes:</h2>
 * <ul>
 *   <li>§0-§9, §a-§f - Colors (black through white)</li>
 *   <li>§k - Obfuscated</li>
 *   <li>§l - Bold</li>
 *   <li>§m - Strikethrough</li>
 *   <li>§n - Underline</li>
 *   <li>§o - Italic</li>
 *   <li>§r - Reset</li>
 * </ul>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * LegacySerializer serializer = LegacySerializer.get();
 * 
 * Component styled = MiniMessage.parse("<red><bold>Error!</bold></red>");
 * String legacy = serializer.serialize(styled);
 * // Result: "§c§lError!§r"
 * }</pre>
 */
public final class LegacySerializer implements ComponentVisitor<String> {
    
    /** The section sign character used for legacy codes */
    public static final char SECTION_SIGN = '§';
    
    /** Alternative character often used in configs (ampersand) */
    public static final char AMPERSAND = '&';
    
    private static final LegacySerializer INSTANCE = new LegacySerializer(
        SECTION_SIGN, Locale.ENGLISH, TranslationRegistry.get()
    );
    
    private static final Map<String, Character> NAMED_COLOR_CODES = Map.ofEntries(
        Map.entry("black", '0'),
        Map.entry("dark_blue", '1'),
        Map.entry("dark_green", '2'),
        Map.entry("dark_aqua", '3'),
        Map.entry("dark_red", '4'),
        Map.entry("dark_purple", '5'),
        Map.entry("gold", '6'),
        Map.entry("gray", '7'),
        Map.entry("dark_gray", '8'),
        Map.entry("blue", '9'),
        Map.entry("green", 'a'),
        Map.entry("aqua", 'b'),
        Map.entry("red", 'c'),
        Map.entry("light_purple", 'd'),
        Map.entry("yellow", 'e'),
        Map.entry("white", 'f')
    );
    
    private final char colorChar;
    private final Locale locale;
    private final TranslationRegistry translationRegistry;
    
    private LegacySerializer(char colorChar, @NotNull Locale locale, @NotNull TranslationRegistry registry) {
        this.colorChar = colorChar;
        this.locale = locale;
        this.translationRegistry = registry;
    }
    
    /**
     * Returns the default serializer using § character.
     * 
     * @return The default LegacySerializer
     */
    public static LegacySerializer get() {
        return INSTANCE;
    }
    
    /**
     * Creates a serializer using ampersand (&) as the color character.
     * Common for configuration files and user input.
     * 
     * @return A new LegacySerializer using &
     */
    public static LegacySerializer ampersand() {
        return new LegacySerializer(AMPERSAND, Locale.ENGLISH, TranslationRegistry.get());
    }
    
    /**
     * Creates a serializer with a custom color character.
     * 
     * @param colorChar The character to use for color codes
     * @return A new LegacySerializer
     */
    public static LegacySerializer withChar(char colorChar) {
        return new LegacySerializer(colorChar, Locale.ENGLISH, TranslationRegistry.get());
    }
    
    /**
     * Creates a serializer for a specific locale.
     * 
     * @param locale The locale for translation resolution
     * @return A new LegacySerializer
     */
    public static LegacySerializer forLocale(@NotNull Locale locale) {
        return new LegacySerializer(SECTION_SIGN, locale, TranslationRegistry.get());
    }
    
    /**
     * Serializes a component to legacy format.
     * 
     * @param component The component to serialize
     * @return The legacy-formatted string
     */
    public @NotNull String serialize(@NotNull Component component) {
        StringBuilder sb = new StringBuilder();
        serializeRecursive(component, sb, null);
        return sb.toString();
    }
    
    private void serializeRecursive(Component component, StringBuilder sb, @Nullable Style parentStyle) {
        Style style = component.style();
        
        // Apply style codes
        appendStyleCodes(sb, style, parentStyle);
        
        // Visit this component for content
        String content = ComponentVisitor.visit(component, this);
        sb.append(content);
        
        // Process children
        for (Component child : component.children()) {
            serializeRecursive(child, sb, style);
        }
        
        // Reset if we applied any formatting
        if (hasFormatting(style)) {
            sb.append(colorChar).append('r');
        }
    }
    
    private void appendStyleCodes(StringBuilder sb, Style style, @Nullable Style parentStyle) {
        // Color
        if (style.color() != null) {
            char code = getColorCode(style.color());
            sb.append(colorChar).append(code);
        }
        
        // Decorations
        if (Boolean.TRUE.equals(style.obfuscated())) {
            sb.append(colorChar).append('k');
        }
        if (Boolean.TRUE.equals(style.bold())) {
            sb.append(colorChar).append('l');
        }
        if (Boolean.TRUE.equals(style.strikethrough())) {
            sb.append(colorChar).append('m');
        }
        if (Boolean.TRUE.equals(style.underlined())) {
            sb.append(colorChar).append('n');
        }
        if (Boolean.TRUE.equals(style.italic())) {
            sb.append(colorChar).append('o');
        }
    }
    
    private boolean hasFormatting(Style style) {
        return style.color() != null ||
               Boolean.TRUE.equals(style.bold()) ||
               Boolean.TRUE.equals(style.italic()) ||
               Boolean.TRUE.equals(style.underlined()) ||
               Boolean.TRUE.equals(style.strikethrough()) ||
               Boolean.TRUE.equals(style.obfuscated());
    }
    
    private char getColorCode(TextColor color) {
        // Try named color first
        String name = color.name();
        if (name != null) {
            Character code = NAMED_COLOR_CODES.get(name.toLowerCase());
            if (code != null) {
                return code;
            }
        }
        
        // Approximate hex color to nearest legacy color
        int rgb = color.value();
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return approximateLegacyColor(r, g, b);
    }
    
    private char approximateLegacyColor(int r, int g, int b) {
        // Simple nearest-color approximation
        int brightness = (r + g + b) / 3;
        
        if (brightness < 43) return '0';  // black
        if (r > 200 && g < 100 && b < 100) return 'c';  // red
        if (r < 100 && g > 200 && b < 100) return 'a';  // green
        if (r < 100 && g < 100 && b > 200) return '9';  // blue
        if (r > 200 && g > 200 && b < 100) return 'e';  // yellow
        if (r > 200 && g < 100 && b > 200) return 'd';  // light_purple
        if (r < 100 && g > 200 && b > 200) return 'b';  // aqua
        if (r > 200 && g > 100 && b < 100) return '6';  // gold
        if (brightness > 200) return 'f';  // white
        if (brightness > 128) return '7';  // gray
        return '8';  // dark_gray
    }
    
    @Override
    public String visitText(@NotNull TextComponent component) {
        return component.content();
    }
    
    @Override
    public String visitTranslatable(@NotNull TranslatableComponent component) {
        TextComponent resolved = component.resolve(locale, translationRegistry);
        return resolved.content();
    }
    
    @Override
    public String visitScore(@NotNull ScoreComponent component) {
        if (component.fallback() != null) {
            return component.fallback();
        }
        return "[" + component.objective() + "]";
    }
    
    @Override
    public String visitUnknown(@NotNull Component component) {
        return "";
    }
    
    // =========================================================================
    // Deserialization (Legacy -> Component)
    // =========================================================================
    
    /**
     * Parses a legacy-formatted string into a Component.
     * 
     * @param legacy The legacy-formatted string
     * @return The parsed component
     */
    public @NotNull Component deserialize(@NotNull String legacy) {
        return deserialize(legacy, colorChar);
    }
    
    /**
     * Parses a legacy-formatted string with a specific color character.
     * 
     * @param legacy The legacy-formatted string
     * @param colorChar The color character to look for
     * @return The parsed component
     */
    public static @NotNull Component deserialize(@NotNull String legacy, char colorChar) {
        TextComponent root = new TextComponent("");
        StringBuilder currentText = new StringBuilder();
        Style.Builder styleBuilder = Style.builder();
        
        for (int i = 0; i < legacy.length(); i++) {
            char c = legacy.charAt(i);
            
            if (c == colorChar && i + 1 < legacy.length()) {
                // Flush current text with current style
                if (!currentText.isEmpty()) {
                    TextComponent segment = new TextComponent(currentText.toString());
                    segment.style(styleBuilder.build());
                    root.append(segment);
                    currentText.setLength(0);
                }
                
                char code = Character.toLowerCase(legacy.charAt(++i));
                styleBuilder = applyLegacyCode(styleBuilder, code);
            } else {
                currentText.append(c);
            }
        }
        
        // Flush remaining text
        if (!currentText.isEmpty()) {
            TextComponent segment = new TextComponent(currentText.toString());
            segment.style(styleBuilder.build());
            root.append(segment);
        }
        
        return root;
    }
    
    private static Style.Builder applyLegacyCode(Style.Builder builder, char code) {
        return switch (code) {
            case '0' -> builder.color(colorFromRgb("black", 0, 0, 0));
            case '1' -> builder.color(colorFromRgb("dark_blue", 0, 0, 170));
            case '2' -> builder.color(colorFromRgb("dark_green", 0, 170, 0));
            case '3' -> builder.color(colorFromRgb("dark_aqua", 0, 170, 170));
            case '4' -> builder.color(colorFromRgb("dark_red", 170, 0, 0));
            case '5' -> builder.color(colorFromRgb("dark_purple", 170, 0, 170));
            case '6' -> builder.color(colorFromRgb("gold", 255, 170, 0));
            case '7' -> builder.color(colorFromRgb("gray", 170, 170, 170));
            case '8' -> builder.color(colorFromRgb("dark_gray", 85, 85, 85));
            case '9' -> builder.color(colorFromRgb("blue", 85, 85, 255));
            case 'a' -> builder.color(colorFromRgb("green", 85, 255, 85));
            case 'b' -> builder.color(colorFromRgb("aqua", 85, 255, 255));
            case 'c' -> builder.color(colorFromRgb("red", 255, 85, 85));
            case 'd' -> builder.color(colorFromRgb("light_purple", 255, 85, 255));
            case 'e' -> builder.color(colorFromRgb("yellow", 255, 255, 85));
            case 'f' -> builder.color(colorFromRgb("white", 255, 255, 255));
            case 'k' -> builder.obfuscated(true);
            case 'l' -> builder.bold(true);
            case 'm' -> builder.strikethrough(true);
            case 'n' -> builder.underlined(true);
            case 'o' -> builder.italic(true);
            case 'r' -> Style.builder();  // Reset - return fresh builder
            default -> builder;
        };
    }
    
    private static TextColor colorFromRgb(String name, int r, int g, int b) {
        int rgb = (r << 16) | (g << 8) | b;
        return new TextColor(rgb, name);
    }
}
