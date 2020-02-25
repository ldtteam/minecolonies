package com.minecolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.*;

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
    public final ForgeConfigSpec.BooleanValue disableCitizenVoices;
    public final ForgeConfigSpec.IntValue diseaseModifier;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Command settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue     teleportBuffer;
    public final ForgeConfigSpec.IntValue     opLevelForServer;
    public final ForgeConfigSpec.IntValue     autoDeleteColoniesInHours;
    public final ForgeConfigSpec.BooleanValue autoDestroyColonyBlocks;
    public final ForgeConfigSpec.BooleanValue canPlayerUseRTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseColonyTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseAllyTHTeleport;
    public final ForgeConfigSpec.BooleanValue canPlayerUseHomeTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseShowColonyInfoCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseKillCitizensCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseAddOfficerCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseDeleteColonyCommand;
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
    public final ForgeConfigSpec.BooleanValue officersReceiveAdvancements;

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
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> listOfPlantables;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> enchantments;
    public final ForgeConfigSpec.DoubleValue                         enchanterExperienceMultiplier;
    public final ForgeConfigSpec.IntValue                            dynamicTreeHarvestSize;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> diseases;


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

    /*  --------------------------------------------------------------------------------- *
     *  ------------------- ######## Research Settings ######## ------------------- *
     *  --------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> tactictraining;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> improvedswords;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> squiretraining;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> knighttraining;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> captaintraining;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> captainoftheguard;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> improvedbows;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> tickshot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> multishot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> rapidshot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> masterbowman;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> avoidance ;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> parry ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> repost ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> duelist ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> provost ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> masterswordsman;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> dodge ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> improveddodge;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> evasion ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> improvedevasion;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> agilearcher;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> improvedleather ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> boiledleather;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> ironskin ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> ironarmour;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> steelarmour;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> diamondskin ;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> regeneration;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> avoid;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> evade ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> flee;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> hotfoot;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> feint;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fear ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> retreat;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fullretreat;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> accuracy ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> quickdraw;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> powerattack ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> cleave;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> mightycleave;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> whirlwind;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> preciseshot ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> penetratingshot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> piercingshot ;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> woundingshot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> deadlyaim;

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
        disableCitizenVoices = defineBoolean(builder, "disablecitizenvoices", false);
        diseaseModifier = defineInteger(builder, "diseasemodifier", 5, 1, 100);

        swapToCategory(builder, "commands");

        teleportBuffer = defineInteger(builder, "teleportbuffer", 120, 30, 99999);
        opLevelForServer = defineInteger(builder, "oplevelforserver", 3, 0, 3);
        autoDeleteColoniesInHours = defineInteger(builder, "autodeletecoloniesinhours", 0, 168, 10000);
        autoDestroyColonyBlocks = defineBoolean(builder, "autodestroycolonyblocks", true);
        canPlayerUseRTPCommand = defineBoolean(builder, "canplayerusertpcommand", true);
        canPlayerUseColonyTPCommand = defineBoolean(builder, "canplayerusecolonytpcommand", false);
        canPlayerUseAllyTHTeleport = defineBoolean(builder, "canplayeruseallytownhallteleport", true);
        canPlayerUseHomeTPCommand = defineBoolean(builder, "canplayerusehometpcommand", true);
        canPlayerUseShowColonyInfoCommand = defineBoolean(builder, "canplayeruseshowcolonyinfocommand", true);
        canPlayerUseKillCitizensCommand = defineBoolean(builder, "canplayerusekillcitizenscommand", true);
        canPlayerUseAddOfficerCommand = defineBoolean(builder, "canplayeruseaddofficercommand", true);
        canPlayerUseDeleteColonyCommand = defineBoolean(builder, "canplayerusedeletecolonycommand", true);
        numberOfAttemptsForSafeTP = defineInteger(builder, "numberofattemptsforsafetp", 4, 1, 10);


        swapToCategory(builder, "claims");

        /*workingRangeTownHall = defineInteger(builder, "workingrangetownhall", 0, 0, 0);*/

        workingRangeTownHallChunks = defineInteger(builder, "workingrangetownhallchunks", 8, 1, 50);
        minTownHallPadding = defineInteger(builder, "mintownhallpadding", 3, 1, 200);
        townHallPadding = defineInteger(builder, "townhallpadding", 20, 1, 20000);
        townHallPaddingChunk = defineInteger(builder, "townhallpaddingchunk", 1, 1, 200);
        restrictColonyPlacement = defineBoolean(builder, "restrictcolonyplacement", false);
        enableDynamicColonySizes = defineBoolean(builder, "enabledynamiccolonysizes", true);
        maxDistanceFromWorldSpawn = defineInteger(builder, "maxdistancefromworldspawn", 8000, 1000, 100000);
        minDistanceFromWorldSpawn = defineInteger(builder, "mindistancefromworldspawn", 512, 1, 1000);
        protectVillages = defineBoolean(builder, "protectvillages", false);
        officersReceiveAdvancements = defineBoolean(builder, "officersreceiveadvancements", true);

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

        listOfPlantables = defineList(builder, "listofplantables",
          Arrays.asList
                   ("minecraft:sunflower",
                     "minecraft:lilac",
                     "minecraft:rose_bush",
                     "minecraft:peony",
                     "minecraft:tall_grass",
                     "minecraft:large_fern",
                     "minecraft:fern",
                     "minecraft:sugar_cane",
                     "minecraft:cactus",
                     "small_flowers"
                   ),
          s -> s instanceof String);

        enchantments = defineList(builder, "enchantments",
          Arrays.asList
                   (
                     "1,minecraft:aqua_affinity,1,50",
                     "1,minecraft:bane_of_arthropods,1,50",
                     "1,minecraft:blast_protection,1,50",
                     "1,minecraft:depth_strider,1,50",
                     "1,minecraft:feather_falling,1,50",
                     "1,minecraft:fire_aspect,1,50",
                     "1,minecraft:fire_protection,1,50",
                     "1,minecraft:flame,1,50",
                     "1,minecraft:frost_walker,1,50",
                     "1,minecraft:knockback,1,50",
                     "1,minecraft:looting,1,50",
                     "1,minecraft:power,1,50",
                     "1,minecraft:projectile_protection,1,50",
                     "1,minecraft:protection,1,50",
                     "1,minecraft:punch,1,50",
                     "1,minecraft:respiration,1,50",
                     "1,minecraft:sharpness,1,50",
                     "1,minecraft:smite,1,50",
                     "1,minecraft:sweeping,1,50",
                     "1,minecraft:unbreaking,1,50",

                     "2,minecraft:aqua_affinity,2,25",
                     "2,minecraft:bane_of_arthropods,2,25",
                     "2,minecraft:blast_protection,2,25",
                     "2,minecraft:depth_strider,2,25",
                     "2,minecraft:feather_falling,2,25",
                     "2,minecraft:fire_aspect,2,25",
                     "2,minecraft:fire_protection,2,25",
                     "2,minecraft:flame,2,25",
                     "2,minecraft:frost_walker,2,25",
                     "2,minecraft:knockback,2,25",
                     "2,minecraft:looting,2,25",
                     "2,minecraft:power,2,25",
                     "2,minecraft:projectile_protection,2,25",
                     "2,minecraft:protection,2,25",
                     "2,minecraft:punch,2,25",
                     "2,minecraft:respiration,2,25",
                     "2,minecraft:sharpness,2,25",
                     "2,minecraft:smite,2,25",
                     "2,minecraft:sweeping,2,25",
                     "2,minecraft:unbreaking,2,25",

                     "3,minecraft:aqua_affinity,3,15",
                     "3,minecraft:bane_of_arthropods,3,15",
                     "3,minecraft:blast_protection,3,15",
                     "3,minecraft:depth_strider,3,15",
                     "3,minecraft:feather_falling,3,15",
                     "3,minecraft:fire_aspect,3,15",
                     "3,minecraft:fire_protection,3,15",
                     "3,minecraft:flame,3,15",
                     "3,minecraft:frost_walker,3,15",
                     "3,minecraft:knockback,3,15",
                     "3,minecraft:looting,3,15",
                     "3,minecraft:power,3,15",
                     "3,minecraft:projectile_protection,3,15",
                     "3,minecraft:protection,3,15",
                     "3,minecraft:punch,3,15",
                     "3,minecraft:respiration,3,15",
                     "3,minecraft:sharpness,3,15",
                     "3,minecraft:smite,3,15",
                     "3,minecraft:sweeping,3,15",
                     "3,minecraft:unbreaking,3,15",

                     "4,minecraft:aqua_affinity,4,5",
                     "4,minecraft:bane_of_arthropods,4,5",
                     "4,minecraft:blast_protection,4,5",
                     "4,minecraft:depth_strider,4,5",
                     "4,minecraft:feather_falling,4,5",
                     "4,minecraft:fire_aspect,4,5",
                     "4,minecraft:fire_protection,4,5",
                     "4,minecraft:flame,4,5",
                     "4,minecraft:frost_walker,4,5",
                     "4,minecraft:infinity,1,5",
                     "4,minecraft:knockback,4,5",
                     "4,minecraft:looting,4,5",
                     "4,minecraft:power,4,5",
                     "4,minecraft:projectile_protection,4,5",
                     "4,minecraft:protection,4,5",
                     "4,minecraft:punch,4,5",
                     "4,minecraft:respiration,4,5",
                     "4,minecraft:sharpness,4,5",
                     "4,minecraft:smite,4,5",
                     "4,minecraft:sweeping,4,5",
                     "4,minecraft:unbreaking,4,5",

                     "5,minecraft:aqua_affinity,5,1",
                     "5,minecraft:bane_of_arthropods,5,1",
                     "5,minecraft:blast_protection,5,1",
                     "5,minecraft:depth_strider,5,1",
                     "5,minecraft:feather_falling,5,1",
                     "5,minecraft:fire_aspect,5,1",
                     "5,minecraft:fire_protection,5,1",
                     "5,minecraft:flame,5,1",
                     "5,minecraft:frost_walker,5,1",
                     "5,minecraft:infinity,1,1",
                     "5,minecraft:knockback,5,1",
                     "5,minecraft:looting,5,1",
                     "5,minecraft:mending,1,1",
                     "5,minecraft:power,5,1",
                     "5,minecraft:projectile_protection,5,1",
                     "5,minecraft:protection,5,1",
                     "5,minecraft:punch,5,1",
                     "5,minecraft:respiration,5,1",
                     "5,minecraft:sharpness,5,1",
                     "5,minecraft:smite,5,1",
                     "5,minecraft:sweeping,5,1",
                     "5,minecraft:unbreaking,5,1"
                   ),
          s -> s instanceof String);

        enchanterExperienceMultiplier = defineDouble(builder,  "enchanterexperiencemultiplier", 2, 1, 10);

        dynamicTreeHarvestSize = defineInteger(builder,  "dynamictreeharvestsize", 5, 1, 5);

        diseases = defineList(builder, "diseases",
          Arrays.asList("Influenza,100,minecraft:carrot,minecraft:potato",
                        "Measles,10,minecraft:dandelion,minecraft:kelp,minecraft:poppy",
                        "Smallpox,1,minecraft:honeycomb,minecraft:golden_apple"),
          s -> s instanceof String);

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

        swapToCategory(builder, "research");

        tactictraining = defineList(builder, "tactictraining",
          Collections.singletonList("minecraft:iron_block*3"),
          s -> s instanceof String);

        improvedswords = defineList(builder, "improvedswords",
          Collections.singletonList("minecraft:iron_block*6"),
          s -> s instanceof String);
        squiretraining = defineList(builder, "squiretraining",
          Collections.singletonList("minecraft:shield*5"),
          s -> s instanceof String);
        knighttraining = defineList(builder, "knighttraining",
          Collections.singletonList("minecraft:shield*10"),
          s -> s instanceof String);
        captaintraining = defineList(builder, "captaintraining",
          Collections.singletonList("minecraft:shield*25"),
          s -> s instanceof String);
        captainoftheguard = defineList(builder, "captainoftheguard",
          Collections.singletonList("minecraft:shield*64"),
          s -> s instanceof String);

        improvedbows = defineList(builder, "improvedbows",
          Collections.singletonList("minecraft:iron_block*6"),
          s -> s instanceof String);
        tickshot = defineList(builder, "tickshot",
          Collections.singletonList("minecraft:bow*5"),
          s -> s instanceof String);
        multishot = defineList(builder, "multishot",
          Collections.singletonList("minecraft:bow*10"),
          s -> s instanceof String);
        rapidshot = defineList(builder, "rapidshot",
          Collections.singletonList("minecraft:bow*25"),
          s -> s instanceof String);
        masterbowman = defineList(builder, "masterbowman",
          Collections.singletonList("minecraft:bow*64"),
          s -> s instanceof String);

        avoidance = defineList(builder, "avoidance",
          Collections.singletonList("minecraft:iron_block*3"),
          s -> s instanceof String);

        parry = defineList(builder, "parry",
          Collections.singletonList("minecraft:iron_ingot*16"),
          s -> s instanceof String);
        repost = defineList(builder, "repost",
          Collections.singletonList("minecraft:iron_ingot*32"),
          s -> s instanceof String);
        duelist = defineList(builder, "duelist",
          Collections.singletonList("minecraft:iron_ingot*64"),
          s -> s instanceof String);
        provost = defineList(builder, "provost",
          Collections.singletonList("minecraft:diamond*16"),
          s -> s instanceof String);
        masterswordsman = defineList(builder, "masterswordsman",
          Collections.singletonList("minecraft:diamond*64"),
          s -> s instanceof String);

        dodge = defineList(builder, "dodge",
          Collections.singletonList("minecraft:leather*16"),
          s -> s instanceof String);
        improveddodge = defineList(builder, "improveddodge",
          Collections.singletonList("minecraft:leather*32"),
          s -> s instanceof String);
        evasion = defineList(builder, "evasion",
          Collections.singletonList("minecraft:leather*64"),
          s -> s instanceof String);
        improvedevasion = defineList(builder, "improvedevasion",
          Collections.singletonList("minecraft:diamond*16"),
          s -> s instanceof String);
        agilearcher = defineList(builder, "agilearcher",
          Collections.singletonList("minecraft:diamond*64"),
          s -> s instanceof String);

        this.improvedleather = defineList(builder, "improvedleather",
          Collections.singletonList("minecraft:leather*32"),
          s -> s instanceof String);
        this.boiledleather = defineList(builder, "boiledleather",
          Collections.singletonList("minecraft:leather*64"),
          s -> s instanceof String);
        this.ironskin = defineList(builder, "ironskin",
          Collections.singletonList("minecraft:iron_ingot*16"),
          s -> s instanceof String);
        this.ironarmour = defineList(builder, "ironarmour",
          Collections.singletonList("minecraft:iron_ingot*32"),
          s -> s instanceof String);
        this.steelarmour = defineList(builder, "steelarmour",
          Collections.singletonList("minecraft:iron_ingot*64"),
          s -> s instanceof String);
        this.diamondskin = defineList(builder, "diamondskin",
          Collections.singletonList("minecraft:diamond*64"),
          s -> s instanceof String);

        this.regeneration = defineList(builder, "regeneration",
          Collections.singletonList("minecraft:emerald*1"),
          s -> s instanceof String);

        this.feint = defineList(builder, "feint",
          Collections.singletonList("minecraft:emerald*8"),
          s -> s instanceof String);
        this.fear = defineList(builder, "fear",
          Collections.singletonList("minecraft:emerald*16"),
          s -> s instanceof String);
        this.retreat = defineList(builder, "retreat",
          Collections.singletonList("minecraft:emerald*32"),
          s -> s instanceof String);
        this.fullretreat = defineList(builder, "fullretreat",
          Collections.singletonList("minecraft:emerald*64"),
          s -> s instanceof String);

        this.avoid = defineList(builder, "avoid",
          Collections.singletonList("minecraft:emerald*8"),
          s -> s instanceof String);
        this.evade = defineList(builder, "dodge",
          Collections.singletonList("minecraft:emerald*16"),
          s -> s instanceof String);
        this.flee = defineList(builder, "dodge",
          Collections.singletonList("minecraft:emerald*32"),
          s -> s instanceof String);
        this.hotfoot = defineList(builder, "dodge",
          Collections.singletonList("minecraft:emerald*64"),
          s -> s instanceof String);

        this.accuracy = defineList(builder, "accuracy",
          Collections.singletonList("minecraft:iron_ingot*1"),
          s -> s instanceof String);

        this.quickdraw = defineList(builder, "quickdraw",
          Collections.singletonList("minecraft:iron_block*2"),
          s -> s instanceof String);
        this.powerattack = defineList(builder, "powerattack",
          Collections.singletonList("minecraft:iron_block*4"),
          s -> s instanceof String);
        this.cleave = defineList(builder, "cleave",
          Collections.singletonList("minecraft:iron_block*8"),
          s -> s instanceof String);
        this.mightycleave = defineList(builder, "mightycleave",
          Collections.singletonList("minecraft:iron_block*16"),
          s -> s instanceof String);
        this.whirlwind = defineList(builder, "whirlwind",
          Collections.singletonList("minecraft:iron_block*32"),
          s -> s instanceof String);

        this.preciseshot = defineList(builder, "preciseshot",
          Collections.singletonList("minecraft:flint*16"),
          s -> s instanceof String);
        this.penetratingshot = defineList(builder, "penetratingshot",
          Collections.singletonList("minecraft:flint*32"),
          s -> s instanceof String);
        this.piercingshot = defineList(builder, "piercingshot",
          Collections.singletonList("minecraft:flint*64"),
          s -> s instanceof String);
        this.woundingshot = defineList(builder, "woundingshot",
          Collections.singletonList("minecraft:flint*128"),
          s -> s instanceof String);
        this.deadlyaim = defineList(builder, "deadlyaim",
          Collections.singletonList("minecraft:flint*256"),
          s -> s instanceof String);

        finishCategory(builder);
    }
}
