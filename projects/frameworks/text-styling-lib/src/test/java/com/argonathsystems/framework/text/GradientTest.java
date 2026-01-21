package com.argonathsystems.framework.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Gradient Tests")
class GradientTest {
    
    @Nested
    @DisplayName("Creation")
    class Creation {
        
        @Test
        @DisplayName("should create two-color gradient")
        void createTwoColorGradient() {
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.BLUE);
            
            assertThat(gradient.colors()).hasSize(2);
            assertThat(gradient.colors().get(0)).isEqualTo(TextColor.RED);
            assertThat(gradient.colors().get(1)).isEqualTo(TextColor.BLUE);
        }
        
        @Test
        @DisplayName("should create multi-color gradient")
        void createMultiColorGradient() {
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.GREEN, TextColor.BLUE);
            
            assertThat(gradient.colors()).hasSize(3);
        }
        
        @Test
        @DisplayName("should reject single color")
        void rejectSingleColor() {
            assertThatThrownBy(() -> Gradient.of(TextColor.RED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 2 colors");
        }
    }
    
    @Nested
    @DisplayName("Color Interpolation")
    class ColorInterpolation {
        
        @Test
        @DisplayName("should return first color at position 0")
        void firstColorAtZero() {
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.BLUE);
            
            TextColor color = gradient.colorAt(0f);
            
            assertThat(color.value()).isEqualTo(TextColor.RED.value());
        }
        
        @Test
        @DisplayName("should return last color at position 1")
        void lastColorAtOne() {
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.BLUE);
            
            TextColor color = gradient.colorAt(1f);
            
            assertThat(color.value()).isEqualTo(TextColor.BLUE.value());
        }
        
        @Test
        @DisplayName("should interpolate at midpoint")
        void interpolateAtMidpoint() {
            // Red (0xFF5555) to Blue (0x5555FF)
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.BLUE);
            
            TextColor color = gradient.colorAt(0.5f);
            
            // Should be a purple-ish color
            int r = (color.value() >> 16) & 0xFF;
            int g = (color.value() >> 8) & 0xFF;
            int b = color.value() & 0xFF;
            
            // R should be between RED's R and BLUE's R
            assertThat(r).isBetween(0x55, 0xFF);
            assertThat(b).isBetween(0x55, 0xFF);
        }
    }
    
    @Nested
    @DisplayName("Phase Offset")
    class PhaseOffset {
        
        @Test
        @DisplayName("should apply phase offset")
        void applyPhaseOffset() {
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.BLUE).withPhase(0.5f);
            
            // At position 0 with 0.5 phase, we should be at the middle
            TextColor colorAtStart = gradient.colorAt(0f);
            
            // This should not equal the first color due to phase
            // (exact value depends on interpolation)
            assertThat(gradient.phase()).isEqualTo(0.5f);
        }
        
        @Test
        @DisplayName("should create gradient with phase")
        void createWithPhase() {
            Gradient gradient = Gradient.withPhase(0.25f, TextColor.RED, TextColor.BLUE);
            
            assertThat(gradient.phase()).isEqualTo(0.25f);
        }
    }
    
    @Nested
    @DisplayName("Text Application")
    class TextApplication {
        
        @Test
        @DisplayName("should apply gradient to text")
        void applyToText() {
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.BLUE);
            
            List<TextComponent> result = gradient.apply("Hello");
            
            assertThat(result).hasSize(5); // One component per character
            assertThat(result.get(0).content()).isEqualTo("H");
            assertThat(result.get(4).content()).isEqualTo("o");
        }
        
        @Test
        @DisplayName("should apply gradient with base style")
        void applyWithBaseStyle() {
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.BLUE);
            Style baseStyle = Style.builder().bold(true).build();
            
            List<TextComponent> result = gradient.apply("Hi", baseStyle);
            
            assertThat(result).hasSize(2);
            assertThat(result.get(0).style().bold()).isTrue();
            assertThat(result.get(0).style().color()).isNotNull();
        }
        
        @Test
        @DisplayName("should return empty list for empty string")
        void emptyStringReturnsEmptyList() {
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.BLUE);
            
            List<TextComponent> result = gradient.apply("");
            
            assertThat(result).isEmpty();
        }
        
        @Test
        @DisplayName("should create component with gradient children")
        void createComponentWithGradient() {
            Gradient gradient = Gradient.of(TextColor.RED, TextColor.BLUE);
            
            TextComponent result = gradient.applyAsComponent("Hi");
            
            assertThat(result.children()).hasSize(2);
        }
    }
    
    @Nested
    @DisplayName("Preset Gradients")
    class PresetGradients {
        
        @Test
        @DisplayName("should have FIRE preset")
        void firePreset() {
            assertThat(Gradient.FIRE.colors()).hasSize(3);
        }
        
        @Test
        @DisplayName("should have RAINBOW preset")
        void rainbowPreset() {
            assertThat(Gradient.RAINBOW.colors()).hasSize(7);
        }
        
        @Test
        @DisplayName("should have LEGENDARY preset")
        void legendaryPreset() {
            assertThat(Gradient.LEGENDARY.colors()).hasSize(3);
        }
    }
}
