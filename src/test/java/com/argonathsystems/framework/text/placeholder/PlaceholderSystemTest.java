package com.argonathsystems.framework.text.placeholder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Placeholder System Tests")
class PlaceholderSystemTest {
    
    @Nested
    @DisplayName("PlaceholderContext Tests")
    class PlaceholderContextTests {
        
        @Test
        @DisplayName("should create simple context")
        void createSimpleContext() {
            PlaceholderContext ctx = PlaceholderContext.of("player_name");
            
            assertThat(ctx.key()).isEqualTo("player_name");
            assertThat(ctx.params()).isEmpty();
            assertThat(ctx.viewerId()).isNull();
        }
        
        @Test
        @DisplayName("should parse context with params")
        void parseWithParams() {
            PlaceholderContext ctx = PlaceholderContext.parse("currency:gold:compact", null);
            
            assertThat(ctx.key()).isEqualTo("currency");
            assertThat(ctx.params()).containsExactly("gold", "compact");
        }
        
        @Test
        @DisplayName("should access params by index")
        void accessParamsByIndex() {
            PlaceholderContext ctx = PlaceholderContext.parse("stat:health:max", null);
            
            assertThat(ctx.param(0)).isEqualTo("health");
            assertThat(ctx.param(1)).isEqualTo("max");
            assertThat(ctx.param(2)).isNull();
            assertThat(ctx.param(2, "default")).isEqualTo("default");
        }
    }
    
    @Nested
    @DisplayName("PlaceholderProcessor Tests")
    class PlaceholderProcessorTests {
        
        private PlaceholderResolver resolver;
        
        @BeforeEach
        void setUp() {
            resolver = ctx -> switch (ctx.key()) {
                case "player_name" -> Optional.of("Steve");
                case "player_level" -> Optional.of("42");
                case "server_name" -> Optional.of("My Server");
                default -> Optional.empty();
            };
        }
        
        @Test
        @DisplayName("should process single placeholder")
        void processSinglePlaceholder() {
            String result = PlaceholderProcessor.process("Hello, {player_name}!", resolver);
            
            assertThat(result).isEqualTo("Hello, Steve!");
        }
        
        @Test
        @DisplayName("should process multiple placeholders")
        void processMultiplePlaceholders() {
            String result = PlaceholderProcessor.process(
                "{player_name} (Level {player_level}) on {server_name}",
                resolver
            );
            
            assertThat(result).isEqualTo("Steve (Level 42) on My Server");
        }
        
        @Test
        @DisplayName("should keep unresolved placeholders")
        void keepUnresolvedPlaceholders() {
            String result = PlaceholderProcessor.process("Hello, {unknown}!", resolver);
            
            assertThat(result).isEqualTo("Hello, {unknown}!");
        }
        
        @Test
        @DisplayName("should remove unresolved when requested")
        void removeUnresolvedPlaceholders() {
            String result = PlaceholderProcessor.process(
                "Hello, {unknown}!", resolver, null, true
            );
            
            assertThat(result).isEqualTo("Hello, !");
        }
        
        @Test
        @DisplayName("should process percent-style placeholders")
        void processPercentPlaceholders() {
            String result = PlaceholderProcessor.processPercent(
                "Hello, %player_name%!", resolver
            );
            
            assertThat(result).isEqualTo("Hello, Steve!");
        }
        
        @Test
        @DisplayName("should detect placeholders")
        void detectPlaceholders() {
            assertThat(PlaceholderProcessor.containsPlaceholders("Hello, {player}!")).isTrue();
            assertThat(PlaceholderProcessor.containsPlaceholders("Hello, World!")).isFalse();
        }
        
        @Test
        @DisplayName("should detect percent placeholders")
        void detectPercentPlaceholders() {
            assertThat(PlaceholderProcessor.containsPercentPlaceholders("Hello, %player%!")).isTrue();
            assertThat(PlaceholderProcessor.containsPercentPlaceholders("Hello, World!")).isFalse();
        }
        
        @Test
        @DisplayName("should handle text without placeholders")
        void handleTextWithoutPlaceholders() {
            String result = PlaceholderProcessor.process("No placeholders here", resolver);
            
            assertThat(result).isEqualTo("No placeholders here");
        }
    }
    
    @Nested
    @DisplayName("PlaceholderResolver Tests")
    class PlaceholderResolverTests {
        
        @Test
        @DisplayName("should chain resolvers")
        void chainResolvers() {
            PlaceholderResolver resolver1 = ctx -> 
                ctx.key().equals("a") ? Optional.of("A") : Optional.empty();
            PlaceholderResolver resolver2 = ctx -> 
                ctx.key().equals("b") ? Optional.of("B") : Optional.empty();
            
            PlaceholderResolver chained = PlaceholderResolver.chain(resolver1, resolver2);
            
            assertThat(chained.resolve(PlaceholderContext.of("a"))).contains("A");
            assertThat(chained.resolve(PlaceholderContext.of("b"))).contains("B");
            assertThat(chained.resolve(PlaceholderContext.of("c"))).isEmpty();
        }
        
        @Test
        @DisplayName("should create fixed resolver")
        void createFixedResolver() {
            PlaceholderResolver resolver = PlaceholderResolver.fixed("key", "value");
            
            assertThat(resolver.resolve(PlaceholderContext.of("key"))).contains("value");
            assertThat(resolver.resolve(PlaceholderContext.of("other"))).isEmpty();
        }
        
        @Test
        @DisplayName("should create empty resolver")
        void createEmptyResolver() {
            PlaceholderResolver resolver = PlaceholderResolver.empty();
            
            assertThat(resolver.resolve(PlaceholderContext.of("anything"))).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("PlaceholderRegistry Tests")
    class PlaceholderRegistryTests {
        
        private PlaceholderRegistry registry;
        
        @BeforeEach
        void setUp() {
            registry = new PlaceholderRegistry();
        }
        
        @Test
        @DisplayName("should register and retrieve resolver")
        void registerAndRetrieve() {
            PlaceholderResolver resolver = ctx -> Optional.of("test");
            registry.register("mymod", resolver);
            
            assertThat(registry.get("mymod")).isPresent();
        }
        
        @Test
        @DisplayName("should route by prefix")
        void routeByPrefix() {
            registry.register("player", ctx -> 
                ctx.key().startsWith("player_") ? Optional.of("Player Value") : Optional.empty()
            );
            registry.register("server", ctx -> 
                ctx.key().startsWith("server_") ? Optional.of("Server Value") : Optional.empty()
            );
            
            PlaceholderResolver combined = registry.combined();
            
            assertThat(combined.resolve(PlaceholderContext.of("player_name"))).contains("Player Value");
            assertThat(combined.resolve(PlaceholderContext.of("server_name"))).contains("Server Value");
        }
        
        @Test
        @DisplayName("should unregister resolver")
        void unregisterResolver() {
            registry.register("test", ctx -> Optional.of("value"));
            registry.unregister("test");
            
            assertThat(registry.get("test")).isEmpty();
        }
        
        @Test
        @DisplayName("should clear all resolvers")
        void clearAllResolvers() {
            registry.register("a", ctx -> Optional.of("A"));
            registry.register("b", ctx -> Optional.of("B"));
            registry.clear();
            
            assertThat(registry.size()).isZero();
        }
    }
}
