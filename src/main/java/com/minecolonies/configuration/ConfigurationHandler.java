package com.minecolonies.configuration;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

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
            Configurations.workingRangeTownhall = config.get("General", "Working Range Townhall", Configurations.DEFAULT_WORKINGRANGETOWNHALL).getInt(Configurations.DEFAULT_WORKINGRANGETOWNHALL);
        }
        finally
        {
            config.save();
        }
    }
}
