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
    private static final String CATEGORY_GAMEPLAY = "Game Play";

    public static void init(File file)
    {
        Configuration config = new Configuration(file);

        try
        {
            config.load();
            Configurations.workingRangeTownhall = config.get(CATEGORY_GAMEPLAY, "Working Range Townhall: ", DEFAULT_WORKINGRANGETOWNHALL).getInt(DEFAULT_WORKINGRANGETOWNHALL);
            Configurations.townhallPadding = config.get(CATEGORY_GAMEPLAY, "Empty space between townhall boundaries: ", DEFAULT_TOWNHALLPADDING).getInt(DEFAULT_TOWNHALLPADDING);
            Configurations.allowInfiniteSupplyChests = config.get(CATEGORY_GAMEPLAY, "Allow infinite placing of Supply Chests: ", DEFAULT_ALLOWINFINTESUPPLYCHESTS).getBoolean(DEFAULT_ALLOWINFINTESUPPLYCHESTS);
            Configurations.citizenRespawnInterval = config.get(CATEGORY_GAMEPLAY, "Citizen Respawn Interval in seconds: ", DEFAULT_CITIZENRESPAWNINTERVAL).getInt(DEFAULT_CITIZENRESPAWNINTERVAL);
        }
        finally
        {
            config.save();
        }
    }
}
