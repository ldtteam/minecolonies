package com.minecolonies.configuration;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

import static com.minecolonies.configuration.Configurations.*;

/**
 * Configuration Handler.
 * Reads the config file, and stores them in Configurations.java
 * The file is FMLPreInitializationEvent.getSuggestedConfigurationFile
 */
public class ConfigurationHandler
{
    public static void init(File file)
    {
        Configuration config = new Configuration(file);

        try
        {
            config.load();
            Configurations.workingRangeTownhall = config.get("Game Play", "Working Range Townhall: ", DEFAULT_WORKINGRANGETOWNHALL).getInt(DEFAULT_WORKINGRANGETOWNHALL);
            Configurations.allowInfinitePlacing = config.get("Game Play", "Allow infinite placing of Supply Chests: ", DEFAULT_ALLOWINFINTEPLACING).getBoolean(DEFAULT_ALLOWINFINTEPLACING);
        }
        finally
        {
            config.save();
        }
    }
}
