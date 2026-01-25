package com.argonathsystems.framework.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a color gradient that transitions between multiple colors.
 * Used for creating smooth color transitions in text.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Simple two-color gradient
 * Gradient sunset = Gradient.of(TextColor.GOLD, TextColor.RED);
 * 
 * // Multi-color gradient
 * Gradient rainbow = Gradient.of(
 *     TextColor.RED, TextColor.GOLD, TextColor.GREEN, TextColor.BLUE
 * );
 * 
 * // Get interpolated color at position
 * TextColor midColor = sunset.colorAt(0.5f); // Halfway between gold and red
 * }</pre>
 */
public class Gradient {
    
    private final List<TextColor> colors;
    private final float phase;
    
    private Gradient(@NotNull List<TextColor> colors, float phase) {
        if (colors.size() < 2) {
            throw new IllegalArgumentException("Gradient requires at least 2 colors");
        }
        this.colors = List.copyOf(colors);
        this.phase = phase;
    }
    
    /**
     * Creates a gradient between two or more colors.
     * 
     * @param colors The colors to transition between (minimum 2)
     * @return A new Gradient
     */
    public static Gradient of(@NotNull TextColor... colors) {
        return new Gradient(List.of(colors), 0f);
    }
    
    /**
     * Creates a gradient from a list of colors.
     * 
     * @param colors The colors to transition between
     * @return A new Gradient
     */
    public static Gradient of(@NotNull List<TextColor> colors) {
        return new Gradient(colors, 0f);
    }
    
    /**
     * Creates a gradient with a phase offset.
     * 
     * @param phase The phase offset (0.0 to 1.0)
     * @param colors The colors to transition between
     * @return A new Gradient
     */
    public static Gradient withPhase(float phase, @NotNull TextColor... colors) {
        return new Gradient(List.of(colors), phase);
    }
    
    /**
     * Returns the colors in this gradient.
     */
    public List<TextColor> colors() {
        return colors;
    }
    
    /**
     * Returns the phase offset.
     */
    public float phase() {
        return phase;
    }
    
    /**
     * Creates a new gradient with the given phase offset.
     * 
     * @param phase The phase offset (0.0 to 1.0)
     * @return A new Gradient with the phase applied
     */
    public Gradient withPhase(float phase) {
        return new Gradient(colors, phase);
    }
    
    /**
     * Gets the interpolated color at a given position.
     * 
     * @param position Position in the gradient (0.0 = start, 1.0 = end)
     * @return The interpolated color
     */
    public TextColor colorAt(float position) {
        // Handle edge cases before applying phase
        if (phase == 0f) {
            if (position <= 0f) return colors.getFirst();
            if (position >= 1f) return colors.getLast();
        }
        
        // Apply phase offset
        position = (position + phase) % 1.0f;
        if (position < 0) position += 1.0f;
        
        if (position <= 0f) return colors.getFirst();
        if (position >= 1f) return colors.getLast();
        
        // Find the two colors to interpolate between
        float scaledPosition = position * (colors.size() - 1);
        int lowerIndex = (int) scaledPosition;
        int upperIndex = Math.min(lowerIndex + 1, colors.size() - 1);
        float localPosition = scaledPosition - lowerIndex;
        
        return interpolate(colors.get(lowerIndex), colors.get(upperIndex), localPosition);
    }
    
    /**
     * Applies this gradient to a string, returning a list of styled components
     * where each character has a different color.
     * 
     * @param text The text to apply the gradient to
     * @return List of single-character TextComponents with gradient colors
     */
    public List<TextComponent> apply(@NotNull String text) {
        return apply(text, Style.empty());
    }
    
    /**
     * Applies this gradient to a string with a base style.
     * 
     * @param text The text to apply the gradient to
     * @param baseStyle The base style to apply (gradient colors will override color)
     * @return List of single-character TextComponents with gradient colors
     */
    public List<TextComponent> apply(@NotNull String text, @NotNull Style baseStyle) {
        Objects.requireNonNull(text, "text cannot be null");
        
        if (text.isEmpty()) {
            return List.of();
        }
        
        List<TextComponent> result = new ArrayList<>(text.length());
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            float position = length == 1 ? 0f : (float) i / (length - 1);
            TextColor color = colorAt(position);
            Style charStyle = baseStyle.merge(Style.of(color));
            result.add(new TextComponent(String.valueOf(text.charAt(i))).style(charStyle));
        }
        
        return result;
    }
    
    /**
     * Creates a single component containing the gradient text as children.
     * 
     * @param text The text to apply the gradient to
     * @return A TextComponent with gradient-colored children
     */
    public TextComponent applyAsComponent(@NotNull String text) {
        TextComponent root = new TextComponent("");
        apply(text).forEach(root::append);
        return root;
    }
    
    /**
     * Linearly interpolates between two colors.
     */
    private static TextColor interpolate(TextColor color1, TextColor color2, float ratio) {
        int r1 = (color1.value() >> 16) & 0xFF;
        int g1 = (color1.value() >> 8) & 0xFF;
        int b1 = color1.value() & 0xFF;
        
        int r2 = (color2.value() >> 16) & 0xFF;
        int g2 = (color2.value() >> 8) & 0xFF;
        int b2 = color2.value() & 0xFF;
        
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        
        return TextColor.of((r << 16) | (g << 8) | b);
    }
    
    // ==================== Preset Gradients ====================
    
    /** Red to Orange to Yellow */
    public static final Gradient FIRE = Gradient.of(
        TextColor.of(0xFF3300), TextColor.of(0xFF9900), TextColor.of(0xFFCC00)
    );
    
    /** Light blue to dark blue */
    public static final Gradient OCEAN = Gradient.of(
        TextColor.of(0x00CCFF), TextColor.of(0x0066CC), TextColor.of(0x003366)
    );
    
    /** Purple to pink to light pink */
    public static final Gradient SUNSET = Gradient.of(
        TextColor.of(0xFF6B6B), TextColor.of(0xC44569), TextColor.of(0x6B5B95)
    );
    
    /** Green to yellow-green to yellow */
    public static final Gradient NATURE = Gradient.of(
        TextColor.of(0x228B22), TextColor.of(0x7CFC00), TextColor.of(0xADFF2F)
    );
    
    /** Full rainbow spectrum */
    public static final Gradient RAINBOW = Gradient.of(
        TextColor.of(0xFF0000), // Red
        TextColor.of(0xFF7F00), // Orange
        TextColor.of(0xFFFF00), // Yellow
        TextColor.of(0x00FF00), // Green
        TextColor.of(0x0000FF), // Blue
        TextColor.of(0x4B0082), // Indigo
        TextColor.of(0x9400D3)  // Violet
    );
    
    /** Gold to white for legendary items */
    public static final Gradient LEGENDARY = Gradient.of(
        TextColor.of(0xFFAA00), TextColor.of(0xFFDD44), TextColor.of(0xFFFFAA)
    );
    
    /** Dark purple to light purple for epic items */
    public static final Gradient EPIC = Gradient.of(
        TextColor.of(0x6600CC), TextColor.of(0x9933FF), TextColor.of(0xCC99FF)
    );
    
    @Override
    public String toString() {
        return "Gradient{colors=" + colors + ", phase=" + phase + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gradient gradient = (Gradient) o;
        return Float.compare(phase, gradient.phase) == 0 && colors.equals(gradient.colors);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(colors, phase);
    }
}
