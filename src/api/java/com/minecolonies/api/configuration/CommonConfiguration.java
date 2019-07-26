package com.minecolonies.api.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
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

    public final ForgeConfigSpec.BooleanValue supplyChests;

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

    @Config.Comment("Time until a next teleport can be executed (in seconds). [Default: 120]")
    public final ForgeConfigSpec.IntValue teleportBuffer = 120;

    @Config.Comment("Which level counts as op level on the server. [Default: 3]")
    public final ForgeConfigSpec.IntValue opLevelForServer = 3;

    @Config.Comment("Sets the amount of hours until a colony will be deleted after not seeing it's mayor, set to zero to disable. [Default: 0]")
    public final ForgeConfigSpec.IntValue autoDeleteColoniesInHours = 0;

    @Config.Comment("Sets weither or not Colony structures are destroyed automatically. [Default: true]")
    public final ForgeConfigSpec.BooleanValue autoDestroyColonyBlocks = true;

    @Config.Comment("Should the player be allowed to use the '/mc rtp' command? [Default: true]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseRTPCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony teleport' command? [Default: false]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseColonyTPCommand = false;

    @Config.Comment("Should the player be allowed to use the '/mc home' command? [Default: true]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseHomeTPCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc citizens info' command? [Default: true]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseCitizenInfoCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc citizens list' command? [Default: true]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseListCitizensCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc citizens respawn' command? [Default: true]")
    public final ForgeConfigSpec.BooleanValue canPlayerRespawnCitizensCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony info' command? [Default: true]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseShowColonyInfoCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc citizens kill' command? [Default: true]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseKillCitizensCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony addOfficer' command? [Default: true]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseAddOfficerCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony delete' command? [Default: true]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseDeleteColonyCommand = true;

    @Config.Comment("Should the player be allowed to use the '/mc colony refresh' command? [Default: false]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseRefreshColonyCommand = false;

    @Config.Comment("Should the player be allowed to use the '/mc backup' command? [Default: false]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseBackupCommand = false;

    @Config.Comment("Amount of attempts to find a save rtp. [Default: 4]")
    public final ForgeConfigSpec.IntValue numberOfAttemptsForSafeTP = 4;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Claim settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    @Config.Comment("Max distance a colony can claim a chunk from the center, 0 if disable maximum.  [Default: 0]")
    public final ForgeConfigSpec.IntValue workingRangeTownHall = 0;

    @Config.Comment("Colony size (radius in chunks around central colony chunk). Only for the static mode. [Default: 8]")
    public final ForgeConfigSpec.IntValue workingRangeTownHallChunks = 8;

    @Config.Comment("The minimum distances between town halls for dynamic colony sizes (used as default initial claim too). [Default: 3]")
    public final ForgeConfigSpec.IntValue minTownHallPadding = 3;

    @Config.Comment("Padding between colonies  - deprecated, don't use.  [Default: 20]")
    public final ForgeConfigSpec.IntValue townHallPadding = 20;

    @Config.Comment("Padding between colonies in chunks. [Default: 1]")
    public final ForgeConfigSpec.IntValue townHallPaddingChunk = 1;

    @Config.Comment("Should the min/max distance from spawn also affect colony placement? [Default: false]")
    public final ForgeConfigSpec.BooleanValue restrictColonyPlacement = false;

    @Config.Comment("Should the colony have a fixed radius or should it be dynamic. [Default: false]")
    public final ForgeConfigSpec.BooleanValue enableDynamicColonySizes = false;

    @Config.Comment("Max distance from world spawn. [Default: 8000]")
    public final ForgeConfigSpec.IntValue maxDistanceFromWorldSpawn = 8000;

    @Config.Comment("Min distance from world spawn. [Default: 512]")
    public final ForgeConfigSpec.IntValue minDistanceFromWorldSpawn = 512;

    @Config.Comment("Should players be allowed to build their colonies over existing villages? [Default: false]")
    public final ForgeConfigSpec.BooleanValue protectVillages = false;

    /*  ------------------------------------------------------------------------- *
     *  ------------------- ######## Combat Settings ######## ------------------- *
     *  ------------------------------------------------------------------------- */

    @Config.Comment("Whether or not to spawn barbarians. [Default: true]")
    public final ForgeConfigSpec.BooleanValue doBarbariansSpawn = true;

    @Config.RangeInt(min = (MIN_BARBARIAN_DIFFICULTY), max = MAX_BARBARIAN_DIFFICULTY)
    @Config.Comment("The difficulty setting for barbarians. [Default: 5]")
    public final ForgeConfigSpec.IntValue barbarianHordeDifficulty = 5;

    @Config.RangeInt(min = (MIN_SPAWN_BARBARIAN_HORDE_SIZE), max = MAX_SPAWN_BARBARIAN_HORDE_SIZE)
    @Config.Comment("The spawn size of a barbarian horde. [Default: 5]")
    public final ForgeConfigSpec.IntValue spawnBarbarianSize = 5;

    @Config.RangeInt(min = (MIN_BARBARIAN_HORDE_SIZE), max = MAX_BARBARIAN_HORDE_SIZE)
    @Config.Comment("The max size of a barbarian horde. [Default: 20]")
    public final ForgeConfigSpec.IntValue maxBarbarianSize = 20;

    @Config.Comment("Whether or not to barbarians can break, scale, bridge obstacles. [Default: true]")
    public final ForgeConfigSpec.BooleanValue doBarbariansBreakThroughWalls = true;

    @Config.Comment("The average amount of nights between raids. [Default: 3]")
    public final ForgeConfigSpec.IntValue averageNumberOfNightsBetweenRaids = 3;

    @Config.Comment("The minimum number of nights between raids. [Default: 1]")
    public final ForgeConfigSpec.IntValue minimumNumberOfNightsBetweenRaids = 1;

    // TODO: change to true over time
    @Config.Comment("Should Mobs attack citizens? [Default: false")
    public final ForgeConfigSpec.BooleanValue mobAttackCitizens = false;

    @Config.Comment("Should Citizens call guards for help when attacked? default:true")
    public final ForgeConfigSpec.BooleanValue citizenCallForHelp = true;

    @Config.Comment("Should Guard Rangers benefit from Power/Smite/Bane of Arthropods enchants? [Default: true]")
    public final ForgeConfigSpec.BooleanValue rangerEnchants = true;

    @Config.Comment("Damage multiplier for Ranger Guards. [Default: 1.0]")
    public final ForgeConfigSpec.DoubleValue rangerDamageMult = 1.0;

    @Config.Comment("Damage multiplier for Knight Guards. [Default: 1.0]")
    public final ForgeConfigSpec.DoubleValue knightDamageMult = 1.0;

    @Config.Comment("Health multiplier for all Guards. [Default: 1.0]")
    public final ForgeConfigSpec.DoubleValue guardHealthMult = 1.0;

    @Config.Comment("Turn on Minecolonies pvp mode, attention (colonies can be destroyed and can be griefed under certain conditions). [Default: false]")
    public final ForgeConfigSpec.BooleanValue pvp_mode = false;

    @Config.Comment("Days until the pirate ships despawn again. [Default: 3]")
    public final ForgeConfigSpec.IntValue daysUntilPirateshipsDespawn = 3;

    @Config.Comment("Max Y level for Barbarians to spawn. [Default: 200]")
    public final ForgeConfigSpec.IntValue maxYForBarbarians = 200;

    /*  ----------------------------------------------------------------------------- *
     *  ------------------- ######## Permission Settings ######## ------------------- *
     *  ----------------------------------------------------------------------------- */

    @Config.Comment("Should the colony protection be enabled? [Default: true]")
    public final ForgeConfigSpec.BooleanValue enableColonyProtection = true;

    @Config.Comment("Independent from the colony protection, should explosions be turned off? [Default: true]")
    public final ForgeConfigSpec.BooleanValue turnOffExplosionsInColonies = true;

    @Config.Comment("Players who have special permission (Patreons for example)")
    public String[] specialPermGroup = new String[]
                                         {
                                           "_Raycoms_"
                                         };

    @Config.Comment("Blocks players should be able to final ForgeConfigSpec.IntValueeract with in any colony (Ex vending machines)")
    public String[] freeToInteractBlocks = new String[]
                                             {
                                               "block:dirt",
                                               "0 0 0"
                                             };

    @Config.Comment("Seconds between permission messages. [Default: 30]")
    public final ForgeConfigSpec.IntValue secondsBetweenPermissionMessages = 30;

    /*  -------------------------------------------------------------------------------- *
     *  ------------------- ######## Compatibility Settings ######## ------------------- *
     *  -------------------------------------------------------------------------------- */

    @Config.Comment("Ores for the miner to mine that aren't autodetected")
    public String[] extraOres = new String[]
                                  {
                                    "minestuck:ore_cruxite",
                                    "minestuck:ore_uranium",
                                  };

    @Config.Comment("ResourceLocations for extra entities for the GuardHut's list. \n"
                      + "once done you'll need to recalculate the list."
                      + "EntityMob's already calculated in list.")
    public String[] guardResourceLocations = new String[]
                                               {
                                                 "minecraft:slime",
                                                 "tconstruct:blueslime"
                                               };

    @Config.Comment("List of items the Students in the library can use. \n"
                      + "Format: itemname;SkillIncreasePCT[100-1000];BreakPCT[0-100] \n"
                      + "Example: minecraft:paper;400;100 \n"
                      + "Which adds minecraft Paper with a 400%(4x) increased chance to skillup and a 100% chance to be used up during the try to skillup")
    public final String[] configListStudyItems = new String[]
                                                   {
                                                     "minecraft:paper;400;100"
                                                   };

    @Config.Comment("The items and item-tags that the composter can use to produce compost.")
    public String[] listOfCompostableItems = new String[]
                                               {
                                                 "minecraft:rotten_flesh",
                                                 "minecraft:tallgrass",
                                                 "minecraft:yellow_flower",
                                                 "minecraft:red_flower",
                                                 "minecraft:brown_mushroom",
                                                 "minecraft:red_mushroom",
                                                 "minecraft:final ForgeConfigSpec.DoubleValue_plant",
                                                 "minecraft:feather",
                                                 "food",
                                                 "seed",
                                                 "treeSapling"
                                               };

    @Config.Comment("The blocks where the miner has a chance to get a random ore.")
    public String[] luckyBlocks = new String[]
                                    {
                                      "minecraft:stone",
                                      "minecraft:cobblestone",
                                    };

    @Config.Comment("The random ores the miner can get separated by ! for rarity")
    public String[] luckyOres = new String[]
                                  {
                                    "minecraft:coal_ore!64",
                                    "minecraft:iron_ore!32",
                                    "minecraft:gold_ore!16",
                                    "minecraft:redstone_ore!8",
                                    "minecraft:lapis_ore!4",
                                    "minecraft:diamond_ore!2",
                                    "minecraft:emerald_ore!1"
                                  };

    @Config.Comment("What the crusher can produce at the cost of 2:1")
    public String[] crusherProduction = new String[]
                                          {
                                            "minecraft:cobblestone!minecraft:gravel",
                                            "minecraft:gravel!minecraft:sand",
                                            "minecraft:sand!minecraft:clay"
                                          };

    @Config.Comment("The different meshes which can be bought in the building with durability")
    public String[] sifterMeshes = new String[]
                                     {
                                       "minecraft:string,0",
                                       "minecraft:flfinal ForgeConfigSpec.IntValue,0.1",
                                       "minecraft:iron_ingot,0.1",
                                       "minecraft:diamond,0.1"
                                     };

    @Config.Comment("The blocks which can be sifted for items")
    public String[] siftableBlocks = new String[]
                                       {
                                         "minecraft:dirt",
                                         "minecraft:sand",
                                         "minecraft:gravel",
                                         "minecraft:soul_sand",
                                       };

    @Config.Comment("The possible drops from sifting - keyBlock, keyMesh, item, probability")
    public String[] sifterDrops = new String[]
                                    {
                                      //Dirt with String mesh
                                      "0,0,minecraft:wheat_seeds,25",
                                      "0,0,minecraft:sapling:0,1",
                                      "0,0,minecraft:sapling:1,1",
                                      "0,0,minecraft:sapling:2,1",
                                      "0,0,minecraft:sapling:3,1",

                                      //Dirt with flfinal ForgeConfigSpec.IntValue mesh
                                      "0,1,minecraft:wheat_seeds,50",
                                      "0,1,minecraft:sapling:0,5",
                                      "0,1,minecraft:sapling:1,5",
                                      "0,1,minecraft:sapling:2,5",
                                      "0,1,minecraft:sapling:3,5",
                                      "0,1,minecraft:carrot:0,1",
                                      "0,1,minecraft:potato:0,1",

                                      //Dirt with iron mesh
                                      "0,2,minecraft:wheat_seeds,50",
                                      "0,2,minecraft:sapling:0,10",
                                      "0,2,minecraft:sapling:1,10",
                                      "0,2,minecraft:sapling:2,10",
                                      "0,2,minecraft:sapling:3,10",
                                      "0,2,minecraft:pumpkin_seeds:0,1",
                                      "0,2,minecraft:melon_seeds:0,1",
                                      "0,2,minecraft:beetroot_seeds:0,1",
                                      "0,2,minecraft:carrot:0,1",
                                      "0,2,minecraft:potato:0,1",
                                      "0,2,minecraft:sapling:4,1",
                                      "0,2,minecraft:sapling:5,1",

                                      //Dirt with diamond mesh
                                      "0,3,minecraft:wheat_seeds,25",
                                      "0,3,minecraft:sapling:0,10",
                                      "0,3,minecraft:sapling:1,10",
                                      "0,3,minecraft:sapling:2,10",
                                      "0,3,minecraft:sapling:3,10",
                                      "0,3,minecraft:pumpkin_seeds:0,5",
                                      "0,3,minecraft:melon_seeds:0,5",
                                      "0,3,minecraft:beetroot_seeds:0,5",
                                      "0,3,minecraft:carrot:0,5",
                                      "0,3,minecraft:potato:0,5",
                                      "0,3,minecraft:sapling:4,5",
                                      "0,3,minecraft:sapling:5,5",

                                      //Sand with string mesh
                                      "1,0,minecraft:cactus,2.5",
                                      "1,0,minecraft:reeds,2.5",

                                      //Sand with flfinal ForgeConfigSpec.IntValue mesh
                                      "1,1,minecraft:cactus,5",
                                      "1,1,minecraft:reeds,5",
                                      "1,1,minecraft:gold_nugget,5",

                                      //Sand with iron mesh
                                      "1,2,minecraft:cactus,10",
                                      "1,2,minecraft:reeds,10",
                                      "1,2,minecraft:dye:3,10",
                                      "1,2,minecraft:gold_nugget,10",

                                      //Sand with diamond mesh
                                      "1,3,minecraft:cactus,15",
                                      "1,3,minecraft:reeds,15",
                                      "1,3,minecraft:dye:3,15",
                                      "1,3,minecraft:gold_nugget,15",

                                      //Gravel with string mesh
                                      "2,0,minecraft:iron_nugget,5",
                                      "2,0,minecraft:flfinal ForgeConfigSpec.IntValue,5",
                                      "2,0,minecraft:coal,5",

                                      //Gravel with flfinal ForgeConfigSpec.IntValue mesh
                                      "2,1,minecraft:redstone,10",
                                      "2,1,minecraft:iron_nugget,10",
                                      "2,1,minecraft:flfinal ForgeConfigSpec.IntValue,10",
                                      "2,1,minecraft:coal,10",

                                      //Gravel with iron mesh
                                      "2,2,minecraft:redstone,15",
                                      "2,2,minecraft:iron_nugget,15",
                                      "2,2,minecraft:coal,15",
                                      "2,2,minecraft:dye:4,5",
                                      "2,2,minecraft:iron_ingot,1",
                                      "2,2,minecraft:gold_ingot,1",
                                      "2,2,minecraft:emerald,1",
                                      "2,2,minecraft:diamond,1",

                                      //Gravel with diamond mesh
                                      "2,3,minecraft:redstone,20",
                                      "2,3,minecraft:coal,20",
                                      "2,3,minecraft:dye:4,10",
                                      "2,3,minecraft:iron_ingot,2.5",
                                      "2,3,minecraft:gold_ingot,2.5",
                                      "2,3,minecraft:emerald,2.5",
                                      "2,3,minecraft:diamond,2.5",

                                      //Soulsand with string mesh
                                      "3,0,minecraft:nether_wart,5",
                                      "3,0,minecraft:quartz,5",

                                      //Soulsand with flfinal ForgeConfigSpec.IntValue mesh
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
                                      "3,3,minecraft:skull:3,5",
                                    };

    @Config.Comment("Harvest trunk-size for dynamic trees:1-8. [Default: 5]")
    public final ForgeConfigSpec.IntValue dynamicTreeHarvestSize = 5;

    /*  ------------------------------------------------------------------------------ *
     *  ------------------- ######## Pathfinding Settings ######## ------------------- *
     *  ------------------------------------------------------------------------------ */

    @Config.Comment("Draw pathfinding paths (might be laggy). [Default: false]")
    public final ForgeConfigSpec.BooleanValue pathfindingDebugDraw = false;

    @Config.Comment("Verbosity of pathfinding. [Default: 0]")
    public final ForgeConfigSpec.IntValue pathfindingDebugVerbosity = 0;

    @Config.Comment("Amount of additional threads to be used for pathfinding. [Default: 2]")
    public final ForgeConfigSpec.IntValue pathfindingMaxThreadCount = 2;

    @Config.Comment("Max amount of Nodes(positions) to map during pathfinding. Lowering increases performance, but might lead to pathing glitches. [Default: 5000]")
    public final ForgeConfigSpec.IntValue pathfindingMaxNodes = 5000;

    /*  --------------------------------------------------------------------------------- *
     *  ------------------- ######## Request System Settings ######## ------------------- *
     *  --------------------------------------------------------------------------------- */

    @Config.Comment("Should the request system prfinal ForgeConfigSpec.IntValue out debug information? Useful in case of malfunctioning of set system. [Default: false]")
    public final ForgeConfigSpec.BooleanValue enableDebugLogging = false;

    @Config.Comment("The maximal amount of tries that the request system will perform for retryable requests. Higher increases server load. [Default: 3]")
    public final ForgeConfigSpec.IntValue maximalRetries = 3;

    @Config.Comment("The amount of ticks between retries of the request system for retryable requests. Lower increases server load. [Default: 1200]")
    public final ForgeConfigSpec.IntValue delayBetweenRetries = 1200;

    @Config.Comment("The maximal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse. [Default: 6]")
    public final ForgeConfigSpec.IntValue maximalBuildingsToGather = 6;

    @Config.Comment("The minimal amount of buildings the Delivery Man should try to gather before attempting a drop off at the warehouse. [Default: 3]")
    public final ForgeConfigSpec.IntValue minimalBuildingsToGather = 3;

    @Config.Comment("Should the request system creatively resolve (if possible) when the player is required to resolve a request. [Default: false]")
    public final ForgeConfigSpec.BooleanValue creativeResolve = false;

    @Config.Comment("Should the player be allowed to use the '/mc colony rs reset' command? [Default: false]")
    public final ForgeConfigSpec.BooleanValue canPlayerUseResetCommand = false;

    /**
     * Builds common configuration.
     *
     * @param builder config builder
     */
    protected CommonConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");

        initialCitizenAmount = defineInteger(builder, "maxOperationsPerTick", 4, 1, 10);
        builderPlaceConstructionTape = defineBoolean(builder, "ignoreSchematicsFromJar", true);
        playerGetsGuidebookOnFirstJoin = defineBoolean(builder, "ignoreSchematicsFromJar", true);
        supplyChests = defineBoolean(builder, "ignoreSchematicsFromJar", true);
        allowInfiniteSupplyChests = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        allowInfiniteColonies = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        allowOtherDimColonies = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        citizenRespawnInterval = defineInteger(builder, "maxOperationsPerTick", 60, CITIZEN_RESPAWN_INTERVAL_MIN, CITIZEN_RESPAWN_INTERVAL_MAX);
        maxCitizenPerColony = defineInteger(builder, "maxOperationsPerTick", 50, 4, 500);
        builderInfiniteResources = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        limitToOneWareHousePerColony = defineBoolean(builder, "ignoreSchematicsFromJar", true);
        builderBuildBlockDelay = defineInteger(builder, "maxOperationsPerTick", 15, 1, 500);
        blockMiningDelayModifier = defineInteger(builder, "maxOperationsPerTick", 500, 1, 10000);
        maxBlocksCheckedByBuilder = defineInteger(builder, "maxOperationsPerTick", 1000, 1000, 100000);
        chatFrequency = defineInteger(builder, "maxOperationsPerTick", 30, 1, 100);
        enableInDevelopmentFeatures = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        alwaysRenderNameTag = defineBoolean(builder, "ignoreSchematicsFromJar", true);
        growthModifier = defineDouble(builder, "maxOperationsPerTick", 1, 1, 100);
        workersAlwaysWorkInRain = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        sendEnteringLeavingMessages = defineBoolean(builder, "ignoreSchematicsFromJar", true);
        allowPlayerSchematics = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        allowGlobalNameChanges = defineInteger(builder, "maxOperationsPerTick", 1, -1, 1);
        holidayFeatures = defineBoolean(builder, "ignoreSchematicsFromJar", true);
        updateRate = defineInteger(builder, "maxOperationsPerTick", 1, 0, 100);
        dirtFromCompost = defineInteger(builder, "maxOperationsPerTick", 1, 0, 100);
        luckyBlockChance = defineInteger(builder, "maxOperationsPerTick", 1, 0, 100);
        fixOrphanedChunks = defineBoolean(builder, "ignoreSchematicsFromJar", false);
        restrictBuilderUnderground = defineBoolean(builder, "ignoreSchematicsFromJar", true);
        fisherSpongeChance = defineDouble(builder, "maxCachedChanges", 0.1, 0, 100);
        minThLevelToTeleport = defineInteger(builder, "maxOperationsPerTick", 3, 0, 5);
        suggestBuildToolPlacement = defineBoolean(builder, "ignoreSchematicsFromJar", true);
        foodModifier = defineDouble(builder, "maxCachedChanges", 1.0, 0, 100);
        ForgeConfigSpec.ConfigValue<List<? extends String>> list = defineList(builder, "key", (ArrayList<String>)Arrays.asList("s1", "s2", "s3", "s4"), s -> s instanceof String);


        swapToCategory(builder, "commands");

        swapToCategory(builder, "combat");

        swapToCategory(builder, "permissions");

        swapToCategory(builder, "compatibility");

        swapToCategory(builder, "pathfinding");

        swapToCategory(builder, "requestSystem");

        finishcategory(builder);
    }
}