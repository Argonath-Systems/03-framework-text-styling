package com.argonathsystems.framework.text;

import com.argonathsystems.framework.text.event.ClickAction;
import com.argonathsystems.framework.text.event.HoverEvent;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the styling of a text component.
 * Includes colors, decorations, fonts, click actions, and hover events.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Simple style
 * Style bold = Style.builder().bold(true).build();
 * 
 * // Full style with events
 * Style interactive = Style.builder()
 *     .color(TextColor.GOLD)
 *     .bold(true)
 *     .clickAction(ClickAction.runCommand("/accept"))
 *     .hoverEvent(HoverEvent.showText("Click to accept"))
 *     .build();
 * }</pre>
 */
public class Style {
    private final @Nullable TextColor color;
    private final @Nullable String font;
    private final @Nullable Boolean bold;
    private final @Nullable Boolean italic;
    private final @Nullable Boolean underlined;
    private final @Nullable Boolean strikethrough;
    private final @Nullable Boolean obfuscated;
    private final @Nullable ClickAction clickAction;
    private final @Nullable HoverEvent hoverEvent;
    private final @Nullable String insertion;

    private Style(@Nullable TextColor color, @Nullable String font, 
                  @Nullable Boolean bold, @Nullable Boolean italic, 
                  @Nullable Boolean underlined, @Nullable Boolean strikethrough,
                  @Nullable Boolean obfuscated, @Nullable ClickAction clickAction,
                  @Nullable HoverEvent hoverEvent, @Nullable String insertion) {
        this.color = color;
        this.font = font;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
        this.clickAction = clickAction;
        this.hoverEvent = hoverEvent;
        this.insertion = insertion;
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
    public @Nullable Boolean obfuscated() { return obfuscated; }
    public @Nullable ClickAction clickAction() { return clickAction; }
    public @Nullable HoverEvent hoverEvent() { return hoverEvent; }
    public @Nullable String insertion() { return insertion; }
    
    /**
     * Checks if this style has any click or hover interactions.
     */
    public boolean isInteractive() {
        return clickAction != null || hoverEvent != null;
    }
    
    /**
     * Checks if this style has any decorations (bold, italic, etc.)
     */
    public boolean hasDecorations() {
        return bold != null || italic != null || underlined != null 
            || strikethrough != null || obfuscated != null;
    }

    /**
     * Merges this style with another, with the other style taking precedence
     * for any non-null values.
     */
    public Style merge(Style other) {
        return new Builder()
            .color(other.color != null ? other.color : this.color)
            .font(other.font != null ? other.font : this.font)
            .bold(other.bold != null ? other.bold : this.bold)
            .italic(other.italic != null ? other.italic : this.italic)
            .underlined(other.underlined != null ? other.underlined : this.underlined)
            .strikethrough(other.strikethrough != null ? other.strikethrough : this.strikethrough)
            .obfuscated(other.obfuscated != null ? other.obfuscated : this.obfuscated)
            .clickAction(other.clickAction != null ? other.clickAction : this.clickAction)
            .hoverEvent(other.hoverEvent != null ? other.hoverEvent : this.hoverEvent)
            .insertion(other.insertion != null ? other.insertion : this.insertion)
            .build();
    }
    
    /**
     * Creates a new style that is a copy of this one with the given color.
     */
    public Style withColor(TextColor color) {
        return builder().from(this).color(color).build();
    }
    
    /**
     * Creates a new style that is a copy of this one with the given click action.
     */
    public Style withClickAction(ClickAction action) {
        return builder().from(this).clickAction(action).build();
    }
    
    /**
     * Creates a new style that is a copy of this one with the given hover event.
     */
    public Style withHoverEvent(HoverEvent event) {
        return builder().from(this).hoverEvent(event).build();
    }
    
    @Override
    public String toString() {
        return "Style{color=" + color + ", font=" + font + ", bold=" + bold + 
               ", italic=" + italic + ", underlined=" + underlined +
               ", strikethrough=" + strikethrough + ", obfuscated=" + obfuscated +
               ", clickAction=" + clickAction + ", hoverEvent=" + hoverEvent + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Style style = (Style) o;
        return Objects.equals(color, style.color) && Objects.equals(font, style.font) 
            && Objects.equals(bold, style.bold) && Objects.equals(italic, style.italic)
            && Objects.equals(underlined, style.underlined) 
            && Objects.equals(strikethrough, style.strikethrough)
            && Objects.equals(obfuscated, style.obfuscated)
            && Objects.equals(clickAction, style.clickAction)
            && Objects.equals(hoverEvent, style.hoverEvent)
            && Objects.equals(insertion, style.insertion);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(color, font, bold, italic, underlined, strikethrough, 
                           obfuscated, clickAction, hoverEvent, insertion);
    }

    public static class Builder {
        private TextColor color;
        private String font;
        private Boolean bold;
        private Boolean italic;
        private Boolean underlined;
        private Boolean strikethrough;
        private Boolean obfuscated;
        private ClickAction clickAction;
        private HoverEvent hoverEvent;
        private String insertion;
        
        /**
         * Copies all values from an existing style.
         */
        public Builder from(Style style) {
            this.color = style.color;
            this.font = style.font;
            this.bold = style.bold;
            this.italic = style.italic;
            this.underlined = style.underlined;
            this.strikethrough = style.strikethrough;
            this.obfuscated = style.obfuscated;
            this.clickAction = style.clickAction;
            this.hoverEvent = style.hoverEvent;
            this.insertion = style.insertion;
            return this;
        }

        public Builder color(TextColor color) { this.color = color; return this; }
        public Builder font(String font) { this.font = font; return this; }
        public Builder bold(Boolean bold) { this.bold = bold; return this; }
        public Builder italic(Boolean italic) { this.italic = italic; return this; }
        public Builder underlined(Boolean underlined) { this.underlined = underlined; return this; }
        public Builder strikethrough(Boolean strikethrough) { this.strikethrough = strikethrough; return this; }
        public Builder obfuscated(Boolean obfuscated) { this.obfuscated = obfuscated; return this; }
        public Builder clickAction(ClickAction clickAction) { this.clickAction = clickAction; return this; }
        public Builder hoverEvent(HoverEvent hoverEvent) { this.hoverEvent = hoverEvent; return this; }
        public Builder insertion(String insertion) { this.insertion = insertion; return this; }

        public Style build() {
            return new Style(color, font, bold, italic, underlined, strikethrough,
                           obfuscated, clickAction, hoverEvent, insertion);
        }
    }
}
