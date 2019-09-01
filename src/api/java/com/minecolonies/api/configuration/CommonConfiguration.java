package com.minecolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.*;
import static com.minecolonies.api.util.constant.Constants.MAX_BARBARIAN_HORDE_SIZE;

/**
 * Mod common configuration.
 * Loaded everywhere, not synced.
 */
public class CommonConfiguration extends AbstractConfiguration
{
    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Gameplay settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue initialCitizenAmount;
    public final ForgeConfigSpec.BooleanValue builderPlaceConstructionTape;
    public final ForgeConfigSpec.BooleanValue playerGetsGuidebookOnFirstJoin;
    public final ForgeConfigSpec.BooleanValue allowInfiniteSupplyChests;
    public final ForgeConfigSpec.BooleanValue allowInfiniteColonies;
    public final ForgeConfigSpec.BooleanValue allowOtherDimColonies;
    public final ForgeConfigSpec.IntValue citizenRespawnInterval;
    public final ForgeConfigSpec.IntValue maxCitizenPerColony;
    public final ForgeConfigSpec.BooleanValue builderInfiniteResources;
    public final ForgeConfigSpec.BooleanValue limitToOneWareHousePerColony;
    public final ForgeConfigSpec.IntValue builderBuildBlockDelay;
    public final ForgeConfigSpec.IntValue blockMiningDelayModifier;
    public final ForgeConfigSpec.IntValue maxBlocksCheckedByBuilder;
    public final ForgeConfigSpec.IntValue chatFrequency;
    public final ForgeConfigSpec.BooleanValue enableInDevelopmentFeatures;
    public final ForgeConfigSpec.BooleanValue alwaysRenderNameTag;
    public final ForgeConfigSpec.DoubleValue growthModifier;
    public final ForgeConfigSpec.BooleanValue workersAlwaysWorkInRain;
    public final ForgeConfigSpec.BooleanValue sendEnteringLeavingMessages;
    public final ForgeConfigSpec.BooleanValue allowPlayerSchematics;
    public final ForgeConfigSpec.IntValue allowGlobalNameChanges;
    public final ForgeConfigSpec.BooleanValue holidayFeatures;
    public final ForgeConfigSpec.IntValue updateRate;
    public final ForgeConfigSpec.IntValue dirtFromCompost;
    public final ForgeConfigSpec.IntValue luckyBlockChance;
    public final ForgeConfigSpec.BooleanValue fixOrphanedChunks;
    public final ForgeConfigSpec.BooleanValue restrictBuilderUnderground;
    public final ForgeConfigSpec.DoubleValue fisherSpongeChance;
    public final ForgeConfigSpec.IntValue minThLevelToTeleport;
    public final ForgeConfigSpec.BooleanValue suggestBuildToolPlacement;
    public final ForgeConfigSpec.DoubleValue foodModifier;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Command settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue teleportBuffer;
    public final ForgeConfigSpec.IntValue opLevelForServer;
    public final ForgeConfigSpec.IntValue autoDeleteColoniesInHours;
    public final ForgeConfigSpec.BooleanValue autoDestroyColonyBlocks;
    public final ForgeConfigSpec.BooleanValue canPlayerUseRTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseColonyTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseHomeTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseCitizenInfoCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseListCitizensCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerRespawnCitizensCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseShowColonyInfoCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseKillCitizensCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseAddOfficerCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseDeleteColonyCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseRefreshColonyCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseBackupCommand;
    public final ForgeConfigSpec.IntValue numberOfAttemptsForSafeTP;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Claim settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue     workingRangeTownHallChunks;
    public final ForgeConfigSpec.IntValue     minTownHallPadding;
    public final ForgeConfigSpec.IntValue     townHallPadding;
    public final ForgeConfigSpec.IntValue     townHallPaddingChunk;
    public final ForgeConfigSpec.BooleanValue restrictColonyPlacement;
    public final ForgeConfigSpec.BooleanValue enableDynamicColonySizes;
    public final ForgeConfigSpec.IntValue     maxDistanceFromWorldSpawn;
    public final ForgeConfigSpec.IntValue     minDistanceFromWorldSpawn;
    public final ForgeConfigSpec.BooleanValue protectVillages;

    /*  ------------------------------------------------------------------------- *
     *  ------------------- ######## Combat Settings ######## ------------------- *
     *  ------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue doBarbariansSpawn;
    public final ForgeConfigSpec.IntValue barbarianHordeDifficulty;
    public final ForgeConfigSpec.IntValue spawnBarbarianSize;
    public final ForgeConfigSpec.IntValue maxBarbarianSize;
    public final ForgeConfigSpec.BooleanValue doBarbariansBreakThroughWalls;
    public final ForgeConfigSpec.IntValue averageNumberOfNightsBetweenRaids;
    public final ForgeConfigSpec.IntValue minimumNumberOfNightsBetweenRaids;
    // TODO: change to true over time
    public final ForgeConfigSpec.BooleanValue mobAttackCitizens;
    public final ForgeConfigSpec.BooleanValue citizenCallForHelp;
    public final ForgeConfigSpec.BooleanValue rangerEnchants;
    public final ForgeConfigSpec.DoubleValue rangerDamageMult;
    public final ForgeConfigSpec.DoubleValue knightDamageMult;
    public final ForgeConfigSpec.DoubleValue guardHealthMult;
    public final ForgeConfigSpec.BooleanValue pvp_mode;
    public final ForgeConfigSpec.IntValue daysUntilPirateshipsDespawn;
    public final ForgeConfigSpec.IntValue maxYForBarbarians;

    /*  ----------------------------------------------------------------------------- *
     *  ------------------- ######## Permission Settings ######## ------------------- *
     *  ----------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue enableColonyProtection;
    public final ForgeConfigSpec.BooleanValue turnOffExplosionsInColonies;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> specialPermGroup;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> freeToInteractBlocks;
    public final ForgeConfigSpec.IntValue secondsBetweenPermissionMessages;

    /*  -------------------------------------------------------------------------------- *
     *  ------------------- ######## Compatibility Settings ######## ------------------- *
     *  -------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> extraOres;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> guardResourceLocations;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> configListStudyItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> listOfCompostableItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> luckyBlocks;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> luckyOres;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> crusherProduction ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> sifterMeshes;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> siftableBlocks;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> sifterDrops;
    public final ForgeConfigSpec.IntValue dynamicTreeHarvestSize;

    /*  ------------------------------------------------------------------------------ *
     *  ------------------- ######## Pathfinding Settings ######## ------------------- *
     *  ------------------------------------------------------------------------------ */

    public final ForgeConfigSpec.BooleanValue pathfindingDebugDraw;
    public final ForgeConfigSpec.IntValue pathfindingDebugVerbosity;
    public final ForgeConfigSpec.IntValue pathfindingMaxThreadCount;
    public final ForgeConfigSpec.IntValue pathfindingMaxNodes;

    /*  --------------------------------------------------------------------------------- *
     *  ------------------- ######## Request System Settings ######## ------------------- *
     *  --------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue enableDebugLogging;
    public final ForgeConfigSpec.IntValue maximalRetries;
    public final ForgeConfigSpec.IntValue delayBetweenRetries;
    public final ForgeConfigSpec.IntValue maximalBuildingsToGather;
    public final ForgeConfigSpec.IntValue minimalBuildingsToGather;
    public final ForgeConfigSpec.BooleanValue creativeResolve;
    public final ForgeConfigSpec.BooleanValue canPlayerUseResetCommand;

    /**
     * Builds common configuration.
     *
     * @param builder config builder
     */
    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");

        initialCitizenAmount = defineInteger(builder, "initialcitizenamount", 4, 1, 10);
        builderPlaceConstructionTape = defineBoolean(builder, "builderplaceconstructiontape", true);
        playerGetsGuidebookOnFirstJoin = defineBoolean(builder, "playergetsguidebookonfirstjoin", true);
        allowInfiniteSupplyChests = defineBoolean(builder, "allowinfinitesupplychests", false);
        allowInfiniteColonies = defineBoolean(builder, "allowinfinitecolonies", false);
        allowOtherDimColonies = defineBoolean(builder, "allowotherdimcolonies", false);
        citizenRespawnInterval = defineInteger(builder, "citizenrespawninterval", 60, CITIZEN_RESPAWN_INTERVAL_MIN, CITIZEN_RESPAWN_INTERVAL_MAX);
        maxCitizenPerColony = defineInteger(builder, "maxcitizenpercolony", 50, 4, 500);
        builderInfiniteResources = defineBoolean(builder, "builderinfiniteresources", false);
        limitToOneWareHousePerColony = defineBoolean(builder, "limittoonewarehousepercolony", true);
        builderBuildBlockDelay = defineInteger(builder, "builderbuildblockdelay", 15, 1, 500);
        blockMiningDelayModifier = defineInteger(builder, "blockminingdelaymodifier", 500, 1, 10000);
        maxBlocksCheckedByBuilder = defineInteger(builder, "maxblockscheckedbybuilder", 1000, 1000, 100000);
        chatFrequency = defineInteger(builder, "chatfrequency", 30, 1, 100);
        enableInDevelopmentFeatures = defineBoolean(builder, "enableindevelopmentfeatures", false);
        alwaysRenderNameTag = defineBoolean(builder, "alwaysrendernametag", true);
        growthModifier = defineDouble(builder, "growthmodifier", 1, 1, 100);
        workersAlwaysWorkInRain = defineBoolean(builder, "workersalwaysworkinrain", false);
        sendEnteringLeavingMessages = defineBoolean(builder, "sendenteringleavingmessages", true);
        allowPlayerSchematics = defineBoolean(builder, "allowplayerschematics", false);
        allowGlobalNameChanges = defineInteger(builder, "allowglobalnamechanges", 1, -1, 1);
        holidayFeatures = defineBoolean(builder, "holidayfeatures", true);
        updateRate = defineInteger(builder, "updaterate", 1, 0, 100);
        dirtFromCompost = defineInteger(builder, "dirtfromcompost", 1, 0, 100);
        luckyBlockChance = defineInteger(builder, "luckyblockchance", 1, 0, 100);
        fixOrphanedChunks = defineBoolean(builder, "fixorphanedchunks", false);
        restrictBuilderUnderground = defineBoolean(builder, "restrictbuilderunderground", true);
        fisherSpongeChance = defineDouble(builder, "fisherspongechance", 0.1, 0, 100);
        minThLevelToTeleport = defineInteger(builder, "minthleveltoteleport", 3, 0, 5);
        suggestBuildToolPlacement = defineBoolean(builder, "suggestbuildtoolplacement", true);
        foodModifier = defineDouble(builder, "foodmodifier", 1.0, 0, 100);


        swapToCategory(builder, "commands");

        teleportBuffer = defineInteger(builder, "teleportbuffer", 120, 30, 99999);
        opLevelForServer = defineInteger(builder, "oplevelforserver", 3, 0, 3);
        autoDeleteColoniesInHours = defineInteger(builder, "autodeletecoloniesinhours", 0, 168, 10000);
        autoDestroyColonyBlocks = defineBoolean(builder, "autodestroycolonyblocks", true);
        canPlayerUseRTPCommand = defineBoolean(builder, "canplayerusertpcommand", true);
        canPlayerUseColonyTPCommand = defineBoolean(builder, "canplayerusecolonytpcommand", false);
        canPlayerUseHomeTPCommand = defineBoolean(builder, "canplayerusehometpcommand", true);
        canPlayerUseCitizenInfoCommand = defineBoolean(builder, "canplayerusecitizeninfocommand", true);
        canPlayerUseListCitizensCommand = defineBoolean(builder, "canplayeruselistcitizenscommand", true);
        canPlayerRespawnCitizensCommand = defineBoolean(builder, "canplayerrespawncitizenscommand", true);
        canPlayerUseShowColonyInfoCommand = defineBoolean(builder, "canplayeruseshowcolonyinfocommand", true);
        canPlayerUseKillCitizensCommand = defineBoolean(builder, "canplayerusekillcitizenscommand", true);
        canPlayerUseAddOfficerCommand = defineBoolean(builder, "canplayeruseaddofficercommand", true);
        canPlayerUseDeleteColonyCommand = defineBoolean(builder, "canplayerusedeletecolonycommand", true);
        canPlayerUseRefreshColonyCommand = defineBoolean(builder, "canplayeruserefreshcolonycommand", false);
        canPlayerUseBackupCommand = defineBoolean(builder, "canplayerusebackupcommand", false);
        numberOfAttemptsForSafeTP = defineInteger(builder, "numberofattemptsforsafetp", 4, 1, 10);


        swapToCategory(builder, "claims");

        /*workingRangeTownHall = defineInteger(builder, "workingrangetownhall", 0, 0, 0);*/

        workingRangeTownHallChunks = defineInteger(builder, "workingrangetownhallchunks", 8, 1, 50);
        minTownHallPadding = defineInteger(builder, "mintownhallpadding", 3, 1, 200);
        townHallPadding = defineInteger(builder, "townhallpadding", 20, 1, 20000);
        townHallPaddingChunk = defineInteger(builder, "townhallpaddingchunk", 1, 1, 200);
        restrictColonyPlacement = defineBoolean(builder, "restrictcolonyplacement", false);
        enableDynamicColonySizes = defineBoolean(builder, "enabledynamiccolonysizes", false);
        maxDistanceFromWorldSpawn = defineInteger(builder, "maxdistancefromworldspawn", 8000, 1000, 100000);
        minDistanceFromWorldSpawn = defineInteger(builder, "mindistancefromworldspawn", 512, 1, 1000);
        protectVillages = defineBoolean(builder, "protectvillages", false);

        swapToCategory(builder, "combat");

        doBarbariansSpawn = defineBoolean(builder, "dobarbariansspawn", true);
        barbarianHordeDifficulty = defineInteger(builder,  "barbarianhordedifficulty", 5, MIN_BARBARIAN_DIFFICULTY, MAX_BARBARIAN_DIFFICULTY);
        spawnBarbarianSize = defineInteger(builder,  "spawnbarbariansize", 5, MIN_SPAWN_BARBARIAN_HORDE_SIZE, MAX_SPAWN_BARBARIAN_HORDE_SIZE);
        maxBarbarianSize = defineInteger(builder, "maxBarbarianSize", 20, MIN_BARBARIAN_HORDE_SIZE, MAX_BARBARIAN_HORDE_SIZE);
        doBarbariansBreakThroughWalls = defineBoolean(builder, "dobarbariansbreakthroughwalls", true);
        averageNumberOfNightsBetweenRaids = defineInteger(builder, "averagenumberofnightsbetweenraids", 3, 1, 10);
        minimumNumberOfNightsBetweenRaids = defineInteger(builder, "minimumnumberofnightsbetweenraids", 1, 1, 30);
        mobAttackCitizens = defineBoolean(builder, "mobattackcitizens", false);
        citizenCallForHelp = defineBoolean(builder, "citizencallforhelp", true);
        rangerEnchants = defineBoolean(builder, "rangerenchants", true);
        rangerDamageMult = defineDouble(builder,  "rangerdamagemult", 1.0, 0.1, 5.0);
        knightDamageMult = defineDouble(builder,  "knightdamagemult", 1.0, 0.1, 5.0);
        guardHealthMult = defineDouble(builder,  "guardhealthmult", 1.0, 0.1, 5.0);
        pvp_mode = defineBoolean(builder,   "pvp_mode", false);
        daysUntilPirateshipsDespawn = defineInteger(builder,  "daysuntilpirateshipsdespawn", 3, 1, 10);
        maxYForBarbarians = defineInteger(builder,  "maxyforbarbarians", 200, 1, 500);

        swapToCategory(builder, "permissions");

        enableColonyProtection = defineBoolean(builder,  "enablecolonyprotection", true);
        turnOffExplosionsInColonies = defineBoolean(builder, "turnoffexplosionsincolonies", true);
        specialPermGroup = defineList(builder, "specialpermgroup",
          Arrays.asList
                   ("_Raycoms_" ),
          s -> s instanceof String);
        freeToInteractBlocks = defineList(builder, "freetointeractblocks",
          Arrays.asList
                   ("dirt",
                     "0 0 0" ),
          s -> s instanceof String);
        secondsBetweenPermissionMessages = defineInteger(builder, "secondsBetweenPermissionMessages",  30, 1, 1000);


        swapToCategory(builder, "compatibility");

        extraOres = defineList(builder, "extraOres",
          Arrays.asList
                   ("minestuck:ore_cruxite",
                     "minestuck:ore_uranium" ),
          s -> s instanceof String);
        guardResourceLocations = defineList(builder, "guardresourcelocations",
          Arrays.asList
                   ("minecraft:slime",
                     "tconstruct:blueslime" ),
          s -> s instanceof String);
        configListStudyItems = defineList(builder, "configliststudyitems",
          Arrays.asList
                   ("minecraft:paper;400;100"),
          s -> s instanceof String);
        listOfCompostableItems = defineList(builder, "listOfCompostableItems",
          Arrays.asList
                   ("minecraft:rotten_flesh",
                     "minecraft:tallgrass",
                     "minecraft:brown_mushroom",
                     "minecraft:red_mushroom",
                     "minecraft:rose_bush",
                     "minecraft:feather",
                     "saplings",
                     "small_flowers"),
          s -> s instanceof String);
        luckyBlocks = defineList(builder, "luckyblocks",
          Arrays.asList
                   ("minecraft:stone",
                     "minecraft:cobblestone"),
          s -> s instanceof String);
        luckyOres = defineList(builder, "luckyblocks",
          Arrays.asList
                   ("minecraft:coal_ore!64",
                     "minecraft:iron_ore!32",
                     "minecraft:gold_ore!16",
                     "minecraft:redstone_ore!8",
                     "minecraft:lapis_ore!4",
                     "minecraft:diamond_ore!2",
                     "minecraft:emerald_ore!1"),
          s -> s instanceof String);
        crusherProduction = defineList(builder, "crusherproduction",
          Arrays.asList
                   ("minecraft:cobblestone!minecraft:gravel",
                     "minecraft:gravel!minecraft:sand",
                     "minecraft:sand!minecraft:clay" ),
          s -> s instanceof String);
        sifterMeshes = defineList(builder, "siftermeshes",
          Arrays.asList
                   ("minecraft:string,0",
                     "minecraft:flint,0.1",
                     "minecraft:iron_ingot,0.1",
                     "minecraft:diamond,0.1" ),
          s -> s instanceof String);
        siftableBlocks = defineList(builder, "siftableblocks",
          Arrays.asList
                   ("minecraft:dirt",
                     "minecraft:sand",
                     "minecraft:gravel",
                     "minecraft:soul_sand"),
          s -> s instanceof String);

        sifterDrops = defineList(builder, "sifterdrops",
          Arrays.asList
                   (//Dirt with String mesh
                     "0,0,minecraft:wheat_seeds,25",
                     "0,0,minecraft:oak_sapling,1",
                     "0,0,minecraft:birch_sapling,1",
                     "0,0,minecraft:spruce_sapling,1",
                     "0,0,minecraft:jungle_sapling,1",

                     //Dirt with flfinal ForgeConfigSpec.IntValue mesh
                     "0,1,minecraft:wheat_seeds,50",
                     "0,1,minecraft:oak_sapling,5",
                     "0,1,minecraft:birch_sapling,5",
                     "0,1,minecraft:spruce_sapling,5",
                     "0,1,minecraft:jungle_sapling,5",
                     "0,1,minecraft:carrot:0,1",
                     "0,1,minecraft:potato:0,1",

                     //Dirt with iron mesh
                     "0,2,minecraft:wheat_seeds,50",
                     "0,2,minecraft:oak_sapling,10",
                     "0,2,minecraft:birch_sapling,10",
                     "0,2,minecraft:spruce_sapling,10",
                     "0,2,minecraft:jungle_sapling,10",
                     "0,2,minecraft:pumpkin_seeds:0,1",
                     "0,2,minecraft:melon_seeds:0,1",
                     "0,2,minecraft:beetroot_seeds:0,1",
                     "0,2,minecraft:carrot,1",
                     "0,2,minecraft:potato,1",
                     "0,2,minecraft:dark_oak_sapling,1",
                     "0,2,minecraft:acacia_sapling,1",

                     //Dirt with diamond mesh
                     "0,3,minecraft:wheat_seeds,25",
                     "0,3,minecraft:oak_sapling,10",
                     "0,3,minecraft:birch_sapling,10",
                     "0,3,minecraft:spruce_sapling,10",
                     "0,3,minecraft:jungle_sapling,10",
                     "0,3,minecraft:pumpkin_seeds:0,5",
                     "0,3,minecraft:melon_seeds:0,5",
                     "0,3,minecraft:beetroot_seeds:0,5",
                     "0,3,minecraft:carrot:0,5",
                     "0,3,minecraft:potato:0,5",
                     "0,3,minecraft:dark_oak_sapling,5",
                     "0,3,minecraft:acacia_sapling,5",

                     //Sand with string mesh
                     "1,0,minecraft:cactus,2.5",
                     "1,0,minecraft:sugar_cane,2.5",

                     //Sand with flfinal ForgeConfigSpec.IntValue mesh
                     "1,1,minecraft:cactus,5",
                     "1,1,minecraft:sugar_cane,5",
                     "1,1,minecraft:gold_nugget,5",

                     //Sand with iron mesh
                     "1,2,minecraft:cactus,10",
                     "1,2,minecraft:sugar_cane,10",
                     "1,2,minecraft:cocoa_beans,10",
                     "1,2,minecraft:gold_nugget,10",

                     //Sand with diamond mesh
                     "1,3,minecraft:cactus,15",
                     "1,3,minecraft:sugar_cane,15",
                     "1,3,minecraft:cocoa_beans,15",
                     "1,3,minecraft:gold_nugget,15",

                     //Gravel with string mesh
                     "2,0,minecraft:iron_nugget,5",
                     "2,0,minecraft:flint,5",
                     "2,0,minecraft:coal,5",

                     //Gravel with flint mesh
                     "2,1,minecraft:redstone,10",
                     "2,1,minecraft:iron_nugget,10",
                     "2,1,minecraft:flint,10",
                     "2,1,minecraft:coal,10",

                     //Gravel with iron mesh
                     "2,2,minecraft:redstone,15",
                     "2,2,minecraft:iron_nugget,15",
                     "2,2,minecraft:coal,15",
                     "2,2,minecraft:lapis_lazuli,5",
                     "2,2,minecraft:iron_ingot,1",
                     "2,2,minecraft:gold_ingot,1",
                     "2,2,minecraft:emerald,1",
                     "2,2,minecraft:diamond,1",

                     //Gravel with diamond mesh
                     "2,3,minecraft:redstone,20",
                     "2,3,minecraft:coal,20",
                     "2,3,minecraft:lapis_lazuli,10",
                     "2,3,minecraft:iron_ingot,2.5",
                     "2,3,minecraft:gold_ingot,2.5",
                     "2,3,minecraft:emerald,2.5",
                     "2,3,minecraft:diamond,2.5",

                     //Soulsand with string mesh
                     "3,0,minecraft:nether_wart,5",
                     "3,0,minecraft:quartz,5",

                     //Soulsand with flint mesh
                     "3,1,minecraft:nether_wart,10",
                     "3,1,minecraft:quartz,10",
                     "3,1,minecraft:glowstone_dust,5",

                     //Soulsand with iron mesh
                     "3,2,minecraft:nether_wart,10",
                     "3,2,minecraft:quartz,10",
                     "3,2,minecraft:glowstone_dust,10",
                     "3,2,minecraft:blaze_powder,1",
                     "3,2,minecraft:magma_cream,1",

                     //Soulsand with diamond mesh
                     "3,3,minecraft:nether_wart,15",
                     "3,3,minecraft:quartz,15",
                     "3,3,minecraft:glowstone_dust,15",
                     "3,3,minecraft:blaze_powder,5",
                     "3,3,minecraft:magma_cream,5",
                     "3,3,minecraft:player_head,5"),
          s -> s instanceof String);
        dynamicTreeHarvestSize = defineInteger(builder,  "dynamictreeharvestsize", 5, 1, 5);


        swapToCategory(builder, "pathfinding");

        pathfindingDebugDraw = defineBoolean(builder,  "pathfindingdebugdraw", false);
        pathfindingDebugVerbosity = defineInteger(builder,  "pathfindingdebugverbosity", 0, 0, 10);
        pathfindingMaxThreadCount = defineInteger(builder,  "pathfindingmaxthreadcount", 2, 1, 10);
        pathfindingMaxNodes = defineInteger(builder,  "pathfindingmaxnodes", 5000, 1, 10000);


        swapToCategory(builder, "requestSystem");

        enableDebugLogging = defineBoolean(builder,  "enabledebuglogging", false);
        maximalRetries = defineInteger(builder,  "maximalretries", 3, 1, 10);
        delayBetweenRetries = defineInteger(builder,  "delaybetweenretries", 1200, 30, 10000);
        maximalBuildingsToGather = defineInteger(builder,  "maximalbuildingstogather", 6, 1, 50);
        minimalBuildingsToGather = defineInteger(builder,  "minimalbuildingstogather", 3, 1, 50);
        creativeResolve = defineBoolean(builder,  "creativeresolve", false);
        canPlayerUseResetCommand = defineBoolean(builder,  "canplayeruseresetcommand", false);


        finishCategory(builder);
    }
}
