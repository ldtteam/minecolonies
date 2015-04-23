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
    private static final String CATEGORY_GAMEPLAY = "gameplay";
    private static final String CATEGORY_PATHFINDING = "pathfinding";
    private static final String CATEGORY_NAMES    = "names";

    public static void init(File file)
    {
        Configuration config = new Configuration(file);

        try
        {
            config.load();
            workingRangeTownhall = config.get(CATEGORY_GAMEPLAY, "workingRangeTownhall", workingRangeTownhall, "Townhall Working Range").getInt();
            townhallPadding = config.get(CATEGORY_GAMEPLAY, "townhallPadding", townhallPadding, "Empty space between townhall boundaries").getInt();
            allowInfiniteSupplyChests = config.get(CATEGORY_GAMEPLAY, "allowInfiniteSupplyChests", allowInfiniteSupplyChests, "Allow infinite placing of Supply Chests?").getBoolean();
            citizenRespawnInterval = getClampedInt(config, CATEGORY_GAMEPLAY, "citizenRespawnInterval", citizenRespawnInterval, CITIZEN_RESPAWN_INTERVAL_MIN, CITIZEN_RESPAWN_INTERVAL_MAX, "Citizen respawn interval in seconds");
            builderInfiniteResources = config.get(CATEGORY_GAMEPLAY, "builderInfiniteResources", builderInfiniteResources, "Does Builder have infinite resources?").getBoolean();
            deliverymanInfiniteResources = config.get(CATEGORY_GAMEPLAY, "deliverymanInfiniteResources", deliverymanInfiniteResources, "Does Deliveryman have infinite resources?").getBoolean();
            maxCitizens = config.get(CATEGORY_GAMEPLAY, "maxCitizens", maxCitizens, "Maximum number of citizens").getInt();
            alwaysRenderNameTag = config.get(CATEGORY_GAMEPLAY, "alwaysRenderNameTag", alwaysRenderNameTag, "Always render Citizen's name tag?").getBoolean();
            maxBlocksCheckedByBuilder = config.get(CATEGORY_GAMEPLAY, "maxBlocksCheckedByBuilder", maxBlocksCheckedByBuilder, "Limits the number of checked blocks per builder update").getInt();

            enableInDevelopmentFeatures = config.get(CATEGORY_GAMEPLAY, "development", enableInDevelopmentFeatures, "Don't hide in-development features which do not work and may break your game").getBoolean();

            pathfindingDebugDraw = config.get(CATEGORY_PATHFINDING, "debugDraw", pathfindingDebugDraw, "Render pathfinding results for debugging purposes (SSP only)").getBoolean();
            pathfindingDebugVerbosity = config.get(CATEGORY_PATHFINDING, "debugVerbosity", pathfindingDebugVerbosity, "Debug output verbosity of pathfinding (0=none, 1=results, 2=live work)").getInt();
            pathfindingMaxThreadCount = config.get(CATEGORY_PATHFINDING, "maxThreads", pathfindingMaxThreadCount, "Maximum number of threads to use for pathfinding.").getInt();

            maleFirstNames = config.get(CATEGORY_NAMES, "maleFirstNames", maleFirstNames, "Male First Names").getStringList();
            femaleFirstNames = config.get(CATEGORY_NAMES, "femaleFirstNames", femaleFirstNames, "Female First Names").getStringList();
            lastNames = config.get(CATEGORY_NAMES, "lastNames", lastNames, "Last Names").getStringList();
        }
        finally
        {
            config.save();
        }
    }

    private static final String FORMAT_RANGE = "%s (range: %s ~ %s, default: %s)";

    private static int getClampedInt(Configuration config, String category, String key, int defaultValue, int min, int max, String comment)
    {
        return config.get(category, key, defaultValue, String.format(FORMAT_RANGE, comment, min, max, defaultValue), min, max).getInt();
    }
}
