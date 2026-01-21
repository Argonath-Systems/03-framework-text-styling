package com.argonathsystems.framework.text.i18n;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Translation System Tests")
class TranslationSystemTest {
    
    private TranslationRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new TranslationRegistry();
    }
    
    @Nested
    @DisplayName("TranslationKey Tests")
    class TranslationKeyTests {
        
        @Test
        @DisplayName("should create key with default namespace")
        void createWithDefaultNamespace() {
            TranslationKey key = TranslationKey.of("ui.button.confirm");
            
            assertThat(key.namespace()).isEqualTo("core");
            assertThat(key.key()).isEqualTo("ui.button.confirm");
            assertThat(key.fullKey()).isEqualTo("core:ui.button.confirm");
        }
        
        @Test
        @DisplayName("should create key with custom namespace")
        void createWithCustomNamespace() {
            TranslationKey key = TranslationKey.of("my-mod", "quest.tutorial.title");
            
            assertThat(key.namespace()).isEqualTo("my-mod");
            assertThat(key.key()).isEqualTo("quest.tutorial.title");
            assertThat(key.fullKey()).isEqualTo("my-mod:quest.tutorial.title");
        }
        
        @Test
        @DisplayName("should parse key with namespace")
        void parseWithNamespace() {
            TranslationKey key = TranslationKey.parse("quest-mod:objectives.kill");
            
            assertThat(key.namespace()).isEqualTo("quest-mod");
            assertThat(key.key()).isEqualTo("objectives.kill");
        }
        
        @Test
        @DisplayName("should parse key without namespace")
        void parseWithoutNamespace() {
            TranslationKey key = TranslationKey.parse("ui.welcome");
            
            assertThat(key.namespace()).isEqualTo("core");
            assertThat(key.key()).isEqualTo("ui.welcome");
        }
        
        @Test
        @DisplayName("should reject null key")
        void rejectNullKey() {
            assertThatThrownBy(() -> TranslationKey.of(null))
                .isInstanceOf(NullPointerException.class);
        }
        
        @Test
        @DisplayName("should reject blank key")
        void rejectBlankKey() {
            assertThatThrownBy(() -> TranslationKey.of("  "))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
    
    @Nested
    @DisplayName("Locale Tests")
    class LocaleTests {
        
        @Test
        @DisplayName("should create language-only locale")
        void createLanguageOnly() {
            Locale locale = Locale.of("en");
            
            assertThat(locale.language()).isEqualTo("en");
            assertThat(locale.country()).isEmpty();
            assertThat(locale.code()).isEqualTo("en");
        }
        
        @Test
        @DisplayName("should create locale with country")
        void createWithCountry() {
            Locale locale = Locale.of("en", "US");
            
            assertThat(locale.language()).isEqualTo("en");
            assertThat(locale.country()).isEqualTo("US");
            assertThat(locale.code()).isEqualTo("en_US");
        }
        
        @Test
        @DisplayName("should parse locale string")
        void parseLocaleString() {
            Locale locale = Locale.parse("fr_CA");
            
            assertThat(locale.language()).isEqualTo("fr");
            assertThat(locale.country()).isEqualTo("CA");
        }
        
        @Test
        @DisplayName("should get parent locale")
        void getParentLocale() {
            Locale locale = Locale.of("en", "US");
            Locale parent = locale.parent();
            
            assertThat(parent).isNotNull();
            assertThat(parent.language()).isEqualTo("en");
            assertThat(parent.country()).isEmpty();
        }
        
        @Test
        @DisplayName("should return null parent for language-only locale")
        void noParentForLanguageOnly() {
            Locale locale = Locale.of("en");
            assertThat(locale.parent()).isNull();
        }
        
        @Test
        @DisplayName("should convert to Java Locale")
        void convertToJavaLocale() {
            Locale locale = Locale.of("de", "AT");
            java.util.Locale javaLocale = locale.toJavaLocale();
            
            assertThat(javaLocale.getLanguage()).isEqualTo("de");
            assertThat(javaLocale.getCountry()).isEqualTo("AT");
        }
    }
    
    @Nested
    @DisplayName("TranslationRegistry Tests")
    class TranslationRegistryTests {
        
        @Test
        @DisplayName("should register and retrieve translation")
        void registerAndRetrieve() {
            registry.register("core", Locale.ENGLISH, "ui.confirm", "Confirm");
            
            Optional<String> result = registry.translate(
                TranslationKey.of("ui.confirm"), Locale.ENGLISH
            );
            
            assertThat(result).contains("Confirm");
        }
        
        @Test
        @DisplayName("should fallback to language-only locale")
        void fallbackToLanguageOnly() {
            registry.register("core", Locale.ENGLISH, "ui.cancel", "Cancel");
            
            // Request en_US but we only have en
            Optional<String> result = registry.translate(
                TranslationKey.of("ui.cancel"), Locale.ENGLISH_US
            );
            
            assertThat(result).contains("Cancel");
        }
        
        @Test
        @DisplayName("should fallback to default locale")
        void fallbackToDefault() {
            registry.register("core", Locale.DEFAULT, "ui.error", "Error");
            
            // Request French but we only have default (English)
            Optional<String> result = registry.translate(
                TranslationKey.of("ui.error"), Locale.FRENCH
            );
            
            assertThat(result).contains("Error");
        }
        
        @Test
        @DisplayName("should substitute placeholders")
        void substitutePlaceholders() {
            registry.register("core", Locale.ENGLISH, "ui.welcome", "Hello, {0}!");
            
            Optional<String> result = registry.translate(
                TranslationKey.of("ui.welcome"), Locale.ENGLISH, "Steve"
            );
            
            assertThat(result).contains("Hello, Steve!");
        }
        
        @Test
        @DisplayName("should substitute multiple placeholders")
        void substituteMultiplePlaceholders() {
            registry.register("core", Locale.ENGLISH, "ui.message", "{0} sent {1} gold to {2}");
            
            String result = registry.translateOrKey(
                TranslationKey.of("ui.message"), Locale.ENGLISH, "Alice", 100, "Bob"
            );
            
            assertThat(result).isEqualTo("Alice sent 100 gold to Bob");
        }
        
        @Test
        @DisplayName("should load from properties file")
        void loadFromProperties() throws IOException {
            String props = """
                ui.title=My App
                ui.button.ok=OK
                ui.button.cancel=Cancel
                """;
            
            registry.loadFromProperties("test", Locale.ENGLISH, 
                new ByteArrayInputStream(props.getBytes(StandardCharsets.UTF_8)));
            
            assertThat(registry.translate(TranslationKey.of("test", "ui.title"), Locale.ENGLISH))
                .contains("My App");
            assertThat(registry.translate(TranslationKey.of("test", "ui.button.ok"), Locale.ENGLISH))
                .contains("OK");
        }
        
        @Test
        @DisplayName("should return key when translation not found")
        void returnKeyWhenNotFound() {
            String result = registry.translateOrKey(
                TranslationKey.of("missing.key"), Locale.ENGLISH
            );
            
            assertThat(result).isEqualTo("missing.key");
        }
        
        @Test
        @DisplayName("should check translation existence")
        void checkTranslationExists() {
            registry.register("core", Locale.ENGLISH, "exists", "Yes");
            
            assertThat(registry.hasTranslation(TranslationKey.of("exists"), Locale.ENGLISH))
                .isTrue();
            assertThat(registry.hasTranslation(TranslationKey.of("missing"), Locale.ENGLISH))
                .isFalse();
        }
        
        @Test
        @DisplayName("should clear namespace")
        void clearNamespace() {
            registry.register("test", Locale.ENGLISH, "key", "value");
            registry.clearNamespace("test");
            
            assertThat(registry.translate(TranslationKey.of("test", "key"), Locale.ENGLISH))
                .isEmpty();
        }
    }
    
    @Nested
    @DisplayName("LocaleProvider Tests")
    class LocaleProviderTests {
        
        @Test
        @DisplayName("should provide default locale")
        void provideDefaultLocale() {
            LocaleProvider provider = LocaleProvider.defaultLocale();
            
            assertThat(provider.getLocale(java.util.UUID.randomUUID()))
                .isEqualTo(Locale.DEFAULT);
        }
        
        @Test
        @DisplayName("should provide fixed locale")
        void provideFixedLocale() {
            LocaleProvider provider = LocaleProvider.fixed(Locale.FRENCH);
            
            assertThat(provider.getLocale(java.util.UUID.randomUUID()))
                .isEqualTo(Locale.FRENCH);
        }
    }
}
