package com.argonathsystems.framework.text;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the styling of a text component.
 */
public class Style {
    private final @Nullable TextColor color;
    private final @Nullable String font;
    private final @Nullable Boolean bold;
    private final @Nullable Boolean italic;
    private final @Nullable Boolean underlined;
    private final @Nullable Boolean strikethrough;

    private Style(@Nullable TextColor color, @Nullable String font, @Nullable Boolean bold, @Nullable Boolean italic, @Nullable Boolean underlined, @Nullable Boolean strikethrough) {
        this.color = color;
        this.font = font;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Style empty() {
        return new Builder().build();
    }
    
    public static Style of(TextColor color) {
        return new Builder().color(color).build();
    }

    public @Nullable TextColor color() { return color; }
    public @Nullable String font() { return font; }
    public @Nullable Boolean bold() { return bold; }
    public @Nullable Boolean italic() { return italic; }
    public @Nullable Boolean underlined() { return underlined; }
    public @Nullable Boolean strikethrough() { return strikethrough; }

    public Style merge(Style other) {
        return new Builder()
            .color(other.color != null ? other.color : this.color)
            .font(other.font != null ? other.font : this.font)
            .bold(other.bold != null ? other.bold : this.bold)
            .italic(other.italic != null ? other.italic : this.italic)
            .underlined(other.underlined != null ? other.underlined : this.underlined)
            .strikethrough(other.strikethrough != null ? other.strikethrough : this.strikethrough)
            .build();
    }

    public static class Builder {
        private TextColor color;
        private String font;
        private Boolean bold;
        private Boolean italic;
        private Boolean underlined;
        private Boolean strikethrough;

        public Builder color(TextColor color) { this.color = color; return this; }
        public Builder font(String font) { this.font = font; return this; }
        public Builder bold(Boolean bold) { this.bold = bold; return this; }
        public Builder italic(Boolean italic) { this.italic = italic; return this; }
        public Builder underlined(Boolean underlined) { this.underlined = underlined; return this; }
        public Builder strikethrough(Boolean strikethrough) { this.strikethrough = strikethrough; return this; }

        public Style build() {
            return new Style(color, font, bold, italic, underlined, strikethrough);
        }
    }
}
