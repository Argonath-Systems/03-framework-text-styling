package com.argonathsystems.framework.text.render;

import com.argonathsystems.framework.text.Component;
import com.argonathsystems.framework.text.TextComponent;
import com.argonathsystems.framework.text.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Visitor interface for processing Component trees.
 * Allows external platforms to consume and render components
 * without knowing their internal structure.
 * 
 * <h2>Usage:</h2>
 * <pre>{@code
 * // Implement a custom visitor
 * class MyRenderer implements ComponentVisitor<String> {
 *     @Override
 *     public String visitText(TextComponent component) {
 *         return component.content();
 *     }
 *     
 *     @Override
 *     public String visitTranslatable(TranslatableComponent component) {
 *         return "[" + component.key().key() + "]";
 *     }
 *     
 *     @Override
 *     public String visitUnknown(Component component) {
 *         return "";
 *     }
 * }
 * 
 * // Use the visitor
 * String result = ComponentVisitor.visit(component, new MyRenderer());
 * }</pre>
 * 
 * @param <R> The return type of the visitor operations
 */
public interface ComponentVisitor<R> {
    
    /**
     * Visits a TextComponent.
     * 
     * @param component The text component to visit
     * @return The result of visiting this component
     */
    R visitText(@NotNull TextComponent component);
    
    /**
     * Visits a TranslatableComponent.
     * 
     * @param component The translatable component to visit
     * @return The result of visiting this component
     */
    R visitTranslatable(@NotNull TranslatableComponent component);
    
    /**
     * Visits a ScoreComponent.
     * 
     * @param component The score component to visit
     * @return The result of visiting this component
     */
    default R visitScore(@NotNull ScoreComponent component) {
        return visitUnknown(component);
    }
    
    /**
     * Visits an unknown component type.
     * Called when the component type is not recognized by this visitor.
     * 
     * @param component The unknown component
     * @return The result of visiting this component
     */
    R visitUnknown(@NotNull Component component);
    
    /**
     * Dispatches a component to the appropriate visit method.
     * 
     * @param component The component to visit
     * @param visitor The visitor to use
     * @param <R> The return type
     * @return The result from the visitor
     */
    static <R> R visit(@NotNull Component component, @NotNull ComponentVisitor<R> visitor) {
        if (component instanceof TextComponent tc) {
            return visitor.visitText(tc);
        } else if (component instanceof TranslatableComponent trc) {
            return visitor.visitTranslatable(trc);
        } else if (component instanceof ScoreComponent sc) {
            return visitor.visitScore(sc);
        } else {
            return visitor.visitUnknown(component);
        }
    }
    
    /**
     * Visits all children of a component recursively.
     * 
     * @param component The root component
     * @param visitor The visitor to use
     * @param combiner Function to combine child results
     * @param <R> The return type
     * @return Combined result from all children
     */
    static <R> R visitChildren(@NotNull Component component, 
                                @NotNull ComponentVisitor<R> visitor,
                                @NotNull ResultCombiner<R> combiner) {
        R result = visit(component, visitor);
        for (Component child : component.children()) {
            R childResult = visitChildren(child, visitor, combiner);
            result = combiner.combine(result, childResult);
        }
        return result;
    }
    
    /**
     * Functional interface for combining visitor results.
     * 
     * @param <R> The result type
     */
    @FunctionalInterface
    interface ResultCombiner<R> {
        R combine(R a, R b);
    }
}
