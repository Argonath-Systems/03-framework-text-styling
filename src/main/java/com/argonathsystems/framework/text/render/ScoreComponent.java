package com.argonathsystems.framework.text.render;

import com.argonathsystems.framework.text.Component;
import com.argonathsystems.framework.text.TextComponent;
import com.argonathsystems.framework.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A component that displays a dynamic score/value from an objective.
 * Used for scoreboards, statistics, and other dynamic numeric displays.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Show a player's kills score
 * Component killsDisplay = ScoreComponent.of("PlayerName", "kills");
 * 
 * // With a fallback value
 * Component score = ScoreComponent.of("PlayerName", "points")
 *     .fallback("0");
 * }</pre>
 * 
 * <p>Note: The actual score resolution happens at render time by the platform adapter.
 * This component just holds the reference information.</p>
 */
public class ScoreComponent implements Component {
    
    private final String name;
    private final String objective;
    private String fallback;
    private com.argonathsystems.framework.text.Style style;
    private final java.util.List<Component> children;
    
    private ScoreComponent(@NotNull String name, @NotNull String objective) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.objective = Objects.requireNonNull(objective, "objective cannot be null");
        this.fallback = null;
        this.style = com.argonathsystems.framework.text.Style.empty();
        this.children = new java.util.ArrayList<>();
    }
    
    /**
     * Creates a score component for a player/entity and objective.
     * 
     * @param name The name of the score holder (player name, entity UUID, or "*" for viewer)
     * @param objective The objective name to read from
     * @return A new ScoreComponent
     */
    public static ScoreComponent of(@NotNull String name, @NotNull String objective) {
        return new ScoreComponent(name, objective);
    }
    
    /**
     * Creates a score component that shows the viewer's own score.
     * 
     * @param objective The objective name
     * @return A new ScoreComponent with "*" as the name
     */
    public static ScoreComponent ofViewer(@NotNull String objective) {
        return new ScoreComponent("*", objective);
    }
    
    /**
     * Returns the name of the score holder.
     * 
     * @return The score holder name
     */
    public String name() {
        return name;
    }
    
    /**
     * Returns the objective name.
     * 
     * @return The objective name
     */
    public String objective() {
        return objective;
    }
    
    /**
     * Sets a fallback value to display if the score cannot be resolved.
     * 
     * @param fallback The fallback value
     * @return This component for chaining
     */
    public ScoreComponent fallback(String fallback) {
        this.fallback = fallback;
        return this;
    }
    
    /**
     * Returns the fallback value.
     * 
     * @return The fallback value, or null if not set
     */
    public String fallback() {
        return fallback;
    }
    
    /**
     * Checks if the score is for the viewing player.
     * 
     * @return true if name is "*"
     */
    public boolean isViewerScore() {
        return "*".equals(name);
    }
    
    @Override
    public @NotNull com.argonathsystems.framework.text.Style style() {
        return style;
    }
    
    /**
     * Sets the style for this component.
     * 
     * @param style The style to apply
     * @return This component for chaining
     */
    public ScoreComponent style(com.argonathsystems.framework.text.Style style) {
        this.style = Objects.requireNonNull(style, "style cannot be null");
        return this;
    }
    
    @Override
    public @NotNull java.util.List<Component> children() {
        return children;
    }
    
    @Override
    public String toString() {
        return "ScoreComponent{name='" + name + "', objective='" + objective + 
               "', fallback=" + fallback + ", style=" + style + "}";
    }
}
