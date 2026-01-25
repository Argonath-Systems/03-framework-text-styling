package com.argonathsystems.framework.text.serialization;

import com.argonathsystems.framework.text.*;
import com.argonathsystems.framework.text.event.ClickAction;
import com.argonathsystems.framework.text.event.HoverEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ComponentSerializer Tests")
class ComponentSerializerTest {
    
    private ComponentSerializer serializer;
    
    @BeforeEach
    void setUp() {
        serializer = ComponentSerializer.get();
    }
    
    @Nested
    @DisplayName("Serialization")
    class Serialization {
        
        @Test
        @DisplayName("should serialize simple text")
        void serializeSimpleText() {
            Component component = Component.text("Hello World");
            
            String json = serializer.toJson(component);
            
            assertThat(json).contains("\"text\"");
            assertThat(json).contains("Hello World");
        }
        
        @Test
        @DisplayName("should serialize with color")
        void serializeWithColor() {
            Component component = Component.text("Red", TextColor.RED);
            
            String json = serializer.toJson(component);
            
            assertThat(json).contains("\"color\"");
            assertThat(json).contains("red");
        }
        
        @Test
        @DisplayName("should serialize with decorations")
        void serializeWithDecorations() {
            Component component = new TextComponent("Bold");
            ((TextComponent) component).style(Style.builder().bold(true).italic(true).build());
            
            String json = serializer.toJson(component);
            
            assertThat(json).contains("\"bold\"");
            assertThat(json).contains("true");
            assertThat(json).contains("\"italic\"");
        }
        
        @Test
        @DisplayName("should serialize click action")
        void serializeClickAction() {
            Style style = Style.builder()
                .clickAction(ClickAction.runCommand("/help"))
                .build();
            TextComponent component = new TextComponent("Click");
            component.style(style);
            
            String json = serializer.toJson(component);
            
            assertThat(json).contains("\"clickEvent\"");
            assertThat(json).contains("\"action\"");
            assertThat(json).contains("run_command");
            assertThat(json).contains("/help");
        }
        
        @Test
        @DisplayName("should serialize hover event")
        void serializeHoverEvent() {
            Style style = Style.builder()
                .hoverEvent(HoverEvent.showText("Tooltip"))
                .build();
            TextComponent component = new TextComponent("Hover");
            component.style(style);
            
            String json = serializer.toJson(component);
            
            assertThat(json).contains("\"hoverEvent\"");
            assertThat(json).contains("show_text");
        }
        
        @Test
        @DisplayName("should serialize children")
        void serializeChildren() {
            Component parent = Component.text("Parent ")
                .append(Component.text("Child", TextColor.GREEN));
            
            String json = serializer.toJson(parent);
            
            assertThat(json).contains("\"extra\"");
            assertThat(json).contains("Child");
        }
        
        @Test
        @DisplayName("should produce compact JSON")
        void produceCompactJson() {
            Component component = Component.text("Test");
            
            String compact = serializer.toCompactJson(component);
            
            assertThat(compact).doesNotContain("\n");
        }
    }
    
    @Nested
    @DisplayName("Deserialization")
    class Deserialization {
        
        @Test
        @DisplayName("should deserialize simple text")
        void deserializeSimpleText() {
            String json = """
                {"text": "Hello"}
                """;
            
            Component component = serializer.fromJson(json);
            
            assertThat(component).isInstanceOf(TextComponent.class);
            assertThat(((TextComponent) component).content()).isEqualTo("Hello");
        }
        
        @Test
        @DisplayName("should deserialize string shorthand")
        void deserializeStringShorthand() {
            String json = "\"Just a string\"";
            
            Component component = serializer.fromJson(json);
            
            assertThat(component).isInstanceOf(TextComponent.class);
            assertThat(((TextComponent) component).content()).isEqualTo("Just a string");
        }
        
        @Test
        @DisplayName("should deserialize with color")
        void deserializeWithColor() {
            String json = """
                {"text": "Blue", "color": "blue"}
                """;
            
            Component component = serializer.fromJson(json);
            
            assertThat(component.style().color()).isEqualTo(TextColor.BLUE);
        }
        
        @Test
        @DisplayName("should deserialize hex color")
        void deserializeHexColor() {
            String json = """
                {"text": "Custom", "color": "#FF5500"}
                """;
            
            Component component = serializer.fromJson(json);
            
            assertThat(component.style().color().value()).isEqualTo(0xFF5500);
        }
        
        @Test
        @DisplayName("should deserialize decorations")
        void deserializeDecorations() {
            String json = """
                {"text": "Fancy", "bold": true, "italic": true, "underlined": true}
                """;
            
            Component component = serializer.fromJson(json);
            
            assertThat(component.style().bold()).isTrue();
            assertThat(component.style().italic()).isTrue();
            assertThat(component.style().underlined()).isTrue();
        }
        
        @Test
        @DisplayName("should deserialize click event")
        void deserializeClickEvent() {
            String json = """
                {
                    "text": "Click",
                    "clickEvent": {
                        "action": "run_command",
                        "value": "/help"
                    }
                }
                """;
            
            Component component = serializer.fromJson(json);
            
            assertThat(component.style().clickAction()).isNotNull();
            assertThat(component.style().clickAction().action())
                .isEqualTo(ClickAction.Action.RUN_COMMAND);
            assertThat(component.style().clickAction().value()).isEqualTo("/help");
        }
        
        @Test
        @DisplayName("should deserialize hover event")
        void deserializeHoverEvent() {
            String json = """
                {
                    "text": "Hover",
                    "hoverEvent": {
                        "action": "show_text",
                        "contents": "Tooltip text"
                    }
                }
                """;
            
            Component component = serializer.fromJson(json);
            
            assertThat(component.style().hoverEvent()).isNotNull();
            assertThat(component.style().hoverEvent().action())
                .isEqualTo(HoverEvent.Action.SHOW_TEXT);
        }
        
        @Test
        @DisplayName("should deserialize children")
        void deserializeChildren() {
            String json = """
                {
                    "text": "Parent",
                    "extra": [
                        {"text": " Child1"},
                        {"text": " Child2", "color": "red"}
                    ]
                }
                """;
            
            Component component = serializer.fromJson(json);
            
            assertThat(component.children()).hasSize(2);
        }
        
        @Test
        @DisplayName("should deserialize array of components")
        void deserializeArray() {
            String json = """
                [
                    {"text": "First"},
                    {"text": "Second"}
                ]
                """;
            
            Component component = serializer.fromJson(json);
            
            assertThat(component.children()).hasSize(2);
        }
    }
    
    @Nested
    @DisplayName("Round Trip")
    class RoundTrip {
        
        @Test
        @DisplayName("should round-trip simple component")
        void roundTripSimple() {
            Component original = Component.text("Test");
            
            String json = serializer.toJson(original);
            Component restored = serializer.fromJson(json);
            
            assertThat(((TextComponent) restored).content()).isEqualTo("Test");
        }
        
        @Test
        @DisplayName("should round-trip styled component")
        void roundTripStyled() {
            Style style = Style.builder()
                .color(TextColor.GOLD)
                .bold(true)
                .clickAction(ClickAction.openUrl("https://example.com"))
                .build();
            TextComponent original = new TextComponent("Styled");
            original.style(style);
            
            String json = serializer.toJson(original);
            Component restored = serializer.fromJson(json);
            
            assertThat(restored.style().color()).isEqualTo(TextColor.GOLD);
            assertThat(restored.style().bold()).isTrue();
            assertThat(restored.style().clickAction()).isNotNull();
        }
    }
}
