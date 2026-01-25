# Text Styling Library

> **Platform-agnostic rich text formatting, i18n, and component system**

[![GitHub](https://img.shields.io/badge/GitHub-Argonath--Systems-181717?logo=github)](https://github.com/Argonath-Systems/03-framework-text-styling)
[![Maven](https://img.shields.io/badge/Maven-Central-C71A36?logo=apache-maven)](https://maven.apache.org/)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](../LICENSE)
[![Website](https://img.shields.io/badge/Docs-argonath--systems.github.io-blue)](https://argonath-systems.github.io/00-Argonath-Wiki)

---

## 📋 Overview

Platform-agnostic library for rich text styling, component handling, internationalization (I18N), and serialization.

## Features

### Core Components
- **Component Model**: Tree-based text components (`TextComponent`, `TranslatableComponent`)
- **Style System**: Colors, decorations (bold, italic, underlined, strikethrough, obfuscated), and custom fonts
- **Interactive Events**: Click actions (run command, open URL, copy to clipboard) and hover events (tooltips, item previews)
- **Gradient Support**: Smooth color transitions with preset gradients (rainbow, fire, sunset, etc.)

### Internationalization (I18N)
- **Translation Keys**: Namespaced translation keys with placeholder substitution
- **Locale Support**: Full locale hierarchy with fallback (e.g., `en_US` → `en` → default)
- **Translation Registry**: Load translations from properties files or programmatically
- **Translatable Components**: Components that resolve at render time based on viewer's locale

### Parsing & Formatting
- **MiniMessage Parser**: Adventure-compatible tag syntax (`<red>`, `<bold>`, `<gradient:red:gold>`, etc.)
- **Placeholder System**: Dynamic text substitution with `{placeholder}` or `%placeholder%` syntax
- **JSON Serialization**: Minecraft-compatible JSON text format

### Architecture
This library follows the "Zero Hytale Imports" rule. It is purely logical and data-driven.
Adapters (in `projects/adapters`) are responsible for converting `Component` objects into Hytale-specific formats.

## Installation

```xml
<dependency>
    <groupId>com.argonathsystems.framework</groupId>
    <artifactId>text-styling-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Basic Components

```java
import com.argonathsystems.framework.text.*;

// Simple text
Component message = Component.text("Hello World");

// Styled text
Component styled = Component.text("Important!", TextColor.RED);

// With builder
Component complex = new TextComponent("Click here")
    .style(Style.builder()
        .color(TextColor.GOLD)
        .bold(true)
        .clickAction(ClickAction.runCommand("/help"))
        .hoverEvent(HoverEvent.showText("Click for help"))
        .build());

// Composing components
Component composed = Component.text("Welcome, ")
    .append(Component.text("Player", TextColor.GREEN))
    .append(Component.text("!"));
```

### MiniMessage Parsing

```java
import com.argonathsystems.framework.text.MiniMessage;

// Parse formatted text
Component message = MiniMessage.parse("<red>Hello <bold>World</bold>!</red>");

// Gradients
Component gradient = MiniMessage.parse("<gradient:gold:red>Epic Loot!</gradient>");

// Interactive text
Component clickable = MiniMessage.parse(
    "<click:run_command:/help><hover:show_text:Click for help>Help</hover></click>"
);

// With placeholders
PlaceholderResolver resolver = ctx -> switch (ctx.key()) {
    case "player" -> Optional.of(playerName);
    case "level" -> Optional.of(String.valueOf(level));
    default -> Optional.empty();
};
Component welcome = MiniMessage.parse("<green>Welcome, {player}! Level: {level}</green>", resolver);
```

### Gradients

```java
import com.argonathsystems.framework.text.Gradient;

// Create custom gradient
Gradient sunset = Gradient.of(TextColor.GOLD, TextColor.RED);
TextComponent gradientText = sunset.applyAsComponent("Sunset Text");

// Use preset gradients
TextComponent rainbow = Gradient.RAINBOW.applyAsComponent("Rainbow!");
TextComponent fire = Gradient.FIRE.applyAsComponent("On Fire!");
TextComponent legendary = Gradient.LEGENDARY.applyAsComponent("Legendary Item");
```

### Internationalization (I18N)

```java
import com.argonathsystems.framework.text.i18n.*;
import com.argonathsystems.framework.text.TranslatableComponent;

// Register translations
TranslationRegistry registry = TranslationRegistry.get();
registry.register("core", Locale.ENGLISH, "ui.welcome", "Welcome, {0}!");
registry.register("core", Locale.FRENCH, "ui.welcome", "Bienvenue, {0}!");

// Load from properties file
registry.loadFromProperties("mymod", Locale.ENGLISH, 
    getClass().getResourceAsStream("/lang/en.properties"));

// Create translatable component
Component message = TranslatableComponent.of("ui.welcome", playerName);

// Resolve for a specific locale
TextComponent resolved = ((TranslatableComponent) message).resolve(Locale.FRENCH);
// Result: "Bienvenue, Steve!"
```

### Placeholders

```java
import com.argonathsystems.framework.text.placeholder.*;

// Create resolver
PlaceholderResolver resolver = ctx -> switch (ctx.key()) {
    case "player_name" -> Optional.of(getPlayerName(ctx.viewerId()));
    case "player_level" -> Optional.of(String.valueOf(getLevel(ctx.viewerId())));
    case "server_online" -> Optional.of(String.valueOf(getOnlineCount()));
    default -> Optional.empty();
};

// Process text
String result = PlaceholderProcessor.process(
    "Hello, {player_name}! There are {server_online} players online.",
    resolver
);

// Register in global registry
PlaceholderRegistry.get().register("player", playerResolver);
PlaceholderRegistry.get().register("server", serverResolver);

// Use combined resolver
String text = PlaceholderProcessor.process(input, PlaceholderRegistry.get().combined());
```

### JSON Serialization

```java
import com.argonathsystems.framework.text.serialization.ComponentSerializer;

ComponentSerializer serializer = ComponentSerializer.get();

// Serialize to JSON
Component component = MiniMessage.parse("<red>Hello <bold>World</bold></red>");
String json = serializer.toJson(component);

// Deserialize from JSON
Component parsed = serializer.fromJson(json);

// Compact JSON (no whitespace)
String compact = serializer.toCompactJson(component);
```

## Supported Tags

| Tag | Description | Example |
|-----|-------------|---------|
| `<color>` | Named colors | `<red>`, `<gold>`, `<dark_blue>` |
| `<#RRGGBB>` | Hex colors | `<#FF5500>` |
| `<bold>`, `<b>` | Bold text | `<bold>Strong</bold>` |
| `<italic>`, `<i>` | Italic text | `<italic>Emphasis</italic>` |
| `<underlined>`, `<u>` | Underlined text | `<underlined>Link</underlined>` |
| `<strikethrough>`, `<st>` | Strikethrough | `<st>Deleted</st>` |
| `<obfuscated>`, `<obf>` | Obfuscated | `<obf>Secret</obf>` |
| `<reset>` | Reset all styles | `<red>Red<reset>Normal` |
| `<gradient:c1:c2>` | Gradient | `<gradient:red:gold>Sunset</gradient>` |
| `<click:action:value>` | Click action | `<click:run_command:/help>` |
| `<hover:action:value>` | Hover event | `<hover:show_text:Tooltip>` |

## API Reference

### Core Classes
- `Component` - Base interface for text components
- `TextComponent` - Concrete text with content and style
- `TranslatableComponent` - I18N-aware component
- `Style` - Immutable style container
- `TextColor` - RGB color representation
- `Gradient` - Color gradient with interpolation

### Events
- `ClickAction` - Click interaction (run_command, open_url, etc.)
- `HoverEvent` - Hover interaction (show_text, show_item, show_entity)

### I18N
- `TranslationKey` - Namespaced translation identifier
- `Locale` - Language/country locale
- `TranslationRegistry` - Translation string storage
- `LocaleProvider` - Player locale resolution

### Placeholders
- `PlaceholderResolver` - Resolves placeholder values
- `PlaceholderContext` - Placeholder parsing context
- `PlaceholderProcessor` - String processing utility
- `PlaceholderRegistry` - Global resolver registry

### Parsing & Serialization
- `MiniMessage` - Tag-based text parser
- `TextParser` - Simple legacy parser
- `ComponentSerializer` - JSON serialization

---

## 📚 Documentation

- 🌐 [**Documentation Website**](https://argonath-systems.github.io/00-Argonath-Wiki)
- 📖 [**Text Styling Guide**](https://argonath-systems.github.io/00-Argonath-Wiki/docs/api/text-styling.html)
- 🎨 [**MiniMessage Documentation**](https://docs.advntr.dev/minimessage/)

## 🤝 Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for development guidelines.

## 💬 Community

- 💬 [**Discord**](https://discord.gg/RK3MtpyH) - Chat and support
- 🐛 [**Issues**](https://github.com/orgs/Argonath-Systems/issues) - Bug reports
- 📖 [**Discussions**](https://github.com/orgs/Argonath-Systems/discussions) - Q&A

## 📄 License

MIT License - Copyright © 2025 Argonath Systems. See [LICENSE](../LICENSE) for details.

---

<div align="center">

Part of the [**Argonath Systems**](https://github.com/orgs/Argonath-Systems/) ecosystem

[Documentation](https://argonath-systems.github.io/00-Argonath-Wiki) • [Discord](https://discord.gg/RK3MtpyH) • [GitHub](https://github.com/orgs/Argonath-Systems/)

</div>
