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

            Configurations.Gameplay.averageNumberOfNightsBetweenRaids =
              config.get(CATEGORY_GAMEPLAY, "averageNumberOfNightsBetweenRaids", Configurations.Gameplay.averageNumberOfNightsBetweenRaids,
                "The average amount of nights between raids").getInt();
            Configurations.Gameplay.barbarianHordeDifficulty =
              getClampedInt(config, CATEGORY_GAMEPLAY, "barbarianHordeDifficulty", Configurations.Gameplay.barbarianHordeDifficulty,
                Constants.MIN_BARBARIAN_DIFFICULTY, Constants.MAX_BARBARIAN_DIFFICULTY, "The difficulty setting for barbarians");
            Configurations.Gameplay.builderBuildBlockDelay = config.get(CATEGORY_GAMEPLAY, "builderBuildBlockDelay", Configurations.Gameplay.builderBuildBlockDelay,
              "Delay after each block placement (Increasing it, increases the delay)").getInt();
            Configurations.Gameplay.townHallPadding = config.get(CATEGORY_GAMEPLAY, "townHallPadding", Configurations.Gameplay.townHallPadding,
              "Padding between colonies").getInt();
            Configurations.Gameplay.maxBarbarianHordeSize = getClampedInt(config, CATEGORY_GAMEPLAY, "maxBarbarianHordeSize", Configurations.Gameplay.maxBarbarianHordeSize,
              Constants.MIN_BARBARIAN_HORDE_SIZE, Constants.MAX_BARBARIAN_HORDE_SIZE, "The max size of a barbarian horde");
            Configurations.Gameplay.citizenRespawnInterval = getClampedInt(config, CATEGORY_GAMEPLAY, "citizenRespawnInterval", Configurations.Gameplay.citizenRespawnInterval,
              Constants.CITIZEN_RESPAWN_INTERVAL_MIN, Constants.CITIZEN_RESPAWN_INTERVAL_MAX, "Average citizen respawn interval (in seconds)");
            Configurations.Gameplay.workingRangeTownHall = config.get(CATEGORY_GAMEPLAY, "workingRangeTownHall", Configurations.Gameplay.workingRangeTownHall,
              "Colony size (radius)").getInt();
            Configurations.Gameplay.blockMiningDelayModifier = config.get(CATEGORY_GAMEPLAY, "blockMiningDelayModifier", Configurations.Gameplay.blockMiningDelayModifier,
              "Delay modifier to mine a block (Decreasing it, decreases the delay)").getInt();
            Configurations.Gameplay.builderPlaceConstructionTape =
              config.get(CATEGORY_GAMEPLAY, "builderPlaceConstructionTape", Configurations.Gameplay.builderPlaceConstructionTape,
                "Should builder place construction tape?").getBoolean();
            Configurations.Gameplay.supplyChests = config.get(CATEGORY_GAMEPLAY, "supplyChests", Configurations.Gameplay.supplyChests,
              "Should supply chests be craftable on this server?").getBoolean();
            Configurations.Gameplay.limitToOneWareHousePerColony =
              config.get(CATEGORY_GAMEPLAY, "limitToOneWareHousePerColony", Configurations.Gameplay.limitToOneWareHousePerColony,
                "Should there be at max 1 warehouse per colony?").getBoolean();
            Configurations.Gameplay.enableColonyProtection = config.get(CATEGORY_GAMEPLAY, "enableColonyProtection", Configurations.Gameplay.enableColonyProtection,
              "Should the colony protection be enabled?").getBoolean();
            Configurations.Gameplay.turnOffExplosionsInColonies = config.get(CATEGORY_GAMEPLAY, "turnOffExplosionsInColonies", Configurations.Gameplay.turnOffExplosionsInColonies,
              "Independend from the colony protection, should explosions be turned off?").getBoolean();
            Configurations.Gameplay.doBarbariansSpawn = config.get(CATEGORY_GAMEPLAY, "doBarbariansSpawn", Configurations.Gameplay.doBarbariansSpawn,
              "Whether or not to spawn barbarians").getBoolean();
            Configurations.Gameplay.allowInfiniteSupplyChests = config.get(CATEGORY_GAMEPLAY, "allowInfiniteSupplyChests", Configurations.Gameplay.allowInfiniteSupplyChests,
              "Should players be able to place an infinite amount of supplychests?").getBoolean();
            Configurations.Gameplay.builderInfiniteResources = config.get(CATEGORY_GAMEPLAY, "builderInfiniteResources", Configurations.Gameplay.builderInfiniteResources,
              "Should builder and miner build without resources? (this also turns off what they produce)").getBoolean();
            Configurations.Gameplay.protectVillages = config.get(CATEGORY_GAMEPLAY, "protectVillages", Configurations.Gameplay.protectVillages,
              "Should players be allowed to build their colonies over existing villages?").getBoolean();
            Configurations.Gameplay.workersAlwaysWorkInRain = config.get(CATEGORY_GAMEPLAY, "workersAlwaysWorkInRain", Configurations.Gameplay.workersAlwaysWorkInRain,
              "Should worker work during the rain?").getBoolean();

            /// --- Schematics --- \\\

            Configurations.Gameplay.allowGlobalNameChanges = config.get(CATEGORY_GAMEPLAY, "allowGlobalNameChanges", Configurations.Gameplay.allowGlobalNameChanges,
              "Should players be allowed to change.Names. -1 for false, 0 for specific groups, 1 for true").getInt();
            Configurations.Gameplay.maxCachedSchematics = config.get(CATEGORY_GAMEPLAY, "maxCachedSchematics", Configurations.Gameplay.maxCachedSchematics,
              "Max amount of schematics to be cached on the server").getInt();
            Configurations.Gameplay.ignoreSchematicsFromJar = config.get(CATEGORY_GAMEPLAY, "ignoreSchematicsFromJar", Configurations.Gameplay.ignoreSchematicsFromJar,
              "Should the default schematics be ignored (from the jar)?").getBoolean();
            Configurations.Gameplay.allowPlayerSchematics = config.get(CATEGORY_GAMEPLAY, "allowPlayerSchematics", Configurations.Gameplay.allowPlayerSchematics,
              "Should player made schematics be allowed").getBoolean();
            Configurations.Gameplay.specialPermGroup = config.get(CATEGORY_GAMEPLAY, "specialPermGroup", Configurations.Gameplay.specialPermGroup,
              "Players who have special permission (Patreons for example)").getStringList();

            /// --- Command Configs --- \\\

            Configurations.Gameplay.autoDeleteColoniesInHours = config.get(CATEGORY_GAMEPLAY, "autoDeleteColoniesInHours", Configurations.Gameplay.autoDeleteColoniesInHours,
              "Sets the amount of hours until a colony will be deleted after not seeing it's mayor, set to zero to disable").getInt();
            Configurations.Gameplay.opLevelForServer = config.get(CATEGORY_GAMEPLAY, "opLevelForServer", Configurations.Gameplay.opLevelForServer,
              "Which level counts as op level on the server").getInt();
            Configurations.Gameplay.teleportBuffer = config.get(CATEGORY_GAMEPLAY, "teleportBuffer", Configurations.Gameplay.teleportBuffer,
              "Time until a next teleport can be executed (in seconds)").getInt();
            Configurations.Gameplay.autoDestroyColonyBlocks = config.get(CATEGORY_GAMEPLAY, "autoDestroyColonyBlocks", Configurations.Gameplay.autoDestroyColonyBlocks,
              "Sets weither or not Colony structures are destroyed automatically.").getBoolean();
            Configurations.Gameplay.canPlayerUseRTPCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseRTPCommand", Configurations.Gameplay.canPlayerUseRTPCommand,
              "Should the player be allowed to use the '/mc rtp' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseHomeTPCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseHomeTPCommand", Configurations.Gameplay.canPlayerUseHomeTPCommand,
              "Should the player be allowed to use the '/mc home' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseCitizenInfoCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseCitizenInfoCommand", Configurations.Gameplay.canPlayerUseCitizenInfoCommand,
                "Should the player be allowed to use the '/mc citizens info' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseListCitizensCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseListCitizensCommand", Configurations.Gameplay.canPlayerUseListCitizensCommand,
                "Should the player be allowed to use the '/mc citizens list' command?").getBoolean();
            Configurations.Gameplay.canPlayerRespawnCitizensCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerRespawnCitizensCommand", Configurations.Gameplay.canPlayerRespawnCitizensCommand,
                "Should the player be allowed to use the '/mc citizens respawn' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseShowColonyInfoCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseShowColonyInfoCommand", Configurations.Gameplay.canPlayerUseShowColonyInfoCommand,
                "Should the player be allowed to use the '/mc colony info' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseKillCitizensCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseKillCitizensCommand", Configurations.Gameplay.canPlayerUseKillCitizensCommand,
                "Should the player be allowed to use the '/mc citizens kill' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseAddOfficerCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseAddOfficerCommand", Configurations.Gameplay.canPlayerUseAddOfficerCommand,
                "Should the player be allowed to use the '/mc colony addOfficer' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseDeleteColonyCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseDeleteColonyCommand", Configurations.Gameplay.canPlayerUseDeleteColonyCommand,
                "Should the player be allowed to use the '/mc colony delete' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseColonyTPCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseColonyTPCommand", Configurations.Gameplay.canPlayerUseColonyTPCommand,
              "Should the player be allowed to use the '/mc colony teleport' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseRefreshColonyCommand =
              config.get(CATEGORY_GAMEPLAY, "canPlayerUseRefreshColonyCommand", Configurations.Gameplay.canPlayerUseRefreshColonyCommand,
                "Should the player be allowed to use the '/mc colony refresh' command?").getBoolean();
            Configurations.Gameplay.canPlayerUseBackupCommand = config.get(CATEGORY_GAMEPLAY, "canPlayerUseBackupCommand", Configurations.Gameplay.canPlayerUseBackupCommand,
              "Should the player be allowed to use the '/mc backup' command?").getBoolean();

            /// --- Colony TP Settings --- \\\

            Configurations.Gameplay.numberOfAttemptsForSafeTP = config.get(CATEGORY_GAMEPLAY, "numberOfAttemptsForSafeTP", Configurations.Gameplay.numberOfAttemptsForSafeTP,
              "Amount of attemps to find a save rtp").getInt();
            Configurations.Gameplay.maxCitizens = config.get(CATEGORY_GAMEPLAY, "maxCitizens", Configurations.Gameplay.maxCitizens,
              "Amount of initial citizens").getInt();
            Configurations.Gameplay.chatFrequency = config.get(CATEGORY_GAMEPLAY, "chatFrequency", Configurations.Gameplay.chatFrequency,
              "Chat frequency of worker requests").getInt();
            Configurations.Gameplay.minDistanceFromWorldSpawn = config.get(CATEGORY_GAMEPLAY, "minDistanceFromWorldSpawn", Configurations.Gameplay.minDistanceFromWorldSpawn,
              "Min distance from world spawn").getInt();
            Configurations.Gameplay.maxBlocksCheckedByBuilder = config.get(CATEGORY_GAMEPLAY, "maxBlocksCheckedByBuilder", Configurations.Gameplay.maxBlocksCheckedByBuilder,
              "Amount of blocks the builder checks (to decrease lag by builder)").getInt();
            Configurations.Gameplay.maxDistanceFromWorldSpawn = config.get(CATEGORY_GAMEPLAY, "maxDistanceFromWorldSpawn", Configurations.Gameplay.maxDistanceFromWorldSpawn,
              "Max distance from world spawn").getInt();
            Configurations.Gameplay.alwaysRenderNameTag = config.get(CATEGORY_GAMEPLAY, "alwaysRenderNameTag", Configurations.Gameplay.alwaysRenderNameTag,
              "Should citizen name tags be rendered?").getBoolean();
            Configurations.Gameplay.deliverymanInfiniteResources =
              config.get(CATEGORY_GAMEPLAY, "deliverymanInfiniteResources", Configurations.Gameplay.deliverymanInfiniteResources,
                "Should the dman create resources out of hot air (Not implemented)").getBoolean();
            Configurations.Gameplay.enableInDevelopmentFeatures = config.get(CATEGORY_GAMEPLAY, "enableInDevelopmentFeatures", Configurations.Gameplay.enableInDevelopmentFeatures,
              "Should in development features be enabled (might be buggy)").getBoolean();
            Configurations.Gameplay.freeToInteractBlocks = config.get(CATEGORY_GAMEPLAY, "freeToInteractBlocks", Configurations.Gameplay.freeToInteractBlocks,
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
        Configurations.Pathfinding.pathfindingDebugDraw = config.get(CATEGORY_PATHFINDING, "debugDraw", Configurations.Pathfinding.pathfindingDebugDraw,
          "Render.Pathfinding.results for debugging purposes (SSP only)").getBoolean();
        Configurations.Pathfinding.pathfindingDebugVerbosity = config.get(CATEGORY_PATHFINDING, "debugVerbosity", Configurations.Pathfinding.pathfindingDebugVerbosity,
          "Debug output verbosity of.Pathfinding.(0=none, 1=results, 2=live work)").getInt();
        Configurations.Pathfinding.pathfindingMaxThreadCount = config.get(CATEGORY_PATHFINDING, "maxThreads", Configurations.Pathfinding.pathfindingMaxThreadCount,
          "Maximum number of threads to use for.Pathfinding.").getInt();
    }

    /**
     * load configuration related to the citizen's.Names.
     */
    private static synchronized void loadNamesConfigurations()
    {
        Configurations.Names.maleFirstNames = config.get(CATEGORY_NAMES, "maleFirstNames", Configurations.Names.maleFirstNames,
          "Male First Names").getStringList();
        Configurations.Names.femaleFirstNames = config.get(CATEGORY_NAMES, "femaleFirstNames", Configurations.Names.femaleFirstNames,
          "Female First Names").getStringList();
        Configurations.Names.lastNames = config.get(CATEGORY_NAMES, "lastNames", Configurations.Names.lastNames,
          "Last Names").getStringList();
    }

    /**
     * load configuration related to the request system.
     */
    private static synchronized void loadRequestSystemConfigurations()
    {
        Configurations.RequestSystem.maximalRetries = config.get(CATEGORY_REQUEST, "maximalRetries", Configurations.RequestSystem.maximalRetries,
          "The maximal amount of tries that the request system will perform for retryable requests. Higher increases server load.").getInt();
        Configurations.RequestSystem.minimalBuildingsToGather = config.get(CATEGORY_REQUEST, "minimalBuildingsToGather", Configurations.RequestSystem.minimalBuildingsToGather,
          "The minimal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse.").getInt();
        Configurations.RequestSystem.maximalBuildingsToGather = config.get(CATEGORY_REQUEST, "maximalBuildingsToGather", Configurations.RequestSystem.maximalBuildingsToGather,
          "The maximal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse.").getInt();
        Configurations.RequestSystem.delayBetweenRetries = config.get(CATEGORY_REQUEST, "delayBetweenRetries", Configurations.RequestSystem.delayBetweenRetries,
          "The amount of ticks between retries of the request system for retryable requests. Lower increases server load.").getInt();
        Configurations.RequestSystem.enableDebugLogging = config.get(CATEGORY_REQUEST, "enableDebugLogging", Configurations.RequestSystem.enableDebugLogging,
          "Should the request system print out debug information? Useful in case of malfunctioning of set system.").getBoolean();
        Configurations.RequestSystem.creativeResolve = config.get(CATEGORY_REQUEST, "creativeResolve", Configurations.RequestSystem.creativeResolve,
          "Should the request system creatively resolve (if possible) when the player is required to resolve a request.").getBoolean();
    }

    /**
     * This event will be called when the config gets changed through
     * the in-game GUI.
     *
     * @param eventArgs An instance to the event.
     */
    @SubscribeEvent
    public void onConfigChanged(final OnConfigChangedEvent eventArgs)
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
