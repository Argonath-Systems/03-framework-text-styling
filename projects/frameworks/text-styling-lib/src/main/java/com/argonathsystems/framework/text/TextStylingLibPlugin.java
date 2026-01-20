package com.argonathsystems.framework.text;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextStylingLibPlugin extends JavaPlugin {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TextStylingLibPlugin.class);

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
