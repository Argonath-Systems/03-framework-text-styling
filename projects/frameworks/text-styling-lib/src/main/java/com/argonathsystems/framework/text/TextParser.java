package com.argonathsystems.framework.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple parser for converting string formatting to Components.
 * Supports:
 * - <color> (e.g. <red>, <blue>, <#FFAA00>)
 * - <bold>, <italic>, <underlined>
 */
public class TextParser {

    private static final Pattern TAG_PATTERN = Pattern.compile("<(#?[a-zA-Z0-9]+)>");

    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        TextComponent root = Component.text("");
        TextComponent current = root;
        
        // Very naive parser for Phase 1 MVP
        // Splits by tags and appends children with style
        
        Matcher matcher = TAG_PATTERN.matcher(input);
        int lastEnd = 0;
        Style currentStyle = Style.empty();

        while (matcher.find()) {
            String textPart = input.substring(lastEnd, matcher.start());
            if (!textPart.isEmpty()) {
                current.append(Component.text(textPart, currentStyle));
            }

            String tag = matcher.group(1);
            currentStyle = updateStyle(currentStyle, tag);
            
            lastEnd = matcher.end();
        }

        if (lastEnd < input.length()) {
            current.append(Component.text(input.substring(lastEnd), currentStyle));
        }

        return root;
    }

    private static Style updateStyle(Style style, String tag) {
        // Reset
        if (tag.equals("reset")) {
            return Style.empty();
        }
        
        // Booleans
        if (tag.equals("bold") || tag.equals("b")) return style.merge(Style.builder().bold(true).build());
        if (tag.equals("italic") || tag.equals("i")) return style.merge(Style.builder().italic(true).build());
        if (tag.equals("underlined") || tag.equals("u")) return style.merge(Style.builder().underlined(true).build());
        
        // Colors
        TextColor color = null;
        if (tag.startsWith("#")) {
            try {
                color = TextColor.fromHex(tag);
            } catch (Exception ignored) {}
        } else {
            // Check standard colors
             try {
                // simple reflection or map check would be better, but hardcoding for MVP
                if (tag.equalsIgnoreCase("red")) color = TextColor.RED;
                else if (tag.equalsIgnoreCase("blue")) color = TextColor.BLUE;
                else if (tag.equalsIgnoreCase("green")) color = TextColor.GREEN;
                else if (tag.equalsIgnoreCase("white")) color = TextColor.WHITE;
                else if (tag.equalsIgnoreCase("black")) color = TextColor.BLACK;
                else if (tag.equalsIgnoreCase("gold")) color = TextColor.GOLD;
                else if (tag.equalsIgnoreCase("gray")) color = TextColor.GRAY;
            } catch (Exception ignored) {}
        }
        
        if (color != null) {
            return style.merge(Style.of(color));
        }

        return style;
    }
}
