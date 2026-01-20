# Text Styling Library

Platform-agnostic library for rich text styling, component handling, and serialization.

## Features
*   **Component Model**: Tree-based text components (`TextComponent`).
*   **Style System**: Colors, decorations (bold, italic, etc.), and custom fonts.
*   **Parsing**: Simple tag-based parser (e.g., `<red>Hello <bold>World`).
*   **Registry**: Centralized style management via `StyleRegistry`.

## Architecture
This library follows the "Zero Hytale Imports" rule. It is purely logical and data-driven.
Adapters (in `projects/adapters`) are responsible for converting `Component` objects into Hytale-specific formats (NBT, JSON, etc.).

## Usage
```java
// Create a component
Component message = Component.text("Hello ", TextColor.RED)
    .append(Component.text("World", Style.builder().bold(true).build()));

// Parse from string
Component parsed = TextParser.parse("<red>Hello <bold>World");
```
