package com.argonathsystems.framework.text;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.logging.Level;

/**
 * Plugin entry point for the Text Styling Library.
 * 
 * <p>This library provides platform-agnostic text styling and component systems.
 * The actual Hytale text component integration is handled by the adapter layer.</p>
 */
public class TextStylingLibPlugin extends JavaPlugin {

    public TextStylingLibPlugin(JavaPluginInit init) {
        super(init);
    }

    protected void setup() {
        getLogger().at(Level.INFO).log("Text Styling Library loaded (library mode).");
    }
}
