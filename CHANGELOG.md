# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.1] - 2026-01-29

### Added

#### i18n Completion
- `PluralResolver` - Language-specific plural form resolution (CLDR-based)
  - Support for 6 plural categories: zero, one, two, few, many, other
  - Built-in rules for English, German, French, Russian, Polish, Arabic, and CJK languages
  - `PluralForms` helper record for plural form selection
- `IcuFormatter` - ICU-like message formatting
  - Simple argument substitution: `{name}`
  - Plural selection: `{count, plural, one{# item} other{# items}}`
  - Select (gender/choice): `{gender, select, male{He} female{She} other{They}}`
  - Number formatting: `{price, number, currency}`, `{score, number, percent}`
  - Date formatting: `{date, date, medium}`, `{time, time, short}`
  - Fluent `MessageBuilder` API
- `TranslationArgument` - Sealed interface for type-safe translation arguments
  - Replaces raw `Object` usage in `TranslatableComponent`
  - Variants: `StringArg`, `NumberArg`, `ComponentArg`, `DateArg`, `PluralArg`

#### Theme System
- `Palette` - Semantic color definitions with builder pattern
  - Named colors by meaning (e.g., "rarity.legendary", "primary")
  - `extend()` method for palette inheritance
  - Default palette with standard UI colors
- `Theme` - Complete theme with palette and pre-defined styles
  - `ThemeMetadata` record for author, version, description
  - Builder pattern for fluent construction
- `ThemeRegistry` - Global singleton registry with hot-swap support
  - Register/unregister themes
  - `onThemeChange()` listener support
  - Thread-safe concurrent access
- `ThemeLoader` - JSON theme file loading and saving
  - Load from Path, Reader, or String
  - Save to Path or Writer
  - Full round-trip serialization

#### Serialization
- `PlainTextSerializer` - Strips all formatting, returns plain text
- `LegacySerializer` - §-code format conversion
  - Serialize to legacy format
  - Deserialize from legacy format
  - Configurable color character

#### Rendering/Formatting
- `ComponentVisitor<T>` - Generic visitor pattern for component trees
  - Visit methods for Text, Translatable, Score, and unknown components
- `ScoreComponent` - Dynamic score display component
  - Objective-based score lookups
  - Optional display value
- `TextStyler` - Central facade (per SF-009 L2.2)
  - `parse()` for MiniMessage syntax
  - `style()` for object-to-component conversion
- `ObjectRenderer` - Functional interface for object rendering
- `ObjectAdapterRegistry` - Registry for object-to-component adapters
  - Class hierarchy lookup
  - Default renderers for String, Number, Boolean, Component
- `StyleContext` - Type-safe rendering context
  - Sealed `ContextValue` interface with typed variants

### Changed
- `TextColor` - Added `named()` static method for color lookup by name
  - Added more standard colors: YELLOW, AQUA, DARK_RED, DARK_GREEN, etc.
- `TranslatableComponent` - Now uses type-safe `TranslationArgument` instead of `Object`
- `ComponentSerializer` - Updated to handle new `TranslationArgument` types

### Removed
- ❌ `TextStylingLibPlugin.java` - Removed (violated Zero Hytale Imports architecture rule)

### Fixed
- Removed HytaleServer-parent dependency from pom.xml (platform-agnostic library)
- Version consistency between CHANGELOG (1.0.0) and IMPLEMENTATION_TRACKING

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
