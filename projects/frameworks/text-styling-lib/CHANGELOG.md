# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-01-21

### Added

#### Core Components
- `Component` interface with factory methods for text creation
- `TextComponent` for concrete text with content and style
- `Style` class with full support for colors, decorations, fonts, click actions, and hover events
- `TextColor` record with named colors and hex support
- `StyleRegistry` for reusable style management

#### Internationalization (I18N)
- `TranslationKey` - Namespaced translation key with parsing support
- `Locale` - Locale representation with parent fallback and Java interop
- `TranslationRegistry` - Thread-safe translation storage with properties file loading
- `LocaleProvider` - Interface for player locale resolution
- `TranslatableComponent` - Component that resolves at render time based on locale

#### Interactive Events
- `ClickAction` - Click interactions (RUN_COMMAND, SUGGEST_COMMAND, OPEN_URL, COPY_TO_CLIPBOARD, CHANGE_PAGE)
- `HoverEvent` - Hover interactions (SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY)
- Sealed interface pattern for type-safe event handling

#### Gradient Support
- `Gradient` class with color interpolation
- Multi-color gradient support
- Phase offset for animated effects
- Preset gradients: FIRE, OCEAN, SUNSET, NATURE, RAINBOW, LEGENDARY, EPIC
- Text application methods for gradient coloring

#### Placeholder System
- `PlaceholderResolver` - Functional interface for placeholder resolution
- `PlaceholderContext` - Context with key, params, and viewer ID
- `PlaceholderProcessor` - String processing with `{placeholder}` and `%placeholder%` syntax
- `PlaceholderRegistry` - Global registry with prefix-based routing

#### Parsing
- `MiniMessage` - Full MiniMessage-style parser with:
  - Named colors (`<red>`, `<gold>`, etc.)
  - Hex colors (`<#FF5500>`)
  - Decorations (`<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`, `<obfuscated>`)
  - Gradients (`<gradient:red:gold>text</gradient>`)
  - Click actions (`<click:run_command:/help>`)
  - Hover events (`<hover:show_text:Tooltip>`)
  - Closing tags (`</red>`, `</bold>`)
  - Placeholder integration
- `TextParser` - Simple legacy parser for basic tags

#### Serialization
- `ComponentSerializer` - JSON serialization compatible with Minecraft raw JSON format
- Support for all component types, styles, and events
- Round-trip serialization/deserialization
- Compact and pretty-printed output options

### Tests
- Comprehensive unit tests for all modules:
  - `TranslationSystemTest` - I18N functionality
  - `MiniMessageTest` - Parser functionality
  - `GradientTest` - Gradient interpolation and application
  - `PlaceholderSystemTest` - Placeholder processing
  - `ComponentSerializerTest` - JSON serialization

## [0.1.0] - 2026-01-15

### Added
- Initial project structure
- `Component` model and `TextComponent` implementation
- `Style` and `TextColor` records
- `StyleRegistry` for managing styles
- `TextParser` for parsing simple tag-based strings
