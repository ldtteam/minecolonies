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
            Configurations.testSucceeded = config.get("Blocks", "Change to true ->", Configurations.DEFAULT_TESTSUCEEDED).getBoolean(Configurations.DEFAULT_TESTSUCEEDED);
        }
        finally
        {
            config.save();
        }
    }
}
