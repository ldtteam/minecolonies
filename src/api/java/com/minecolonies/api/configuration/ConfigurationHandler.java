package com.minecolonies.api.configuration;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

import static com.minecolonies.api.configuration.Configurations.*;

/**
 * Configuration Handler.
 * Reads the config file, and stores them in Configurations.java.
 * The file is FMLPreInitializationEvent.getSuggestedConfigurationFile.
 */
public final class ConfigurationHandler
{
    private static Configuration config;

    public static final String CATEGORY_GAMEPLAY    = "gameplay";
    public static final String CATEGORY_PATHFINDING = "pathfinding";
    public static final String CATEGORY_NAMES       = "names";

    private static final String FORMAT_RANGE = "%s (range: %s ~ %s, default: %s)";

    public ConfigurationHandler()
    {
        // Only used for registering the event
    }

    /**
     * Initializes the configuration.
     *
     * @param file File to read input from.
     */
    public static synchronized void init(final File file)
    {
        config = new Configuration(file);
        loadConfiguration();
    }

    /**
     * Reads all options from the file, and sets those parameters, and saves those in {@link Configurations}.
     * Saves file after reading.
     */
    private static synchronized void loadConfiguration()
    {
        try
        {
            builderPlaceConstructionTape = config.get(CATEGORY_GAMEPLAY, "placeConstructionTape", builderPlaceConstructionTape,
                    "Should builder place construction tape").getBoolean();
            workingRangeTownHall = config.get(CATEGORY_GAMEPLAY, "workingRangeTownHall", workingRangeTownHall,
                    "Colony size (radius)").getInt();
            townHallPadding = config.get(CATEGORY_GAMEPLAY, "townHallPadding", townHallPadding,
                    "Empty space between town hall boundaries").getInt();
            supplyChests = config.get(CATEGORY_GAMEPLAY, "supplyChests", supplyChests,
                    "Allow crafting of a Supply Chest").getBoolean();
            allowInfiniteSupplyChests = config.get(CATEGORY_GAMEPLAY,
              "allowInfiniteSupplyChests", allowInfiniteSupplyChests, "Allow infinite placing of Supply Chests?").getBoolean();
            citizenRespawnInterval = getClampedInt(config, CATEGORY_GAMEPLAY,
              "citizenRespawnInterval", citizenRespawnInterval, CITIZEN_RESPAWN_INTERVAL_MIN, CITIZEN_RESPAWN_INTERVAL_MAX,
                    "Citizen respawn interval in seconds");
            builderInfiniteResources = config.get(CATEGORY_GAMEPLAY, "builderInfiniteResources", builderInfiniteResources,
                    "Does Builder have infinite resources?").getBoolean();
            builderBuildBlockDelay = config.get(CATEGORY_GAMEPLAY, "builderBuildBlockDelay", builderBuildBlockDelay,
                    "How many tick between placing blocks for the builder?").getInt();
            blockMiningDelayModifier = config.get(CATEGORY_GAMEPLAY, "blockMiningDelayModifier", blockMiningDelayModifier,
                    "Block mining Delay modifier, taken into account to determine how long a block need to be successfully mined").getInt();
            enableColonyProtection = config.get(CATEGORY_GAMEPLAY, "enableColonyProtection", enableColonyProtection,
                    "Enable the automatic colony protection?").getBoolean();
            turnOffExplosionsInColonies = config.get(CATEGORY_GAMEPLAY, "turnOffExplosionsInColonies", turnOffExplosionsInColonies,
                    "Turn off explosions inside the colonies radius?").getBoolean();
            limitToOneWareHousePerColony = config.get(CATEGORY_GAMEPLAY, "limitToOneWareHousePerColony", limitToOneWareHousePerColony,
                    "Limit the Colony to 1 Warehouse per Colony?").getBoolean();
            workersAlwaysWorkInRain = config.get(CATEGORY_GAMEPLAY, "workersAlwaysWorkInRain", workersAlwaysWorkInRain,
                    "Set wether workers work in rain regardless of hut level").getBoolean();
            
            /* schematics usage */
            ignoreSchematicsFromJar = config.get(CATEGORY_GAMEPLAY, "ignoreSchematicsFromJar", ignoreSchematicsFromJar,
                    "Ignore the schematic from the jar file").getBoolean();
            allowPlayerSchematics = config.get(CATEGORY_GAMEPLAY, "allowPlayerSchematics", allowPlayerSchematics,
                    "Allow player to use their own schematics (in MP)").getBoolean();
            maxCachedSchematics = config.get(CATEGORY_GAMEPLAY, "maxCachedSchematics", maxCachedSchematics,
                    "How many chached schematics the server can store before deleting them").getInt();

            /* Configs for commands */
            opLevelForServer = config.get(CATEGORY_GAMEPLAY, "opLevelForServer", opLevelForServer,
                    "Required Op level to execute commands").getInt();
            teleportBuffer = config.get(CATEGORY_GAMEPLAY, "timeBetweenTeleport", teleportBuffer,
                    "Time until the next teleport in seconds").getInt();
            canPlayerUseCitizenInfoCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseCitizenInfoCommand", canPlayerUseCitizenInfoCommand,
                    "Players get CitizenInfoCommand").getBoolean();
            canPlayerUseRTPCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseRTPCommand", canPlayerUseRTPCommand,
                    "Players can use the MC TP Command or not").getBoolean();
            canPlayerUseDeleteColonyCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseDeleteColonyCommand", canPlayerUseDeleteColonyCommand,
                    "Players get DeleteColonyCommand").getBoolean();
            canPlayerUseKillCitizensCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseKillCitizensCommand", canPlayerUseKillCitizensCommand,
                    "Players get KillCitizensCommand").getBoolean();
            canPlayerUseListCitizensCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseListCitizensCommand", canPlayerUseListCitizensCommand,
                    "Players get ListCitizensCommand").getBoolean();
            canPlayerRespawnCitizensCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerRespawnCitizensCommand", canPlayerRespawnCitizensCommand,
                    "Players get RespawnCitizensCommand").getBoolean();
            canPlayerUseShowColonyInfoCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseShowColonyInfoCommand", canPlayerUseShowColonyInfoCommand,
                    "Players get ShowColonyInfoCommand").getBoolean();
            canPlayerUseAddOfficerCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseAddOfficerCommand", canPlayerUseAddOfficerCommand,
                    "Players get AddOfficerCommand").getBoolean();
            canPlayerUseRefreshColonyCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseRefreshColonyCommand", canPlayerUseRefreshColonyCommand,
                    "Players get RefreshColonyCommand").getBoolean();

            maxDistanceFromWorldSpawn = config.get(CATEGORY_GAMEPLAY, "maxDistanceFromWorldSpawn", maxDistanceFromWorldSpawn,
                    "Distance from spawn in all directions").getInt();

            deliverymanInfiniteResources = config.get(CATEGORY_GAMEPLAY, "deliverymanInfiniteResources", deliverymanInfiniteResources,
                    "Does Deliveryman have infinite resources?").getBoolean();
            maxCitizens = config.get(CATEGORY_GAMEPLAY, "maxCitizens", maxCitizens,
                    "Maximum number of citizens").getInt();
            alwaysRenderNameTag = config.get(CATEGORY_GAMEPLAY, "alwaysRenderNameTag", alwaysRenderNameTag,
                    "Always render Citizen's name tag?").getBoolean();
            maxBlocksCheckedByBuilder = config.get(CATEGORY_GAMEPLAY, "maxBlocksCheckedByBuilder", maxBlocksCheckedByBuilder,
                    "Limits the number of checked blocks per builder update").getInt();
            chatFrequency = config.get(CATEGORY_GAMEPLAY, "chatFrequency", chatFrequency,
                    "Chat Frequency (seconds)").getInt();

            enableInDevelopmentFeatures = config.get(CATEGORY_GAMEPLAY, "development", enableInDevelopmentFeatures,
                    "Display in-development features which do not work and may break your game").getBoolean();

            freeToInteractBlocks = config.get(CATEGORY_GAMEPLAY, "freeToInteractBlocks", freeToInteractBlocks,
                    "Blocks players should be able to interact with inside any colony.").getStringList();

            loadPathFindingConfigurations();
            loadNamesConfigurations();
        }
        finally
        {
            if(config.hasChanged())
            {
                config.save();
            }
        }
    }

    /**
     * load configuration related to Path finding.
     */
    private static synchronized void loadPathFindingConfigurations()
    {
        pathfindingDebugDraw = config.get(CATEGORY_PATHFINDING, "debugDraw", pathfindingDebugDraw,
                "Render pathfinding results for debugging purposes (SSP only)").getBoolean();
        pathfindingDebugVerbosity = config.get(CATEGORY_PATHFINDING, "debugVerbosity", pathfindingDebugVerbosity,
                "Debug output verbosity of pathfinding (0=none, 1=results, 2=live work)").getInt();
        pathfindingMaxThreadCount = config.get(CATEGORY_PATHFINDING, "maxThreads", pathfindingMaxThreadCount,
                "Maximum number of threads to use for pathfinding.").getInt();
    }

    /**
     * load configuration related to the citizen's names.
     */
    private static synchronized void loadNamesConfigurations()
    {
        maleFirstNames = config.get(CATEGORY_NAMES, "maleFirstNames", maleFirstNames,
                "Male First Names").getStringList();
        femaleFirstNames = config.get(CATEGORY_NAMES, "femaleFirstNames", femaleFirstNames,
                "Female First Names").getStringList();
        lastNames = config.get(CATEGORY_NAMES, "lastNames", lastNames,
                "Last Names").getStringList();
    }

    /**
     * This event will be called when the config gets changed through
     * the in-game GUI.
     * 
     * @param eventArgs An instance to the event. 
     */
    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent eventArgs) 
    {
        if(eventArgs.getModID().equalsIgnoreCase(Constants.MOD_ID)) 
        {
            // resync configs
            loadConfiguration();
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
    private static int getClampedInt(final Configuration config, final String category, final String key,
                                     final int defaultValue, final int min, final int max, final String comment)
    {
        return MathHelper.clamp_int(config.get(category, key, defaultValue, String.format(FORMAT_RANGE, comment, min, max, defaultValue), min, max).getInt(), min, max);
    }
    
    public static Configuration getConfiguration() 
    {
        return config;
    }

}
