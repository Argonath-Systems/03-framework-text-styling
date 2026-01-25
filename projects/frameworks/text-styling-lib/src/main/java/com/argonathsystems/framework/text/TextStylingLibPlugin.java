package com.argonathsystems.framework.text;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.logging.Logger;


public class TextStylingLibPlugin extends JavaPlugin {
    
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public TextStylingLibPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.info("Initializing Text Styling Lib...");
    }

    @Override
    protected void setup() {
        super.setup();
        LOGGER.info("Text Styling Lib enabled.");
    }
}
