package com.argonathsystems.framework.text;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.logger.HytaleLogger;


public class TextStylingLibPlugin extends JavaPlugin {
    
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public TextStylingLibPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Initializing Text Styling Lib...")
    }

    @Override
    protected void setup() {
        super.setup();
        LOGGER.atInfo().log("Text Styling Lib enabled.")
    }
}
