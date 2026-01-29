# Implementation Plan: Text Styling Library

**Module**: `03-framework-text-styling`  
**Generated**: 2026-01-29  
**Architect**: HytaleArchitect  
**Specification Coverage**: SF-ARCHITECTURE-009

---

## Executive Summary

The Text Styling Library is a platform-agnostic framework for rich text formatting, i18n, and component handling. Currently at approximately 70% implementation with **critical violations** including a Hytale API leak in the plugin file (violates "Zero Hytale Imports" rule) and build failures. The module is missing key spec features including the `TextStyler` interface, Object Adapter Registry, and Theme/Palette system.

---

## Critical Issues Found

### Violations (MUST FIX)

| ID | Location | Type | Description | Severity |
|----|----------|------|-------------|----------|
| V-001 | [TextStylingLibPlugin.java#L3-L4](src/main/java/com/argonathsystems/framework/text/TextStylingLibPlugin.java#L3-L4) | Hytale Import Leak | Imports `com.hypixel.hytale.server.core.plugin.JavaPlugin` and `JavaPluginInit` in a standalone library that claims "Zero Hytale Imports" | 🔴 CRITICAL |
| V-002 | [TextStylingLibPlugin.java](src/main/java/com/argonathsystems/framework/text/TextStylingLibPlugin.java) | Compilation Failure | Plugin file causes build failures - cannot find symbol errors for `getLogger()`, `setup()`, `start()`, `stop()` | 🔴 CRITICAL |
| V-003 | [pom.xml#L35-L38](pom.xml#L35-L38) | Dependency Violation | HytaleServer-parent dependency contradicts "Zero platform dependencies allowed" message in enforcer plugin | 🔴 CRITICAL |
| V-004 | CHANGELOG.md vs IMPLEMENTATION_TRACKING.md | Version Mismatch | CHANGELOG declares v1.0.0 released, IMPLEMENTATION_TRACKING says v0.8.0 | 🟡 MEDIUM |

### Technical Debt

| ID | Location | Type | Description | Priority |
|----|----------|------|-------------|----------|
| TD-001 | [TranslatableComponent.java#L33](src/main/java/com/argonathsystems/framework/text/TranslatableComponent.java#L33) | Raw Object Type | `List<Object> arguments` uses raw Object type for translation arguments | 🟡 MEDIUM |
| TD-002 | [StyleRegistry.java](src/main/java/com/argonathsystems/framework/text/StyleRegistry.java) | Incomplete Registry | Missing generic Object Adapter pattern per spec L2.3 | 🟡 MEDIUM |
| TD-003 | Spec L2.4 | Missing Feature | No Theme/Palette system implemented | 🟡 MEDIUM |
| TD-004 | Spec L3.4 | Missing Serializers | Missing `PlainTextSerializer` and `LegacySerializer` | 🟢 LOW |

### TODO/FIXME/STUB Inventory

| Location | Type | Description | Action Required |
|----------|------|-------------|-----------------|
| - | - | ✅ No TODO/FIXME/STUB comments found | None |

---

## Requirements Traceability

### Specification Coverage

| Spec ID | Requirement | Status | Implementation Location | Notes |
|---------|-------------|--------|-------------------------|-------|
| SF-009 L1.2 | Platform Independence | 🚧 PARTIAL | Module structure | Plugin file violates this |
| SF-009 L1.2 | Unified Theming | ❌ NOT IMPL | - | No Theme/Palette system |
| SF-009 L1.2 | Context-Aware Object Formatting | ❌ NOT IMPL | - | No `TextStyler` or Object Adapters |
| SF-009 L1.2 | Localization Ready | ✅ COMPLETE | `i18n/` package | TranslationRegistry, Locale, TranslationKey |
| SF-009 L2.1 | Text Component Model | ✅ COMPLETE | `Component`, `TextComponent`, `Style` | Tree structure with cascading styles |
| SF-009 L2.1 | TranslatableComponent | ✅ COMPLETE | `TranslatableComponent.java` | Key-based translation |
| SF-009 L2.1 | ScoreComponent | ❌ NOT IMPL | - | Dynamic value placeholder component |
| SF-009 L2.2 | TextStyler Interface | ❌ NOT IMPL | - | Central facade missing |
| SF-009 L2.3 | Object Adapter Registry | ❌ NOT IMPL | - | Only basic StyleRegistry exists |
| SF-009 L2.3 | Item Adapter | ❌ NOT IMPL | - | No item formatting |
| SF-009 L2.3 | Entity Adapter | ❌ NOT IMPL | - | No entity formatting |
| SF-009 L2.4 | Semantic Color Palettes | ❌ NOT IMPL | - | No palette system |
| SF-009 L2.4 | Theme Loading (JSON/YAML) | ❌ NOT IMPL | - | No theme files |
| SF-009 L2.4 | Theme Hot-Swapping | ❌ NOT IMPL | - | No runtime theme switch |
| SF-009 L3.1 | model Package | ✅ COMPLETE | Root package | Component tree POJOs |
| SF-009 L3.1 | format Package | 🚧 PARTIAL | Root + `placeholder/` | Missing Object Adapters |
| SF-009 L3.1 | render Package | ❌ NOT IMPL | - | No visitor interfaces |
| SF-009 L3.4 | JsonSerializer | ✅ COMPLETE | `ComponentSerializer.java` | Full JSON round-trip |
| SF-009 L3.4 | LegacySerializer | ❌ NOT IMPL | - | §-code format |
| SF-009 L3.4 | PlainTextSerializer | ❌ NOT IMPL | - | Strip formatting |
| SF-009 L3.5 | Style.EMPTY Flyweight | ✅ COMPLETE | `Style.empty()` | Singleton pattern used |
| SF-009 L3.5 | Translation Caching | ❌ NOT IMPL | - | No caching logic |

**Coverage Summary**: ~45% of spec requirements implemented

### Orphan Implementations (No Specification)

| Location | Description | Proposed Action |
|----------|-------------|-----------------|
| [TextStylingLibPlugin.java](src/main/java/com/argonathsystems/framework/text/TextStylingLibPlugin.java) | Plugin entry point that violates platform independence | **DELETE** - Not needed for standalone library |
| [MiniMessage.java](src/main/java/com/argonathsystems/framework/text/MiniMessage.java) | MiniMessage parser | **DOCUMENT** - Spec mentions parsing but not MiniMessage specifically |
| [Gradient.java](src/main/java/com/argonathsystems/framework/text/Gradient.java) | Color gradient system | **DOCUMENT** - Nice feature, add to spec |
| [PlaceholderProcessor.java](src/main/java/com/argonathsystems/framework/text/placeholder/PlaceholderProcessor.java) | Placeholder processing | **DOCUMENT** - Related to L2.1 ScoreComponent concept |

### Missing Implementations (Spec Not Implemented)

| Spec ID | Requirement | Gap Description | Priority |
|---------|-------------|-----------------|----------|
| SF-009 L2.2 | TextStyler Interface | No central styling facade exists | 🔴 HIGH |
| SF-009 L2.3 | Object Adapter Registry | StyleRegistry only handles Style, not object formatters | 🟡 MEDIUM |
| SF-009 L2.4 | Theme System | No palette/theme infrastructure | 🟡 MEDIUM |
| SF-009 L3.4 | LegacySerializer | Cannot output §-code format | 🟢 LOW |
| SF-009 L3.4 | PlainTextSerializer | Cannot strip formatting | 🟢 LOW |
| SF-009 L3.1 | render Package | No visitor pattern for external renderers | 🟡 MEDIUM |
| IMPL_TRACKING | PluralResolver | Per tracking doc - plural forms for i18n | 🟡 MEDIUM |
| IMPL_TRACKING | IcuFormatter | Per tracking doc - ICU message format | 🟢 LOW |

---

## Accessor v2.0.0 Migration

### Required Changes

| Location | Current Type | Target Type | Migration Notes |
|----------|--------------|-------------|-----------------|
| N/A | - | - | **No accessor dependency required** |

### Breaking Change Impact

This module is **standalone** and has **zero dependency on the accessor framework**. It defines its own type-safe models (`Component`, `Style`, `TextColor`) that are platform-agnostic.

**No accessor migration required.**

The `TranslatableComponent.arguments` field uses `List<Object>` but this is intentional - translation arguments can be any type (String, Number, Component) and are processed at render time. This is acceptable for a text library.

---

## HyUI Integration

### Current UI Components

**N/A** - This is a text model library, not a UI rendering library. HyUI integration happens in:
- `05-framework-ui` - UI framework that consumes Components
- `02-adapter-hytale` - Converts Components to Hytale text format

### Required HyUI Patterns

None - this library is platform-agnostic.

### HyUI Migration Notes

None required.

---

## Hytale SDK Integration

### SDK Types Used

**NONE SHOULD BE USED** - This is a platform-agnostic library.

| Current Issue | Location | Required Action |
|---------------|----------|-----------------|
| JavaPlugin import | TextStylingLibPlugin.java | DELETE the entire file |
| JavaPluginInit import | TextStylingLibPlugin.java | DELETE the entire file |
| HytaleServer-parent dependency | pom.xml | REMOVE from dependencies |

### ECS Alignment Requirements

None - this library is pure data structures (POJOs) with no platform interaction.

---

## Implementation Phases

### Phase 1: Critical Fixes [1 day]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P1-001 | Delete TextStylingLibPlugin.java | `TextStylingLibPlugin.java` | 0.1 days | None |
| P1-002 | Remove HytaleServer-parent dependency from pom.xml | `pom.xml` | 0.1 days | None |
| P1-003 | Fix version mismatch (update IMPLEMENTATION_TRACKING to 1.0.0 or CHANGELOG to 0.8.0) | Both files | 0.1 days | None |
| P1-004 | Verify build passes: `mvn clean compile` | - | 0.2 days | P1-001, P1-002 |
| P1-005 | Run existing tests: `mvn test` | - | 0.1 days | P1-004 |

### Phase 2: Core Missing Features [3-4 days]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P2-001 | Create TextStyler interface per spec L2.2 | `TextStyler.java` | 0.5 days | P1-004 |
| P2-002 | Create ObjectRenderer interface and ObjectAdapterRegistry | `format/ObjectRenderer.java`, `format/ObjectAdapterRegistry.java` | 1 day | P2-001 |
| P2-003 | Create ComponentVisitor interface for render package | `render/ComponentVisitor.java` | 0.5 days | P1-004 |
| P2-004 | Implement PlainTextSerializer | `serialization/PlainTextSerializer.java` | 0.5 days | P2-003 |
| P2-005 | Implement LegacySerializer (§-codes) | `serialization/LegacySerializer.java` | 0.5 days | P2-003 |
| P2-006 | Add ScoreComponent for dynamic values | `ScoreComponent.java` | 0.5 days | P1-004 |

### Phase 3: Theme System [2 days]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P3-001 | Create Palette and Theme classes | `theme/Palette.java`, `theme/Theme.java` | 0.5 days | P1-004 |
| P3-002 | Create ThemeRegistry with semantic colors | `theme/ThemeRegistry.java` | 0.5 days | P3-001 |
| P3-003 | Implement JSON theme loading | `theme/ThemeLoader.java` | 0.5 days | P3-002 |
| P3-004 | Add hot-swap theme support | `theme/ThemeRegistry.java` | 0.5 days | P3-003 |

### Phase 4: i18n Completion [2 days]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P4-001 | Implement PluralResolver for language-specific plurals | `i18n/PluralResolver.java` | 1 day | P1-004 |
| P4-002 | Implement IcuFormatter for complex messages | `i18n/IcuFormatter.java` | 1 day | P4-001 |

### Phase 5: Testing & Validation [1 day]

| Task ID | Description | Files | Effort | Dependencies |
|---------|-------------|-------|--------|--------------|
| P5-001 | Add tests for TextStyler | `test/.../TextStylerTest.java` | 0.25 days | P2-001 |
| P5-002 | Add tests for serializers | `test/.../SerializerTest.java` | 0.25 days | P2-004, P2-005 |
| P5-003 | Add tests for theme system | `test/.../ThemeTest.java` | 0.25 days | P3-004 |
| P5-004 | Increase test coverage to 80% | Multiple test files | 0.25 days | All above |

---

## Estimated Timeline

| Phase | Duration | Start Condition |
|-------|----------|-----------------|
| Phase 1: Critical Fixes | 1 day | Immediate |
| Phase 2: Core Features | 3-4 days | After Phase 1 |
| Phase 3: Theme System | 2 days | After Phase 2 |
| Phase 4: i18n Completion | 2 days | Can parallel with Phase 3 |
| Phase 5: Testing | 1 day | After Phases 2-4 |
| **Total** | **~7-8 days** | - |

---

## Dependencies & Blockers

### Upstream Dependencies

| Module | Requirement | Status |
|--------|-------------|--------|
| `01-platform-core` | Parent POM for build configuration | ✅ Available |

**No framework dependencies** - this is a standalone library per spec L3.6.

### Downstream Impact

Modules that consume this library will need updates when new features are added:

| Module | Impact | Action Required |
|--------|--------|-----------------|
| `02-adapter-hytale` | Converts Component to Hytale text | Update when visitor pattern added |
| `05-framework-ui` | Uses Component for UI text | Update when TextStyler added |
| `06-mod-quest-tracker` | Uses text styling for quest UI | Minimal - API compatible |
| All mods using text | Style and Component usage | Minimal - API backward compatible |

### External Blockers

| Blocker | Description | Mitigation |
|---------|-------------|------------|
| None | This is a standalone library | N/A |

---

## Validation Criteria

### Build Validation
- [ ] `mvn clean compile` succeeds with zero errors
- [ ] `mvn test` passes all unit tests
- [ ] No `com.hypixel.hytale.*` imports in source files
- [ ] Shade plugin bundles Gson correctly

### Architecture Validation
- [ ] TextStylingLibPlugin.java is DELETED
- [ ] No HytaleServer-parent dependency in pom.xml
- [ ] Version numbers consistent across CHANGELOG and IMPLEMENTATION_TRACKING
- [ ] All new code follows existing code style

### Specification Validation
- [ ] TextStyler interface implemented per SF-009 L2.2
- [ ] ObjectAdapterRegistry implemented per SF-009 L2.3
- [ ] Theme/Palette system implemented per SF-009 L2.4
- [ ] Visitor pattern in render package per SF-009 L3.1
- [ ] All serializers implemented per SF-009 L3.4

---

## HytaleModder Handoff Prompt

```
# HytaleModder Task: Fix Text Styling Library Critical Issues

## Module
03-framework-text-styling

## Context
This module is a **standalone, platform-agnostic** text styling library. It MUST NOT have any Hytale SDK imports. The current implementation has critical violations that need immediate fixing.

## Immediate Tasks (Phase 1)

### P1-001: Delete Plugin File
DELETE the file: `src/main/java/com/argonathsystems/framework/text/TextStylingLibPlugin.java`
This file violates the "Zero Hytale Imports" rule and is unnecessary for a standalone library.

### P1-002: Remove Hytale Dependency
In `pom.xml`, REMOVE this dependency block:
```xml
<dependency>
    <groupId>com.hypixel.hytale</groupId>
    <artifactId>HytaleServer-parent</artifactId>
    <scope>provided</scope>
</dependency>
```

### P1-003: Fix Version Mismatch
Update IMPLEMENTATION_TRACKING.md to show version 1.0.0 to match CHANGELOG.md, OR downgrade CHANGELOG to 0.8.0.
Recommended: Keep 1.0.0 since CHANGELOG is the source of truth.

### Validation
After changes, run:
```bash
cd /mnt/d/Gaming/Argonath-Systems/03-framework-text-styling
mvn clean compile
mvn test
```

Both commands should succeed with BUILD SUCCESS.

## Reference
- Specification: SF-ARCHITECTURE-009
- Implementation Plan: IMPLEMENTATION_PLAN.md in module root
```

---

## Appendix: Current Source Structure

```
com.argonathsystems.framework.text/
├── Component.java                  ✅ Complete (interface)
├── TextComponent.java              ✅ Complete
├── TranslatableComponent.java      ✅ Complete
├── Style.java                      ✅ Complete
├── StyleRegistry.java              🚧 Basic only
├── TextColor.java                  ✅ Complete
├── Gradient.java                   ✅ Complete (orphan - not in spec)
├── MiniMessage.java                ✅ Complete (orphan - not in spec)
├── TextParser.java                 ✅ Complete
├── TextStylingLibPlugin.java       ❌ DELETE - violates spec
├── event/
│   ├── ClickAction.java            ✅ Complete
│   └── HoverEvent.java             ✅ Complete
├── i18n/
│   ├── Locale.java                 ✅ Complete
│   ├── LocaleProvider.java         ✅ Complete
│   ├── TranslationKey.java         ✅ Complete
│   └── TranslationRegistry.java    ✅ Complete
├── placeholder/
│   ├── PlaceholderContext.java     ✅ Complete
│   ├── PlaceholderProcessor.java   ✅ Complete
│   ├── PlaceholderRegistry.java    ✅ Complete
│   └── PlaceholderResolver.java    ✅ Complete
└── serialization/
    └── ComponentSerializer.java    ✅ Complete

MISSING per spec:
├── format/
│   ├── TextStyler.java             ❌ NOT IMPLEMENTED
│   ├── ObjectRenderer.java         ❌ NOT IMPLEMENTED
│   └── ObjectAdapterRegistry.java  ❌ NOT IMPLEMENTED
├── render/
│   └── ComponentVisitor.java       ❌ NOT IMPLEMENTED
├── theme/
│   ├── Palette.java                ❌ NOT IMPLEMENTED
│   ├── Theme.java                  ❌ NOT IMPLEMENTED
│   ├── ThemeRegistry.java          ❌ NOT IMPLEMENTED
│   └── ThemeLoader.java            ❌ NOT IMPLEMENTED
├── serialization/
│   ├── PlainTextSerializer.java    ❌ NOT IMPLEMENTED
│   └── LegacySerializer.java       ❌ NOT IMPLEMENTED
└── i18n/
    ├── PluralResolver.java         ❌ NOT IMPLEMENTED
    └── IcuFormatter.java           ❌ NOT IMPLEMENTED
```

---

*Generated by HytaleArchitect on 2026-01-29*
