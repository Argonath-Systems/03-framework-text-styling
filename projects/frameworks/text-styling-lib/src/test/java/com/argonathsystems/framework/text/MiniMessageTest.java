package com.argonathsystems.framework.text;

import com.argonathsystems.framework.text.event.ClickAction;
import com.argonathsystems.framework.text.event.HoverEvent;
import com.argonathsystems.framework.text.placeholder.PlaceholderResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MiniMessage Parser Tests")
class MiniMessageTest {
    
    @Nested
    @DisplayName("Basic Parsing")
    class BasicParsing {
        
        @Test
        @DisplayName("should parse plain text")
        void parsePlainText() {
            Component result = MiniMessage.parse("Hello World");
            
            assertThat(result.children()).hasSize(1);
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.content()).isEqualTo("Hello World");
        }
        
        @Test
        @DisplayName("should parse empty string")
        void parseEmptyString() {
            Component result = MiniMessage.parse("");
            
            assertThat(result).isNotNull();
        }
        
        @Test
        @DisplayName("should parse null as empty")
        void parseNull() {
            Component result = MiniMessage.parse(null);
            
            assertThat(result).isNotNull();
        }
    }
    
    @Nested
    @DisplayName("Color Parsing")
    class ColorParsing {
        
        @Test
        @DisplayName("should parse named color")
        void parseNamedColor() {
            Component result = MiniMessage.parse("<red>Hello</red>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.content()).isEqualTo("Hello");
            assertThat(child.style().color()).isEqualTo(TextColor.RED);
        }
        
        @Test
        @DisplayName("should parse hex color")
        void parseHexColor() {
            Component result = MiniMessage.parse("<#FF5500>Orange</>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.content()).isEqualTo("Orange");
            assertThat(child.style().color().value()).isEqualTo(0xFF5500);
        }
        
        @Test
        @DisplayName("should parse multiple colors")
        void parseMultipleColors() {
            Component result = MiniMessage.parse("<red>Red</red> <blue>Blue</blue>");
            
            assertThat(result.children()).hasSizeGreaterThanOrEqualTo(3); // Red, space, Blue
        }
    }
    
    @Nested
    @DisplayName("Decoration Parsing")
    class DecorationParsing {
        
        @Test
        @DisplayName("should parse bold")
        void parseBold() {
            Component result = MiniMessage.parse("<bold>Strong</bold>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.style().bold()).isTrue();
        }
        
        @Test
        @DisplayName("should parse italic shorthand")
        void parseItalicShorthand() {
            Component result = MiniMessage.parse("<i>Emphasis</i>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.style().italic()).isTrue();
        }
        
        @Test
        @DisplayName("should parse underlined")
        void parseUnderlined() {
            Component result = MiniMessage.parse("<underlined>Underline</underlined>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.style().underlined()).isTrue();
        }
        
        @Test
        @DisplayName("should parse strikethrough")
        void parseStrikethrough() {
            Component result = MiniMessage.parse("<strikethrough>Deleted</strikethrough>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.style().strikethrough()).isTrue();
        }
        
        @Test
        @DisplayName("should parse obfuscated")
        void parseObfuscated() {
            Component result = MiniMessage.parse("<obfuscated>Secret</obfuscated>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.style().obfuscated()).isTrue();
        }
    }
    
    @Nested
    @DisplayName("Gradient Parsing")
    class GradientParsing {
        
        @Test
        @DisplayName("should parse gradient with two colors")
        void parseTwoColorGradient() {
            Component result = MiniMessage.parse("<gradient:red:gold>Sunset</gradient>");
            
            // Gradient creates multiple children for each character
            assertThat(result.children()).isNotEmpty();
        }
        
        @Test
        @DisplayName("should parse gradient with hex colors")
        void parseHexGradient() {
            Component result = MiniMessage.parse("<gradient:#FF0000:#00FF00>Rainbow</gradient>");
            
            assertThat(result.children()).isNotEmpty();
        }
    }
    
    @Nested
    @DisplayName("Click Action Parsing")
    class ClickActionParsing {
        
        @Test
        @DisplayName("should parse run_command")
        void parseRunCommand() {
            Component result = MiniMessage.parse("<click:run_command:/help>Click me</click>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.style().clickAction()).isNotNull();
            assertThat(child.style().clickAction().action()).isEqualTo(ClickAction.Action.RUN_COMMAND);
            assertThat(child.style().clickAction().value()).isEqualTo("/help");
        }
        
        @Test
        @DisplayName("should parse open_url")
        void parseOpenUrl() {
            Component result = MiniMessage.parse("<click:open_url:https://example.com>Link</click>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.style().clickAction().action()).isEqualTo(ClickAction.Action.OPEN_URL);
        }
    }
    
    @Nested
    @DisplayName("Hover Event Parsing")
    class HoverEventParsing {
        
        @Test
        @DisplayName("should parse show_text")
        void parseShowText() {
            Component result = MiniMessage.parse("<hover:show_text:Tooltip here>Hover me</hover>");
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.style().hoverEvent()).isNotNull();
            assertThat(child.style().hoverEvent().action()).isEqualTo(HoverEvent.Action.SHOW_TEXT);
        }
    }
    
    @Nested
    @DisplayName("Placeholder Integration")
    class PlaceholderIntegration {
        
        @Test
        @DisplayName("should resolve placeholders")
        void resolvePlaceholders() {
            PlaceholderResolver resolver = ctx -> switch (ctx.key()) {
                case "player" -> Optional.of("Steve");
                case "level" -> Optional.of("42");
                default -> Optional.empty();
            };
            
            Component result = MiniMessage.parse("<green>Welcome, {player}! Level: {level}</green>", resolver);
            
            TextComponent child = (TextComponent) result.children().get(0);
            assertThat(child.content()).contains("Steve");
            assertThat(child.content()).contains("42");
        }
    }
    
    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethods {
        
        @Test
        @DisplayName("should escape tags")
        void escapeTags() {
            String escaped = MiniMessage.escape("<red>Hello</red>");
            assertThat(escaped).isEqualTo("\\<red\\>Hello\\</red\\>");
        }
        
        @Test
        @DisplayName("should unescape tags")
        void unescapeTags() {
            String unescaped = MiniMessage.unescape("\\<red\\>Hello\\</red\\>");
            assertThat(unescaped).isEqualTo("<red>Hello</red>");
        }
        
        @Test
        @DisplayName("should strip tags")
        void stripTags() {
            String plain = MiniMessage.stripTags("<red>Hello</red> <bold>World</bold>!");
            assertThat(plain).isEqualTo("Hello World!");
        }
    }
}
