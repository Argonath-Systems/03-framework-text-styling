package com.argonathsystems.framework.text;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface Component {
    
    @NotNull Style style();
    
    @NotNull List<Component> children();
    
    default Component append(Component child) {
        children().add(child);
        return this;
    }
    
    static TextComponent text(String content) {
        return new TextComponent(content);
    }
    
    static TextComponent text(String content, Style style) {
        return new TextComponent(content).style(style);
    }
    
    static TextComponent text(String content, TextColor color) {
        return new TextComponent(content).style(Style.of(color));
    }
    
    static Component empty() {
        return new TextComponent("");
    }
}
