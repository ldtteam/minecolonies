package com.minecolonies.api.configuration;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;

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
    public static final String CATEGORY_REQUEST     = "requestsystem";

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

            /// --- General --- \\\

            Configurations.gameplay.averageNumberOfNightsBetweenRaids =
              config.get(CATEGORY_GAMEPLAY, "averageNumberOfNightsBetweenRaids", Configurations.gameplay.averageNumberOfNightsBetweenRaids,
                "The average amount of nights between raids").getInt();
            Configurations.gameplay.barbarianHordeDifficulty =
              getClampedInt(config, CATEGORY_GAMEPLAY, "barbarianHordeDifficulty", Configurations.gameplay.barbarianHordeDifficulty,
                Constants.MIN_BARBARIAN_DIFFICULTY, Constants.MAX_BARBARIAN_DIFFICULTY, "The difficulty setting for barbarians");
            Configurations.gameplay.builderBuildBlockDelay = config.get(CATEGORY_GAMEPLAY, "builderBuildBlockDelay", Configurations.gameplay.builderBuildBlockDelay,
              "Delay after each block placement (Increasing it, increases the delay)").getInt();
            Configurations.gameplay.townHallPadding = config.get(CATEGORY_GAMEPLAY, "townHallPadding", Configurations.gameplay.townHallPadding,
              "Padding between colonies").getInt();
            Configurations.gameplay.maxBarbarianHordeSize = getClampedInt(config, CATEGORY_GAMEPLAY, "maxBarbarianHordeSize", Configurations.gameplay.maxBarbarianHordeSize,
              Constants.MIN_BARBARIAN_HORDE_SIZE, Constants.MAX_BARBARIAN_HORDE_SIZE, "The max size of a barbarian horde");
            Configurations.gameplay.citizenRespawnInterval = getClampedInt(config, CATEGORY_GAMEPLAY, "citizenRespawnInterval", Configurations.gameplay.citizenRespawnInterval,
              Constants.CITIZEN_RESPAWN_INTERVAL_MIN, Constants.CITIZEN_RESPAWN_INTERVAL_MAX, "Average citizen respawn interval (in seconds)");
            Configurations.gameplay.workingRangeTownHall = config.get(CATEGORY_GAMEPLAY, "workingRangeTownHall", Configurations.gameplay.workingRangeTownHall,
              "Colony size (radius)").getInt();
            Configurations.gameplay.blockMiningDelayModifier = config.get(CATEGORY_GAMEPLAY, "blockMiningDelayModifier", Configurations.gameplay.blockMiningDelayModifier,
              "Delay modifier to mine a block (Decreasing it, decreases the delay)").getInt();
            Configurations.gameplay.builderPlaceConstructionTape =
              config.get(CATEGORY_GAMEPLAY, "builderPlaceConstructionTape", Configurations.gameplay.builderPlaceConstructionTape,
                "Should builder place construction tape?").getBoolean();
            Configurations.gameplay.supplyChests = config.get(CATEGORY_GAMEPLAY, "supplyChests", Configurations.gameplay.supplyChests,
              "Should supply chests be craftable on this server?").getBoolean();
            Configurations.gameplay.limitToOneWareHousePerColony =
              config.get(CATEGORY_GAMEPLAY, "limitToOneWareHousePerColony", Configurations.gameplay.limitToOneWareHousePerColony,
                "Should there be at max 1 warehouse per colony?").getBoolean();
            Configurations.gameplay.enableColonyProtection = config.get(CATEGORY_GAMEPLAY, "enableColonyProtection", Configurations.gameplay.enableColonyProtection,
              "Should the colony protection be enabled?").getBoolean();
            Configurations.gameplay.turnOffExplosionsInColonies = config.get(CATEGORY_GAMEPLAY, "turnOffExplosionsInColonies", Configurations.gameplay.turnOffExplosionsInColonies,
              "Independend from the colony protection, should explosions be turned off?").getBoolean();
            Configurations.gameplay.doBarbariansSpawn = config.get(CATEGORY_GAMEPLAY, "doBarbariansSpawn", Configurations.gameplay.doBarbariansSpawn,
              "Whether or not to spawn barbarians").getBoolean();
            Configurations.gameplay.allowInfiniteSupplyChests = config.get(CATEGORY_GAMEPLAY, "allowInfiniteSupplyChests", Configurations.gameplay.allowInfiniteSupplyChests,
              "Should players be able to place an infinite amount of supplychests?").getBoolean();
            Configurations.gameplay.builderInfiniteResources = config.get(CATEGORY_GAMEPLAY, "builderInfiniteResources", Configurations.gameplay.builderInfiniteResources,
              "Should builder and miner build without resources? (this also turns off what they produce)").getBoolean();
            Configurations.gameplay.protectVillages = config.get(CATEGORY_GAMEPLAY, "protectVillages", Configurations.gameplay.protectVillages,
              "Should players be allowed to build their colonies over existing villages?").getBoolean();
            Configurations.gameplay.workersAlwaysWorkInRain = config.get(CATEGORY_GAMEPLAY, "workersAlwaysWorkInRain", Configurations.gameplay.workersAlwaysWorkInRain,
              "Should worker work during the rain?").getBoolean();

            /// --- Schematics --- \\\

            Configurations.gameplay.allowGlobalNameChanges = config.get(CATEGORY_GAMEPLAY, "allowGlobalNameChanges", Configurations.gameplay.allowGlobalNameChanges,
              "Should players be allowed to change names? -1 for false, 0 for specific groups, 1 for true").getInt();
            Configurations.gameplay.maxCachedSchematics = config.get(CATEGORY_GAMEPLAY, "maxCachedSchematics", Configurations.gameplay.maxCachedSchematics,
              "Max amount of schematics to be cached on the server").getInt();
            Configurations.gameplay.ignoreSchematicsFromJar = config.get(CATEGORY_GAMEPLAY, "ignoreSchematicsFromJar", Configurations.gameplay.ignoreSchematicsFromJar,
              "Should the default schematics be ignored (from the jar)?").getBoolean();
            Configurations.gameplay.allowPlayerSchematics = config.get(CATEGORY_GAMEPLAY, "allowPlayerSchematics", Configurations.gameplay.allowPlayerSchematics,
              "Should player made schematics be allowed").getBoolean();
            Configurations.gameplay.specialPermGroup = config.get(CATEGORY_GAMEPLAY, "specialPermGroup", Configurations.gameplay.specialPermGroup,
              "Players who have special permission (Patreons for example)").getStringList();

            /// --- Command Configs --- \\\

            Configurations.gameplay.autoDeleteColoniesInHours = config.get(CATEGORY_GAMEPLAY, "autoDeleteColoniesInHours", Configurations.gameplay.autoDeleteColoniesInHours,
              "Sets the amount of hours until a colony will be deleted after not seeing it's mayor, set to zero to disable").getInt();
            Configurations.gameplay.opLevelForServer = config.get(CATEGORY_GAMEPLAY, "opLevelForServer", Configurations.gameplay.opLevelForServer,
              "Which level counts as op level on the server").getInt();
            Configurations.gameplay.teleportBuffer = config.get(CATEGORY_GAMEPLAY, "teleportBuffer", Configurations.gameplay.teleportBuffer,
              "Time until a next teleport can be executed (in seconds)").getInt();
            Configurations.gameplay.autoDestroyColonyBlocks = config.get(CATEGORY_GAMEPLAY, "autoDestroyColonyBlocks", Configurations.gameplay.autoDestroyColonyBlocks,
              "Sets weither or not Colony structures are destroyed automatically.").getBoolean();
            Configurations.gameplay.canPlayerUseRTPCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseRTPCommand", Configurations.gameplay.canPlayerUseRTPCommand,
              "Should the player be allowed to use the '/mc rtp' command?").getBoolean();
            Configurations.gameplay.canPlayerUseHomeTPCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseHomeTPCommand", Configurations.gameplay.canPlayerUseHomeTPCommand,
              "Should the player be allowed to use the '/mc home' command?").getBoolean();
            Configurations.gameplay.canPlayerUseCitizenInfoCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseCitizenInfoCommand", Configurations.gameplay.canPlayerUseCitizenInfoCommand,
                "Should the player be allowed to use the '/mc citizens info' command?").getBoolean();
            Configurations.gameplay.canPlayerUseListCitizensCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseListCitizensCommand", Configurations.gameplay.canPlayerUseListCitizensCommand,
                "Should the player be allowed to use the '/mc citizens list' command?").getBoolean();
            Configurations.gameplay.canPlayerRespawnCitizensCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerRespawnCitizensCommand", Configurations.gameplay.canPlayerRespawnCitizensCommand,
                "Should the player be allowed to use the '/mc citizens respawn' command?").getBoolean();
            Configurations.gameplay.canPlayerUseShowColonyInfoCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseShowColonyInfoCommand", Configurations.gameplay.canPlayerUseShowColonyInfoCommand,
                "Should the player be allowed to use the '/mc colony info' command?").getBoolean();
            Configurations.gameplay.canPlayerUseKillCitizensCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseKillCitizensCommand", Configurations.gameplay.canPlayerUseKillCitizensCommand,
                "Should the player be allowed to use the '/mc citizens kill' command?").getBoolean();
            Configurations.gameplay.canPlayerUseAddOfficerCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseAddOfficerCommand", Configurations.gameplay.canPlayerUseAddOfficerCommand,
                "Should the player be allowed to use the '/mc colony addOfficer' command?").getBoolean();
            Configurations.gameplay.canPlayerUseDeleteColonyCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseDeleteColonyCommand", Configurations.gameplay.canPlayerUseDeleteColonyCommand,
                "Should the player be allowed to use the '/mc colony delete' command?").getBoolean();
            Configurations.gameplay.canPlayerUseColonyTPCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseColonyTPCommand", Configurations.gameplay.canPlayerUseColonyTPCommand,
              "Should the player be allowed to use the '/mc colony teleport' command?").getBoolean();
            Configurations.gameplay.canPlayerUseRefreshColonyCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseRefreshColonyCommand", Configurations.gameplay.canPlayerUseRefreshColonyCommand,
                "Should the player be allowed to use the '/mc colony refresh' command?").getBoolean();
            Configurations.gameplay.canPlayerUseBackupCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseBackupCommand", Configurations.gameplay.canPlayerUseBackupCommand,
              "Should the player be allowed to use the '/mc backup' command?").getBoolean();

            /// --- Colony TP Settings --- \\\

            Configurations.gameplay.numberOfAttemptsForSafeTP = config.get(CATEGORY_GAMEPLAY, "numberOfAttemptsForSafeTP", Configurations.gameplay.numberOfAttemptsForSafeTP,
              "Amount of attemps to find a save rtp").getInt();
            Configurations.gameplay.maxCitizens = config.get(CATEGORY_GAMEPLAY, "maxCitizens", Configurations.gameplay.maxCitizens,
              "Amount of initial citizens").getInt();
            Configurations.gameplay.chatFrequency = config.get(CATEGORY_GAMEPLAY, "chatFrequency", Configurations.gameplay.chatFrequency,
              "Chat frequency of worker requests").getInt();
            Configurations.gameplay.minDistanceFromWorldSpawn = config.get(CATEGORY_GAMEPLAY, "minDistanceFromWorldSpawn", Configurations.gameplay.minDistanceFromWorldSpawn,
              "Min distance from world spawn").getInt();
            Configurations.gameplay.maxBlocksCheckedByBuilder = config.get(CATEGORY_GAMEPLAY, "maxBlocksCheckedByBuilder", Configurations.gameplay.maxBlocksCheckedByBuilder,
              "Amount of blocks the builder checks (to decrease lag by builder)").getInt();
            Configurations.gameplay.maxDistanceFromWorldSpawn = config.get(CATEGORY_GAMEPLAY, "maxDistanceFromWorldSpawn", Configurations.gameplay.maxDistanceFromWorldSpawn,
              "Max distance from world spawn").getInt();
            Configurations.gameplay.alwaysRenderNameTag = config.get(CATEGORY_GAMEPLAY, "alwaysRenderNameTag", Configurations.gameplay.alwaysRenderNameTag,
              "Should citizen name tags be rendered?").getBoolean();
            Configurations.gameplay.deliverymanInfiniteResources =
              config.get(CATEGORY_GAMEPLAY, "deliverymanInfiniteResources", Configurations.gameplay.deliverymanInfiniteResources,
                "Should the dman create resources out of hot air (Not implemented)").getBoolean();
            Configurations.gameplay.enableInDevelopmentFeatures = config.get(CATEGORY_GAMEPLAY, "enableInDevelopmentFeatures", Configurations.gameplay.enableInDevelopmentFeatures,
              "Should in development features be enabled (might be buggy)").getBoolean();
            Configurations.gameplay.freeToInteractBlocks = config.get(CATEGORY_GAMEPLAY, "freeToInteractBlocks", Configurations.gameplay.freeToInteractBlocks,
              "Blocks players should be able to interact with in any colony (Ex vending machines)").getStringList();

            loadPathFindingConfigurations();
            loadNamesConfigurations();
            loadRequestSystemConfigurations();
        }
        finally
        {
            if (config.hasChanged())
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
        Configurations.pathfinding.pathfindingDebugDraw = config.get(CATEGORY_PATHFINDING, "debugDraw", Configurations.pathfinding.pathfindingDebugDraw,
          "Render pathfinding results for debugging purposes (SSP only)").getBoolean();
        Configurations.pathfinding.pathfindingDebugVerbosity = config.get(CATEGORY_PATHFINDING, "debugVerbosity", Configurations.pathfinding.pathfindingDebugVerbosity,
          "Debug output verbosity of pathfinding (0=none, 1=results, 2=live work)").getInt();
        Configurations.pathfinding.pathfindingMaxThreadCount = config.get(CATEGORY_PATHFINDING, "maxThreads", Configurations.pathfinding.pathfindingMaxThreadCount,
          "Maximum number of threads to use for pathfinding.").getInt();
    }

    /**
     * load configuration related to the citizen's names.
     */
    private static synchronized void loadNamesConfigurations()
    {
        Configurations.names.maleFirstNames = config.get(CATEGORY_NAMES, "maleFirstNames", Configurations.names.maleFirstNames,
          "Male First Names").getStringList();
        Configurations.names.femaleFirstNames = config.get(CATEGORY_NAMES, "femaleFirstNames", Configurations.names.femaleFirstNames,
          "Female First Names").getStringList();
        Configurations.names.lastNames = config.get(CATEGORY_NAMES, "lastNames", Configurations.names.lastNames,
          "Last Names").getStringList();
    }

    /**
     * load configuration related to the request system.
     */
    private static synchronized void loadRequestSystemConfigurations()
    {
        Configurations.requestSystem.maximalRetries = config.get(CATEGORY_REQUEST, "maximalRetries", Configurations.requestSystem.maximalRetries,
          "The maximal amount of tries that the request system will perform for retryable requests. Higher increases server load.").getInt();
        Configurations.requestSystem.minimalBuildingsToGather = config.get(CATEGORY_REQUEST, "minimalBuildingsToGather", Configurations.requestSystem.minimalBuildingsToGather,
          "The minimal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse.").getInt();
        Configurations.requestSystem.maximalBuildingsToGather = config.get(CATEGORY_REQUEST, "maximalBuildingsToGather", Configurations.requestSystem.maximalBuildingsToGather,
          "The maximal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse.").getInt();
        Configurations.requestSystem.delayBetweenRetries = config.get(CATEGORY_REQUEST, "delayBetweenRetries", Configurations.requestSystem.delayBetweenRetries,
          "The amount of ticks between retries of the request system for retryable requests. Lower increases server load.").getInt();
        Configurations.requestSystem.enableDebugLogging = config.get(CATEGORY_REQUEST, "enableDebugLogging", Configurations.requestSystem.enableDebugLogging,
          "Should the request system print out debug information? Useful in case of malfunctioning of set system.").getBoolean();
        Configurations.requestSystem.creativeResolve = config.get(CATEGORY_REQUEST, "creativeResolve", Configurations.requestSystem.creativeResolve,
          "Should the request system creatively resolve (if possible) when the player is required to resolve a request.").getBoolean();
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
        if (eventArgs.getModID().equalsIgnoreCase(Constants.MOD_ID))
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
    private static int getClampedInt(
      final Configuration config, final String category, final String key,
      final int defaultValue, final int min, final int max, final String comment)
    {
        return MathHelper.clamp(config.get(category, key, defaultValue, String.format(FORMAT_RANGE, comment, min, max, defaultValue), min, max).getInt(), min, max);
    }

    public static Configuration getConfiguration()
    {
        return config;
    }
}
