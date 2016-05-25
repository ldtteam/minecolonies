package com.schematica.core.handler;

import com.schematica.core.reference.Names;
import com.schematica.core.reference.Reference;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

public class ConfigurationHandler {
    public static Configuration configuration;

    public static final boolean CHECK_FOR_UPDATES_DEFAULT = true;

    public static boolean checkForUpdates = CHECK_FOR_UPDATES_DEFAULT;

    private static Property propCheckForUpdates = null;

    public static void init(final File configFile) {
        if (configuration == null) {
            configuration = new Configuration(configFile);
            loadConfiguration();
        }
    }

    private static void loadConfiguration() {
        propCheckForUpdates = configuration.get(Names.Config.Category.VERSION_CHECK, Names.Config.CHECK_FOR_UPDATES, CHECK_FOR_UPDATES_DEFAULT, Names.Config.CHECK_FOR_UPDATES_DESC);
        propCheckForUpdates.setLanguageKey(Names.Config.LANG_PREFIX + "." + Names.Config.CHECK_FOR_UPDATES);
        propCheckForUpdates.setRequiresMcRestart(true);
        checkForUpdates = propCheckForUpdates.getBoolean(CHECK_FOR_UPDATES_DEFAULT);

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(final ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(Reference.MODID)) {
            loadConfiguration();
        }
    }
}
