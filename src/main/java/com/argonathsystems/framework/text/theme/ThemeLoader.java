package com.argonathsystems.framework.text.theme;

import com.argonathsystems.framework.text.Style;
import com.argonathsystems.framework.text.TextColor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Loads and saves themes from/to JSON files.
 * 
 * <h2>JSON Format:</h2>
 * <pre>{@code
 * {
 *   "name": "my-theme",
 *   "author": "username",
 *   "version": "1.0.0",
 *   "description": "A beautiful custom theme",
 *   "isDarkTheme": true,
 *   "palette": {
 *     "primary": "#FF5500",
 *     "secondary": "#00AAFF",
 *     "error": "#FF0000",
 *     "rarity.legendary": "#FFD700"
 *   },
 *   "styles": {
 *     "heading": {
 *       "color": "#FFFFFF",
 *       "bold": true
 *     },
 *     "body": {
 *       "color": "#CCCCCC"
 *     }
 *   }
 * }
 * }</pre>
 */
public final class ThemeLoader {
    
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();
    
    private ThemeLoader() {
        // Utility class
    }
    
    /**
     * Loads a theme from a JSON file.
     * 
     * @param path The path to the theme file
     * @return The loaded theme
     * @throws IOException if the file cannot be read
     * @throws ThemeParseException if the JSON is invalid
     */
    public static @NotNull Theme load(@NotNull Path path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return load(reader);
        }
    }
    
    /**
     * Loads a theme from a Reader.
     * 
     * @param reader The reader to read from
     * @return The loaded theme
     * @throws ThemeParseException if the JSON is invalid
     */
    public static @NotNull Theme load(@NotNull Reader reader) {
        Objects.requireNonNull(reader, "reader cannot be null");
        
        JsonObject json = GSON.fromJson(reader, JsonObject.class);
        return parse(json);
    }
    
    /**
     * Loads a theme from a JSON string.
     * 
     * @param jsonString The JSON string
     * @return The loaded theme
     * @throws ThemeParseException if the JSON is invalid
     */
    public static @NotNull Theme loadFromString(@NotNull String jsonString) {
        Objects.requireNonNull(jsonString, "jsonString cannot be null");
        
        JsonObject json = GSON.fromJson(jsonString, JsonObject.class);
        return parse(json);
    }
    
    /**
     * Saves a theme to a JSON file.
     * 
     * @param theme The theme to save
     * @param path The destination path
     * @throws IOException if the file cannot be written
     */
    public static void save(@NotNull Theme theme, @NotNull Path path) throws IOException {
        Objects.requireNonNull(theme, "theme cannot be null");
        Objects.requireNonNull(path, "path cannot be null");
        
        // Ensure parent directories exist
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            save(theme, writer);
        }
    }
    
    /**
     * Saves a theme to a Writer.
     * 
     * @param theme The theme to save
     * @param writer The writer to write to
     */
    public static void save(@NotNull Theme theme, @NotNull Writer writer) {
        JsonObject json = serialize(theme);
        GSON.toJson(json, writer);
    }
    
    /**
     * Converts a theme to a JSON string.
     * 
     * @param theme The theme to convert
     * @return The JSON string
     */
    public static @NotNull String toJsonString(@NotNull Theme theme) {
        JsonObject json = serialize(theme);
        return GSON.toJson(json);
    }
    
    private static Theme parse(JsonObject json) {
        if (json == null) {
            throw new ThemeParseException("JSON object is null");
        }
        
        // Required: name
        String name = getRequiredString(json, "name");
        
        // Optional metadata
        String author = getOptionalString(json, "author", null);
        String version = getOptionalString(json, "version", "1.0.0");
        String description = getOptionalString(json, "description", null);
        boolean isDarkTheme = json.has("isDarkTheme") && json.get("isDarkTheme").getAsBoolean();
        
        Theme.Builder builder = Theme.builder(name)
                .author(author)
                .version(version)
                .description(description)
                .darkTheme(isDarkTheme);
        
        // Parse palette
        if (json.has("palette") && json.get("palette").isJsonObject()) {
            JsonObject paletteJson = json.getAsJsonObject("palette");
            Palette.Builder paletteBuilder = Palette.builder(name + "-palette");
            
            for (Map.Entry<String, JsonElement> entry : paletteJson.entrySet()) {
                String colorKey = entry.getKey();
                String colorValue = entry.getValue().getAsString();
                TextColor color = parseColor(colorValue);
                paletteBuilder.color(colorKey, color);
            }
            
            builder.palette(paletteBuilder.build());
        }
        
        // Parse styles
        if (json.has("styles") && json.get("styles").isJsonObject()) {
            JsonObject stylesJson = json.getAsJsonObject("styles");
            
            for (Map.Entry<String, JsonElement> entry : stylesJson.entrySet()) {
                String styleName = entry.getKey();
                if (entry.getValue().isJsonObject()) {
                    JsonObject styleJson = entry.getValue().getAsJsonObject();
                    Style style = parseStyle(styleJson);
                    builder.style(styleName, style);
                }
            }
        }
        
        return builder.build();
    }
    
    private static Style parseStyle(JsonObject json) {
        Style.Builder builder = Style.builder();
        
        if (json.has("color")) {
            builder.color(parseColor(json.get("color").getAsString()));
        }
        
        if (json.has("bold")) {
            builder.bold(json.get("bold").getAsBoolean());
        }
        
        if (json.has("italic")) {
            builder.italic(json.get("italic").getAsBoolean());
        }
        
        if (json.has("underlined")) {
            builder.underlined(json.get("underlined").getAsBoolean());
        }
        
        if (json.has("strikethrough")) {
            builder.strikethrough(json.get("strikethrough").getAsBoolean());
        }
        
        if (json.has("obfuscated")) {
            builder.obfuscated(json.get("obfuscated").getAsBoolean());
        }
        
        return builder.build();
    }
    
    private static TextColor parseColor(String value) {
        if (value.startsWith("#")) {
            // Hex color
            try {
                int rgb = Integer.parseInt(value.substring(1), 16);
                return TextColor.of(rgb);
            } catch (NumberFormatException e) {
                throw new ThemeParseException("Invalid hex color: " + value);
            }
        }
        
        // Try named color
        TextColor named = TextColor.named(value);
        if (named != null) {
            return named;
        }
        
        throw new ThemeParseException("Unknown color format: " + value);
    }
    
    private static JsonObject serialize(Theme theme) {
        JsonObject json = new JsonObject();
        
        json.addProperty("name", theme.name());
        
        Theme.ThemeMetadata meta = theme.metadata();
        if (meta.author() != null) {
            json.addProperty("author", meta.author());
        }
        if (meta.version() != null) {
            json.addProperty("version", meta.version());
        }
        if (meta.description() != null) {
            json.addProperty("description", meta.description());
        }
        json.addProperty("isDarkTheme", meta.isDarkTheme());
        
        // Serialize palette
        JsonObject paletteJson = new JsonObject();
        for (String colorKey : theme.palette().keys()) {
            TextColor color = theme.palette().get(colorKey);
            if (color != null) {
                paletteJson.addProperty(colorKey, formatColor(color));
            }
        }
        json.add("palette", paletteJson);
        
        // Serialize styles
        JsonObject stylesJson = new JsonObject();
        for (String styleName : theme.styleNames()) {
            Style style = theme.style(styleName);
            if (style != null) {
                stylesJson.add(styleName, serializeStyle(style));
            }
        }
        json.add("styles", stylesJson);
        
        return json;
    }
    
    private static JsonObject serializeStyle(Style style) {
        JsonObject json = new JsonObject();
        
        if (style.color() != null) {
            json.addProperty("color", formatColor(style.color()));
        }
        
        if (style.bold() != null) {
            json.addProperty("bold", style.bold());
        }
        
        if (style.italic() != null) {
            json.addProperty("italic", style.italic());
        }
        
        if (style.underlined() != null) {
            json.addProperty("underlined", style.underlined());
        }
        
        if (style.strikethrough() != null) {
            json.addProperty("strikethrough", style.strikethrough());
        }
        
        if (style.obfuscated() != null) {
            json.addProperty("obfuscated", style.obfuscated());
        }
        
        return json;
    }
    
    private static String formatColor(TextColor color) {
        int value = color.value();
        return String.format("#%06X", value & 0xFFFFFF);
    }
    
    private static String getRequiredString(JsonObject json, String key) {
        if (!json.has(key)) {
            throw new ThemeParseException("Missing required field: " + key);
        }
        return json.get(key).getAsString();
    }
    
    private static String getOptionalString(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }
    
    /**
     * Exception thrown when theme parsing fails.
     */
    public static class ThemeParseException extends RuntimeException {
        public ThemeParseException(String message) {
            super(message);
        }
        
        public ThemeParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
