package com.argonathsystems.framework.text.serialization;

import com.argonathsystems.framework.text.*;
import com.argonathsystems.framework.text.event.ClickAction;
import com.argonathsystems.framework.text.event.HoverEvent;
import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Serializes and deserializes Component trees to/from JSON.
 * Compatible with Minecraft's raw JSON text format for maximum interoperability.
 * 
 * <h2>JSON Format:</h2>
 * <pre>{@code
 * {
 *   "text": "Hello ",
 *   "color": "#FF5500",
 *   "bold": true,
 *   "extra": [
 *     { "text": "World", "color": "gold" }
 *   ],
 *   "clickEvent": { "action": "run_command", "value": "/help" },
 *   "hoverEvent": { "action": "show_text", "contents": "Click me!" }
 * }
 * }</pre>
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * ComponentSerializer serializer = ComponentSerializer.get();
 * 
 * // Serialize
 * Component component = MiniMessage.parse("<red>Hello <bold>World</bold></red>");
 * String json = serializer.toJson(component);
 * 
 * // Deserialize
 * Component parsed = serializer.fromJson(json);
 * }</pre>
 */
public class ComponentSerializer {
    
    private static final ComponentSerializer INSTANCE = new ComponentSerializer();
    
    private final Gson gson;
    
    public ComponentSerializer() {
        this.gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Component.class, new ComponentAdapter())
            .registerTypeAdapter(TextColor.class, new TextColorAdapter())
            .registerTypeAdapter(ClickAction.class, new ClickActionAdapter())
            .registerTypeHierarchyAdapter(HoverEvent.class, new HoverEventAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    }
    
    /**
     * Returns the global serializer instance.
     */
    public static ComponentSerializer get() {
        return INSTANCE;
    }
    
    /**
     * Serializes a component to JSON.
     * 
     * @param component The component to serialize
     * @return The JSON string
     */
    public String toJson(@NotNull Component component) {
        return gson.toJson(component);
    }
    
    /**
     * Serializes a component to a compact JSON string (no pretty printing).
     * 
     * @param component The component to serialize
     * @return The compact JSON string
     */
    public String toCompactJson(@NotNull Component component) {
        Gson compactGson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Component.class, new ComponentAdapter())
            .registerTypeAdapter(TextColor.class, new TextColorAdapter())
            .registerTypeAdapter(ClickAction.class, new ClickActionAdapter())
            .registerTypeHierarchyAdapter(HoverEvent.class, new HoverEventAdapter())
            .disableHtmlEscaping()
            .create();
        return compactGson.toJson(component);
    }
    
    /**
     * Serializes a component to a JsonElement.
     * 
     * @param component The component to serialize
     * @return The JSON element
     */
    public JsonElement toJsonTree(@NotNull Component component) {
        return gson.toJsonTree(component);
    }
    
    /**
     * Deserializes a component from JSON.
     * 
     * @param json The JSON string
     * @return The parsed component
     */
    public Component fromJson(@NotNull String json) {
        return gson.fromJson(json, Component.class);
    }
    
    /**
     * Deserializes a component from a JsonElement.
     * 
     * @param element The JSON element
     * @return The parsed component
     */
    public Component fromJson(@NotNull JsonElement element) {
        return gson.fromJson(element, Component.class);
    }
    
    // ==================== Type Adapters ====================
    
    private static class ComponentAdapter implements JsonSerializer<Component>, JsonDeserializer<Component> {
        
        @Override
        public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            
            // Handle TextComponent
            if (src instanceof TextComponent textComponent) {
                obj.addProperty("text", textComponent.content());
            } else if (src instanceof TranslatableComponent translatable) {
                obj.addProperty("translate", translatable.key().fullKey());
                if (!translatable.arguments().isEmpty()) {
                    JsonArray with = new JsonArray();
                    for (Object arg : translatable.arguments()) {
                        if (arg instanceof Component comp) {
                            with.add(context.serialize(comp));
                        } else {
                            with.add(String.valueOf(arg));
                        }
                    }
                    obj.add("with", with);
                }
                if (translatable.fallback() != null) {
                    obj.addProperty("fallback", translatable.fallback());
                }
            }
            
            // Add style properties
            Style style = src.style();
            if (style.color() != null) {
                obj.add("color", context.serialize(style.color()));
            }
            if (style.font() != null) {
                obj.addProperty("font", style.font());
            }
            if (style.bold() != null) {
                obj.addProperty("bold", style.bold());
            }
            if (style.italic() != null) {
                obj.addProperty("italic", style.italic());
            }
            if (style.underlined() != null) {
                obj.addProperty("underlined", style.underlined());
            }
            if (style.strikethrough() != null) {
                obj.addProperty("strikethrough", style.strikethrough());
            }
            if (style.obfuscated() != null) {
                obj.addProperty("obfuscated", style.obfuscated());
            }
            if (style.insertion() != null) {
                obj.addProperty("insertion", style.insertion());
            }
            if (style.clickAction() != null) {
                obj.add("clickEvent", context.serialize(style.clickAction()));
            }
            if (style.hoverEvent() != null) {
                obj.add("hoverEvent", context.serialize(style.hoverEvent()));
            }
            
            // Add children
            if (!src.children().isEmpty()) {
                JsonArray extra = new JsonArray();
                for (Component child : src.children()) {
                    extra.add(context.serialize(child));
                }
                obj.add("extra", extra);
            }
            
            return obj;
        }
        
        @Override
        public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
                throws JsonParseException {
            
            // Handle string shorthand
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                return new TextComponent(json.getAsString());
            }
            
            // Handle array (list of components)
            if (json.isJsonArray()) {
                TextComponent root = new TextComponent("");
                for (JsonElement element : json.getAsJsonArray()) {
                    root.append(context.deserialize(element, Component.class));
                }
                return root;
            }
            
            JsonObject obj = json.getAsJsonObject();
            
            // Determine component type
            Component component;
            if (obj.has("translate")) {
                String key = obj.get("translate").getAsString();
                TranslatableComponent translatable = TranslatableComponent.of(
                    com.argonathsystems.framework.text.i18n.TranslationKey.parse(key)
                );
                if (obj.has("with")) {
                    for (JsonElement arg : obj.getAsJsonArray("with")) {
                        if (arg.isJsonObject() || arg.isJsonArray()) {
                            translatable.argument(context.deserialize(arg, Component.class));
                        } else {
                            translatable.argument(arg.getAsString());
                        }
                    }
                }
                if (obj.has("fallback")) {
                    translatable.fallback(obj.get("fallback").getAsString());
                }
                component = translatable;
            } else {
                String text = obj.has("text") ? obj.get("text").getAsString() : "";
                component = new TextComponent(text);
            }
            
            // Parse style
            Style.Builder styleBuilder = Style.builder();
            
            if (obj.has("color")) {
                styleBuilder.color(context.deserialize(obj.get("color"), TextColor.class));
            }
            if (obj.has("font")) {
                styleBuilder.font(obj.get("font").getAsString());
            }
            if (obj.has("bold")) {
                styleBuilder.bold(obj.get("bold").getAsBoolean());
            }
            if (obj.has("italic")) {
                styleBuilder.italic(obj.get("italic").getAsBoolean());
            }
            if (obj.has("underlined")) {
                styleBuilder.underlined(obj.get("underlined").getAsBoolean());
            }
            if (obj.has("strikethrough")) {
                styleBuilder.strikethrough(obj.get("strikethrough").getAsBoolean());
            }
            if (obj.has("obfuscated")) {
                styleBuilder.obfuscated(obj.get("obfuscated").getAsBoolean());
            }
            if (obj.has("insertion")) {
                styleBuilder.insertion(obj.get("insertion").getAsString());
            }
            if (obj.has("clickEvent")) {
                styleBuilder.clickAction(context.deserialize(obj.get("clickEvent"), ClickAction.class));
            }
            if (obj.has("hoverEvent")) {
                styleBuilder.hoverEvent(context.deserialize(obj.get("hoverEvent"), HoverEvent.class));
            }
            
            if (component instanceof TextComponent tc) {
                tc.style(styleBuilder.build());
            } else if (component instanceof TranslatableComponent tc) {
                tc.style(styleBuilder.build());
            }
            
            // Parse children
            if (obj.has("extra")) {
                for (JsonElement child : obj.getAsJsonArray("extra")) {
                    component.append(context.deserialize(child, Component.class));
                }
            }
            
            return component;
        }
    }
    
    private static class TextColorAdapter implements JsonSerializer<TextColor>, JsonDeserializer<TextColor> {
        
        @Override
        public JsonElement serialize(TextColor src, Type typeOfSrc, JsonSerializationContext context) {
            // Prefer name for standard colors, hex for custom
            if (src.name() != null) {
                return new JsonPrimitive(src.name());
            }
            return new JsonPrimitive(src.toHex().toLowerCase());
        }
        
        @Override
        public TextColor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            String value = json.getAsString();
            if (value.startsWith("#")) {
                return TextColor.fromHex(value);
            }
            // Named colors
            return switch (value.toLowerCase()) {
                case "black" -> TextColor.BLACK;
                case "white" -> TextColor.WHITE;
                case "red" -> TextColor.RED;
                case "green" -> TextColor.GREEN;
                case "blue" -> TextColor.BLUE;
                case "gold" -> TextColor.GOLD;
                case "gray", "grey" -> TextColor.GRAY;
                default -> TextColor.fromHex(value);
            };
        }
    }
    
    private static class ClickActionAdapter implements JsonSerializer<ClickAction>, JsonDeserializer<ClickAction> {
        
        @Override
        public JsonElement serialize(ClickAction src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("action", src.action().name().toLowerCase());
            obj.addProperty("value", src.value());
            return obj;
        }
        
        @Override
        public ClickAction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            String action = obj.get("action").getAsString();
            String value = obj.get("value").getAsString();
            
            return switch (action.toLowerCase()) {
                case "run_command" -> ClickAction.runCommand(value);
                case "suggest_command" -> ClickAction.suggestCommand(value);
                case "open_url" -> ClickAction.openUrl(value);
                case "copy_to_clipboard" -> ClickAction.copyToClipboard(value);
                case "change_page" -> ClickAction.changePage(Integer.parseInt(value));
                default -> throw new JsonParseException("Unknown click action: " + action);
            };
        }
    }
    
    private static class HoverEventAdapter implements JsonSerializer<HoverEvent>, JsonDeserializer<HoverEvent> {
        
        @Override
        public JsonElement serialize(HoverEvent src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("action", src.action().name().toLowerCase());
            
            if (src instanceof HoverEvent.ShowText showText) {
                obj.add("contents", context.serialize(showText.text()));
            } else if (src instanceof HoverEvent.ShowItem showItem) {
                JsonObject contents = new JsonObject();
                contents.addProperty("id", showItem.itemId());
                contents.addProperty("count", showItem.count());
                if (showItem.nbtData() != null) {
                    contents.addProperty("tag", showItem.nbtData());
                }
                obj.add("contents", contents);
            } else if (src instanceof HoverEvent.ShowEntity showEntity) {
                JsonObject contents = new JsonObject();
                contents.addProperty("type", showEntity.entityType());
                contents.addProperty("id", showEntity.entityId());
                if (showEntity.displayName() != null) {
                    contents.addProperty("name", showEntity.displayName());
                }
                obj.add("contents", contents);
            }
            
            return obj;
        }
        
        @Override
        public HoverEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            String action = obj.get("action").getAsString();
            JsonElement contents = obj.get("contents");
            
            return switch (action.toLowerCase()) {
                case "show_text" -> {
                    if (contents.isJsonPrimitive()) {
                        yield HoverEvent.showText(contents.getAsString());
                    }
                    Component text = context.deserialize(contents, Component.class);
                    yield HoverEvent.showText(text);
                }
                case "show_item" -> {
                    JsonObject itemObj = contents.getAsJsonObject();
                    String id = itemObj.get("id").getAsString();
                    int count = itemObj.has("count") ? itemObj.get("count").getAsInt() : 1;
                    String nbt = itemObj.has("tag") ? itemObj.get("tag").getAsString() : null;
                    yield HoverEvent.showItem(id, count, nbt);
                }
                case "show_entity" -> {
                    JsonObject entityObj = contents.getAsJsonObject();
                    String type = entityObj.get("type").getAsString();
                    String id = entityObj.get("id").getAsString();
                    String name = entityObj.has("name") ? entityObj.get("name").getAsString() : null;
                    yield HoverEvent.showEntity(type, id, name);
                }
                default -> throw new JsonParseException("Unknown hover action: " + action);
            };
        }
    }
}
