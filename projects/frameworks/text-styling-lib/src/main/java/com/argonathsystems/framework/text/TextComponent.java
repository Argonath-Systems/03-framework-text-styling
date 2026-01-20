package com.argonathsystems.framework.text;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class TextComponent implements Component {
    
    private final String content;
    private Style style;
    private final List<Component> children;

    public TextComponent(String content) {
        this.content = content;
        this.style = Style.empty();
        this.children = new ArrayList<>();
    }

    public String content() {
        return content;
    }

    @Override
    public @NotNull Style style() {
        return style;
    }
    
    public TextComponent style(Style style) {
        this.style = style;
        return this;
    }

    @Override
    public @NotNull List<Component> children() {
        return children;
    }
    
    @Override
    public String toString() {
        return "TextComponent{content='" + content + "', style=" + style + ", children=" + children + "}";
    }
}
