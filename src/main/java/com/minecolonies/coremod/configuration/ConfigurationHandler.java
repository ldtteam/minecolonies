package com.minecolonies.coremod.configuration;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

import static com.minecolonies.coremod.configuration.Configurations.*;

/**
 * Configuration Handler.
 * Reads the config file, and stores them in Configurations.java.
 * The file is FMLPreInitializationEvent.getSuggestedConfigurationFile.
 */
public final class ConfigurationHandler
{
    private static final String CATEGORY_GAMEPLAY    = "gameplay";
    private static final String CATEGORY_PATHFINDING = "pathfinding";
    private static final String CATEGORY_NAMES       = "names";

    private static final String FORMAT_RANGE = "%s (range: %s ~ %s, default: %s)";

    private ConfigurationHandler()
    {
        //Hides default constructor
    }

    /**
     * Initializes the configuration.
     * Reads all options from the file, and sets those parameters, and saves those in {@link Configurations}.
     * Saves file after reading.
     *
     * @param file File to read input from.
     */
    public static synchronized void init(final File file)
    {
        final Configuration config = new Configuration(file);

        try
        {
            config.load();
            workingRangeTownHall = config.get(CATEGORY_GAMEPLAY, "workingRangeTownHall", workingRangeTownHall, "Colony size (radius)").getInt();
            townHallPadding = config.get(CATEGORY_GAMEPLAY, "townHallPadding", townHallPadding, "Empty space between town hall boundaries").getInt();
            supplyChests = config.get(CATEGORY_GAMEPLAY, "supplyChests", supplyChests, "Allow crafting of a Supply Chest").getBoolean();
            allowInfiniteSupplyChests = config.get(CATEGORY_GAMEPLAY,
              "allowInfiniteSupplyChests", allowInfiniteSupplyChests, "Allow infinite placing of Supply Chests?").getBoolean();
            citizenRespawnInterval = getClampedInt(config, CATEGORY_GAMEPLAY,
              "citizenRespawnInterval", citizenRespawnInterval, CITIZEN_RESPAWN_INTERVAL_MIN, CITIZEN_RESPAWN_INTERVAL_MAX, "Citizen respawn interval in seconds");
            builderInfiniteResources = config.get(CATEGORY_GAMEPLAY, "builderInfiniteResources", builderInfiniteResources, "Does Builder have infinite resources?").getBoolean();
            builderBuildBlockDelay = config.get(CATEGORY_GAMEPLAY, "builderBuildBlockDelay", builderBuildBlockDelay, "How many tick between placing blocks for the builder?").getInt();
            blockMiningDelayModifier = config.get(CATEGORY_GAMEPLAY, "blockMiningDelayModifier", blockMiningDelayModifier, "Block mining Delay modifier, taken into account to determine how long a block need to be successfully mined").getInt();
            enableColonyProtection = config.get(CATEGORY_GAMEPLAY, "enableColonyProtection", enableColonyProtection, "Enable the automatic colony protection?").getBoolean();
            turnOffExplosionsInColonies = config.get(CATEGORY_GAMEPLAY, "turnOffExplosionsInColonies", turnOffExplosionsInColonies, "Turn off explosions inside the colonies radius?").getBoolean();

            deliverymanInfiniteResources =
              config.get(CATEGORY_GAMEPLAY, "deliverymanInfiniteResources", deliverymanInfiniteResources, "Does Deliveryman have infinite resources?").getBoolean();
            maxCitizens = config.get(CATEGORY_GAMEPLAY, "maxCitizens", maxCitizens, "Maximum number of citizens").getInt();
            alwaysRenderNameTag = config.get(CATEGORY_GAMEPLAY, "alwaysRenderNameTag", alwaysRenderNameTag, "Always render Citizen's name tag?").getBoolean();
            maxBlocksCheckedByBuilder =
              config.get(CATEGORY_GAMEPLAY, "maxBlocksCheckedByBuilder", maxBlocksCheckedByBuilder, "Limits the number of checked blocks per builder update").getInt();
            chatFrequency = config.get(CATEGORY_GAMEPLAY, "chatFrequency", chatFrequency, "Chat Frequency (seconds)").getInt();

            enableInDevelopmentFeatures = config.get(CATEGORY_GAMEPLAY, "development", enableInDevelopmentFeatures,
              "Display in-development features which do not work and may break your game").getBoolean();

            pathfindingDebugDraw = config.get(CATEGORY_PATHFINDING, "debugDraw", pathfindingDebugDraw, "Render pathfinding results for debugging purposes (SSP only)").getBoolean();
            pathfindingDebugVerbosity = config.get(CATEGORY_PATHFINDING, "debugVerbosity", pathfindingDebugVerbosity,
              "Debug output verbosity of pathfinding (0=none, 1=results, 2=live work)").getInt();
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

    /**
     * Returns the value in the config for <code>key</code>.
     *
     * @param config       {@link Configuration} object.
     * @param category     Category of the value to read.
     * @param key          Key of the value to read.
     * @param defaultValue Default value for the value to read.
     * @param min          Minimum accepted value.
     * @param max          Maximum accepted value.
     * @param comment      Comment in config file.
     * @return Value in the configuration file.
     */
    private static int getClampedInt(
                                      final Configuration config, final String category, final String key,
                                      final int defaultValue, final int min, final int max, final String comment)
    {
        return config.get(category, key, defaultValue, String.format(FORMAT_RANGE, comment, min, max, defaultValue), min, max).getInt();
    }
}
