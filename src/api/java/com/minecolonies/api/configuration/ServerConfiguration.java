package com.minecolonies.api.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.minecolonies.api.util.constant.NameConstants;
import net.minecraftforge.common.ForgeConfigSpec;
import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Mod server configuration. Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Names settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue                        useMiddleInitial;
    public final ForgeConfigSpec.BooleanValue                        useEasternNameOrder;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> maleFirstNames;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> femaleFirstNames;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> lastNames;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Gameplay settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue     initialCitizenAmount;
    public final ForgeConfigSpec.BooleanValue builderPlaceConstructionTape;
    public final ForgeConfigSpec.BooleanValue allowInfiniteSupplyChests;
    public final ForgeConfigSpec.BooleanValue allowInfiniteColonies;
    public final ForgeConfigSpec.BooleanValue allowOtherDimColonies;
    public final ForgeConfigSpec.IntValue     citizenRespawnInterval;
    public final ForgeConfigSpec.IntValue     maxCitizenPerColony;
    public final ForgeConfigSpec.BooleanValue builderInfiniteResources;
    public final ForgeConfigSpec.BooleanValue limitToOneWareHousePerColony;
    public final ForgeConfigSpec.IntValue     builderBuildBlockDelay;
    public final ForgeConfigSpec.IntValue     blockMiningDelayModifier;
    public final ForgeConfigSpec.BooleanValue enableInDevelopmentFeatures;
    public final ForgeConfigSpec.BooleanValue alwaysRenderNameTag;
    public final ForgeConfigSpec.DoubleValue  growthModifier;
    public final ForgeConfigSpec.BooleanValue workersAlwaysWorkInRain;
    public final ForgeConfigSpec.BooleanValue sendEnteringLeavingMessages;
    public final ForgeConfigSpec.IntValue     allowGlobalNameChanges;
    public final ForgeConfigSpec.BooleanValue holidayFeatures;
    public final ForgeConfigSpec.IntValue     updateRate;
    public final ForgeConfigSpec.IntValue     dirtFromCompost;
    public final ForgeConfigSpec.IntValue     luckyBlockChance;
    public final ForgeConfigSpec.BooleanValue fixOrphanedChunks;
    public final ForgeConfigSpec.BooleanValue restrictBuilderUnderground;
    public final ForgeConfigSpec.DoubleValue  fisherSpongeChance;
    public final ForgeConfigSpec.DoubleValue  fisherPrismarineChance;
    public final ForgeConfigSpec.IntValue     minThLevelToTeleport;
    public final ForgeConfigSpec.BooleanValue suggestBuildToolPlacement;
    public final ForgeConfigSpec.DoubleValue  foodModifier;
    public final ForgeConfigSpec.IntValue     diseaseModifier;
    public final ForgeConfigSpec.BooleanValue forceLoadColony;
    public final ForgeConfigSpec.IntValue     badVisitorsChance;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Command settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue canPlayerUseRTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseColonyTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseAllyTHTeleport;
    public final ForgeConfigSpec.BooleanValue canPlayerUseHomeTPCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseShowColonyInfoCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseKillCitizensCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseAddOfficerCommand;
    public final ForgeConfigSpec.BooleanValue canPlayerUseDeleteColonyCommand;
    public final ForgeConfigSpec.IntValue     numberOfAttemptsForSafeTP;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Claim settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue     maxColonySize;
    public final ForgeConfigSpec.IntValue     minColonyDistance;
    public final ForgeConfigSpec.IntValue     initialColonySize;
    public final ForgeConfigSpec.BooleanValue restrictColonyPlacement;
    public final ForgeConfigSpec.IntValue     maxDistanceFromWorldSpawn;
    public final ForgeConfigSpec.IntValue     minDistanceFromWorldSpawn;
    public final ForgeConfigSpec.BooleanValue officersReceiveAdvancements;

    /*  ------------------------------------------------------------------------- *
     *  ------------------- ######## Combat Settings ######## ------------------- *
     *  ------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue doBarbariansSpawn;
    public final ForgeConfigSpec.IntValue     barbarianHordeDifficulty;
    public final ForgeConfigSpec.IntValue     spawnBarbarianSize;
    public final ForgeConfigSpec.IntValue     maxBarbarianSize;
    public final ForgeConfigSpec.BooleanValue doBarbariansBreakThroughWalls;
    public final ForgeConfigSpec.IntValue     averageNumberOfNightsBetweenRaids;
    public final ForgeConfigSpec.IntValue     minimumNumberOfNightsBetweenRaids;
    public final ForgeConfigSpec.BooleanValue shouldRaidersBreakDoors;
    public final ForgeConfigSpec.BooleanValue mobAttackCitizens;
    public final ForgeConfigSpec.BooleanValue citizenCallForHelp;
    public final ForgeConfigSpec.BooleanValue rangerEnchants;
    public final ForgeConfigSpec.DoubleValue  rangerDamageMult;
    public final ForgeConfigSpec.DoubleValue  knightDamageMult;
    public final ForgeConfigSpec.DoubleValue  guardHealthMult;
    public final ForgeConfigSpec.BooleanValue pvp_mode;
    public final ForgeConfigSpec.IntValue     daysUntilPirateshipsDespawn;
    public final ForgeConfigSpec.IntValue     maxYForBarbarians;

    /*  ----------------------------------------------------------------------------- *
     *  ------------------- ######## Permission Settings ######## ------------------- *
     *  ----------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue                        enableColonyProtection;
    public final ForgeConfigSpec.BooleanValue                        turnOffExplosionsInColonies;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> specialPermGroup;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> freeToInteractBlocks;
    public final ForgeConfigSpec.IntValue                            secondsBetweenPermissionMessages;

    /*  -------------------------------------------------------------------------------- *
     *  ------------------- ######## Compatibility Settings ######## ------------------- *
     *  -------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> enabledModTags;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> guardResourceLocations;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> configListStudyItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> configListRecruitmentItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> luckyOres;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> crusherProduction;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> sifterMeshes;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> listOfPlantables;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> enchantments;
    public final ForgeConfigSpec.DoubleValue                         enchanterExperienceMultiplier;
    public final ForgeConfigSpec.IntValue                            dynamicTreeHarvestSize;
    public final ForgeConfigSpec.IntValue                            fishingRodDurabilityAdjustT1;
    public final ForgeConfigSpec.IntValue                            fishingRodDurabilityAdjustT2;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> diseases;
    public final ForgeConfigSpec.BooleanValue                        debugInventories;


    /*  ------------------------------------------------------------------------------ *
     *  ------------------- ######## Pathfinding Settings ######## ------------------- *
     *  ------------------------------------------------------------------------------ */

    public final ForgeConfigSpec.IntValue     pathfindingDebugVerbosity;
    public final ForgeConfigSpec.IntValue     pathfindingMaxThreadCount;
    public final ForgeConfigSpec.IntValue     pathfindingMaxNodes;
    public final ForgeConfigSpec.IntValue     minimumRailsToPath;

    /*  --------------------------------------------------------------------------------- *
     *  ------------------- ######## Request System Settings ######## ------------------- *
     *  --------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue enableDebugLogging;
    public final ForgeConfigSpec.IntValue     maximalRetries;
    public final ForgeConfigSpec.IntValue     delayBetweenRetries;
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

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> avoidance;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> parry;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> repost;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> duelist;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> provost;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> masterswordsman;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> dodge;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> taunt;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> improveddodge;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> evasion;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> improvedevasion;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> agilearcher;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> improvedleather;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> boiledleather;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> ironskin;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> ironarmour;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> steelarmour;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> diamondskin;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> regeneration;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> avoid;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> evade;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> flee;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> hotfoot;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> feint;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fear;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> retreat;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fullretreat;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> accuracy;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> quickdraw;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> powerattack;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> cleave;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> mightycleave;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> whirlwind;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> preciseshot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> penetratingshot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> piercingshot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> woundingshot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> deadlyaim;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> higherlearning;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> morebooks;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> bookworm;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> bachelor;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> master;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> phd;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> nurture;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> hormones;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> puberty;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> growth;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> beanstalk;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> keen;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> outpost;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> hamlet;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> village;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> city;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> diligent;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> studious;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> scholarly;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> reflective;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> academic;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> rails;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> nimble;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> agile;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> swift;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> athlete;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> stamina;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> resistance;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> resilience;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> vitality;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fortitude;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> indefatigability;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> bandaid;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> healingcream;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> bandages;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> compress;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> cast;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> gourmand;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> gorger;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> stuffer;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> epicure;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> glutton;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> circus;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> festival;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> nightowl;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> spectacle;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> nightowl2;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> opera;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> theater;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> firstaid;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> firstaid2;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> livesaver;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> livesaver2;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> guardianangel;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> guardianangel2;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> whatyaneed;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> enhanced_gates1;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> enhanced_gates2;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> stringwork;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> thoselungs;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> rainbowheaven;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> veinminer;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> goodveins;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> richveins;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> amazingveins;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> motherlode;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> ability;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> skills;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> tools;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> seemsautomatic;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> madness;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> hittingiron;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> stonecake;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> strong;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> hardened;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> reinforced;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> steelbracing;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> diamondcoated;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> memoryaid;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> cheatsheet;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> recipebook;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> rtm;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> rainman;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> woodwork;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> sieving;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> space;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> capacity;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fullstock;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> theflintstones;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> rockingroll;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> hot;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> isthisredstone;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> redstonepowered;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> heavymachinery;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> whatisthisspeed;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> biodegradable;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> flowerpower;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> letitgrow;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> bonemeal;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> dung;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> compost;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> fertilizer;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> magiccompost;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> lightning;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> deeppockets;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> loaded;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> heavilyloaded;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> gildedhammer;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> doubletrouble;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> hotboots;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> pavetheroad;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> arrowuse;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> arrowpierce;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> knockbackaoe;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> knowtheend;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> morescrolls;

    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "names");

        useMiddleInitial = defineBoolean(builder, "usemiddleinitial", true);
        useEasternNameOrder = defineBoolean(builder, "useeasternnameorder", false);
        maleFirstNames = defineList(builder, "malefirstnames", Arrays.asList(NameConstants.maleFirstNames), s -> s instanceof String);
        femaleFirstNames = defineList(builder, "femalefirstnames", Arrays.asList(NameConstants.femaleFirstNames), s -> s instanceof String);
        lastNames = defineList(builder, "lastnames", Arrays.asList(NameConstants.lastNames), s -> s instanceof String);

        swapToCategory(builder, "gameplay");

        initialCitizenAmount = defineInteger(builder, "initialcitizenamount", 4, 1, 10);
        builderPlaceConstructionTape = defineBoolean(builder, "builderplaceconstructiontape", true);
        allowInfiniteSupplyChests = defineBoolean(builder, "allowinfinitesupplychests", false);
        allowInfiniteColonies = defineBoolean(builder, "allowinfinitecolonies", false);
        allowOtherDimColonies = defineBoolean(builder, "allowotherdimcolonies", false);
        citizenRespawnInterval = defineInteger(builder, "citizenrespawninterval", 60, CITIZEN_RESPAWN_INTERVAL_MIN, CITIZEN_RESPAWN_INTERVAL_MAX);
        maxCitizenPerColony = defineInteger(builder, "maxcitizenpercolony", 150, 4, 500);
        builderInfiniteResources = defineBoolean(builder, "builderinfiniteresources", false);
        limitToOneWareHousePerColony = defineBoolean(builder, "limittoonewarehousepercolony", true);
        builderBuildBlockDelay = defineInteger(builder, "builderbuildblockdelay", 15, 1, 500);
        blockMiningDelayModifier = defineInteger(builder, "blockminingdelaymodifier", 500, 1, 10000);
        enableInDevelopmentFeatures = defineBoolean(builder, "enableindevelopmentfeatures", false);
        alwaysRenderNameTag = defineBoolean(builder, "alwaysrendernametag", true);
        growthModifier = defineDouble(builder, "growthmodifier", 1, 1, 100);
        workersAlwaysWorkInRain = defineBoolean(builder, "workersalwaysworkinrain", false);
        sendEnteringLeavingMessages = defineBoolean(builder, "sendenteringleavingmessages", true);
        allowGlobalNameChanges = defineInteger(builder, "allowglobalnamechanges", 1, -1, 1);
        holidayFeatures = defineBoolean(builder, "holidayfeatures", true);
        updateRate = defineInteger(builder, "updaterate", 1, 1, 100);
        dirtFromCompost = defineInteger(builder, "dirtfromcompost", 1, 0, 100);
        luckyBlockChance = defineInteger(builder, "luckyblockchance", 1, 0, 100);
        fixOrphanedChunks = defineBoolean(builder, "fixorphanedchunks", false);
        restrictBuilderUnderground = defineBoolean(builder, "restrictbuilderunderground", true);
        fisherSpongeChance = defineDouble(builder, "fisherspongechance", 0.1, 0, 100);
        fisherPrismarineChance = defineDouble(builder, "fisherprismarinechance", 2.5, 0, 100);
        minThLevelToTeleport = defineInteger(builder, "minthleveltoteleport", 3, 0, 5);
        suggestBuildToolPlacement = defineBoolean(builder, "suggestbuildtoolplacement", true);
        foodModifier = defineDouble(builder, "foodmodifier", 1.0, 0.1, 100);
        diseaseModifier = defineInteger(builder, "diseasemodifier", 5, 1, 100);
        forceLoadColony = defineBoolean(builder, "forceloadcolony", false);
        badVisitorsChance = defineInteger(builder, "badvisitorchance", 2, 1, 100);

        swapToCategory(builder, "commands");
        
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

        maxColonySize = defineInteger(builder, "maxColonySize", 20, 1, 50);
        minColonyDistance = defineInteger(builder, "minColonyDistance", 8, 1, 200);
        initialColonySize = defineInteger(builder, "initialColonySize", 4, 1, 200);
        restrictColonyPlacement = defineBoolean(builder, "restrictcolonyplacement", false);
        maxDistanceFromWorldSpawn = defineInteger(builder, "maxdistancefromworldspawn", 8000, 1000, 100000);
        minDistanceFromWorldSpawn = defineInteger(builder, "mindistancefromworldspawn", 512, 1, 1000);
        officersReceiveAdvancements = defineBoolean(builder, "officersreceiveadvancements", true);

        swapToCategory(builder, "combat");

        doBarbariansSpawn = defineBoolean(builder, "dobarbariansspawn", true);
        barbarianHordeDifficulty = defineInteger(builder, "barbarianhordedifficulty", DEFAULT_BARBARIAN_DIFFICULTY, MIN_BARBARIAN_DIFFICULTY, MAX_BARBARIAN_DIFFICULTY);
        spawnBarbarianSize = defineInteger(builder, "spawnbarbariansize", 5, MIN_SPAWN_BARBARIAN_HORDE_SIZE, MAX_SPAWN_BARBARIAN_HORDE_SIZE);
        maxBarbarianSize = defineInteger(builder, "maxBarbarianSize", 80, MIN_BARBARIAN_HORDE_SIZE, MAX_BARBARIAN_HORDE_SIZE);
        doBarbariansBreakThroughWalls = defineBoolean(builder, "dobarbariansbreakthroughwalls", true);
        averageNumberOfNightsBetweenRaids = defineInteger(builder, "averagenumberofnightsbetweenraids", 12, 1, 50);
        minimumNumberOfNightsBetweenRaids = defineInteger(builder, "minimumnumberofnightsbetweenraids", 8, 1, 30);
        mobAttackCitizens = defineBoolean(builder, "mobattackcitizens", true);
        shouldRaidersBreakDoors = defineBoolean(builder, "shouldraiderbreakdoors", true);
        citizenCallForHelp = defineBoolean(builder, "citizencallforhelp", true);
        rangerEnchants = defineBoolean(builder, "rangerenchants", true);
        rangerDamageMult = defineDouble(builder, "rangerdamagemult", 1.0, 0.1, 5.0);
        knightDamageMult = defineDouble(builder, "knightdamagemult", 1.0, 0.1, 5.0);
        guardHealthMult = defineDouble(builder, "guardhealthmult", 1.0, 0.1, 5.0);
        pvp_mode = defineBoolean(builder, "pvp_mode", false);
        daysUntilPirateshipsDespawn = defineInteger(builder, "daysuntilpirateshipsdespawn", 3, 1, 10);
        maxYForBarbarians = defineInteger(builder, "maxyforbarbarians", 200, 1, 500);

        swapToCategory(builder, "permissions");

        enableColonyProtection = defineBoolean(builder, "enablecolonyprotection", true);
        turnOffExplosionsInColonies = defineBoolean(builder, "turnoffexplosionsincolonies", true);
        specialPermGroup = defineList(builder, "specialpermgroup",
          Arrays.asList
                   ("_Raycoms_"),
          s -> s instanceof String);
        freeToInteractBlocks = defineList(builder, "freetointeractblocks",
          Arrays.asList
                   ("dirt",
                     "0 0 0"),
          s -> s instanceof String);
        secondsBetweenPermissionMessages = defineInteger(builder, "secondsBetweenPermissionMessages", 30, 1, 1000);


        swapToCategory(builder, "compatibility");

        enabledModTags = defineList(builder, "enabledmodtags",
          Arrays.asList(
            "minecraft:wool",
            "minecraft:planks",
            "minecraft:stone_bricks",
            "minecraft:wooden_buttons",
            "minecraft:buttons",
            "minecraft:carpets",
            "minecraft:wooden_doors",
            "minecraft:wooden_stairs",
            "minecraft:wooden_slabs",
            "minecraft:wooden_fences",
            "minecraft:wooden_pressure_plates",
            "minecraft:wooden_trapdoors",
            "minecraft:saplings",
            "minecraft:logs",
            "minecraft:dark_oak_logs",
            "minecraft:oak_logs",
            "minecraft:birch_logs",
            "minecraft:acacia_logs",
            "minecraft:jungle_logs",
            "minecraft:spruce_logs",
            "minecraft:banners",
            "minecraft:sand",
            "minecraft:walls",
            "minecraft:anvil",
            "minecraft:leaves",
            "minecraft:small_flowers",
            "minecraft:beds",
            "minecraft:fishes",
            "minecraft:signs",
            "minecraft:music_discs",
            "minecraft:arrows",
            "forge:bones",
            "forge:bookshelves",
            "forge:chests/ender",
            "forge:chests/trapped",
            "forge:chests/wooden",
            "forge:cobblestone",
            "forge:crops/beetroot",
            "forge:crops/carrot",
            "forge:crops/nether_wart",
            "forge:crops/potato",
            "forge:crops/wheat",
            "forge:dusts/prismarine",
            "forge:dusts/redstone",
            "forge:dusts/glowstone",
            "forge:dyes",
            "forge:dyes/black",
            "forge:dyes/red",
            "forge:dyes/green",
            "forge:dyes/brown",
            "forge:dyes/blue",
            "forge:dyes/purple",
            "forge:dyes/cyan",
            "forge:dyes/light_gray",
            "forge:dyes/gray",
            "forge:dyes/pink",
            "forge:dyes/lime",
            "forge:dyes/yellow",
            "forge:dyes/light_blue",
            "forge:dyes/magenta",
            "forge:dyes/orange",
            "forge:dyes/white",
            "forge:eggs",
            "forge:ender_pearls",
            "forge:feathers",
            "forge:fence_gates",
            "forge:fence_gates/wooden",
            "forge:fences",
            "forge:fences/nether_brick",
            "forge:fences/wooden",
            "forge:gems/diamond",
            "forge:gems/emerald",
            "forge:gems/lapis",
            "forge:gems/prismarine",
            "forge:gems/quartz",
            "forge:glass",
            "forge:glass/black",
            "forge:glass/blue",
            "forge:glass/brown",
            "forge:glass/colorless",
            "forge:glass/cyan",
            "forge:glass/gray",
            "forge:glass/green",
            "forge:glass/light_blue",
            "forge:glass/light_gray",
            "forge:glass/lime",
            "forge:glass/magenta",
            "forge:glass/orange",
            "forge:glass/pink",
            "forge:glass/purple",
            "forge:glass/red",
            "forge:glass/white",
            "forge:glass/yellow",
            "forge:glass_panes",
            "forge:glass_panes/black",
            "forge:glass_panes/blue",
            "forge:glass_panes/brown",
            "forge:glass_panes/colorless",
            "forge:glass_panes/cyan",
            "forge:glass_panes/gray",
            "forge:glass_panes/green",
            "forge:glass_panes/light_blue",
            "forge:glass_panes/light_gray",
            "forge:glass_panes/lime",
            "forge:glass_panes/magenta",
            "forge:glass_panes/orange",
            "forge:glass_panes/pink",
            "forge:glass_panes/purple",
            "forge:glass_panes/red",
            "forge:glass_panes/white",
            "forge:glass_panes/yellow",
            "forge:gravel",
            "forge:gunpowder",
            "forge:heads",
            "forge:ingots/brick",
            "forge:ingots/gold",
            "forge:ingots/iron",
            "forge:ingots/nether_brick",
            "forge:leather",
            "forge:mushrooms",
            "forge:nether_stars",
            "forge:netherrack",
            "forge:nuggets/gold",
            "forge:nuggets/iron",
            "forge:obsidian",
            "forge:ores/coal",
            "forge:ores/diamond",
            "forge:ores/emerald",
            "forge:ores/gold",
            "forge:ores/iron",
            "forge:ores/lapis",
            "forge:ores/quartz",
            "forge:ores/redstone",
            "forge:rods/blaze",
            "forge:rods/wooden",
            "forge:sand",
            "forge:sand/colorless",
            "forge:sand/red",
            "forge:sandstone",
            "forge:seeds",
            "forge:seeds/beetroot",
            "forge:seeds/melon",
            "forge:seeds/pumpkin",
            "forge:seeds/wheat",
            "forge:slimeballs",
            "forge:stained_glass",
            "forge:stained_glass_panes",
            "forge:stone",
            "forge:storage_blocks/coal",
            "forge:storage_blocks/diamond",
            "forge:storage_blocks/emerald",
            "forge:storage_blocks/gold",
            "forge:storage_blocks/iron",
            "forge:storage_blocks/lapis",
            "forge:storage_blocks/quartz",
            "forge:storage_blocks/redstone",
            "forge:string"),
          s -> s instanceof String);

        guardResourceLocations = defineList(builder, "guardresourcelocations",
          Arrays.asList
                   ("minecraft:slime",
                     "tconstruct:blueslime"),
          s -> s instanceof String);
        configListStudyItems = defineList(builder, "configliststudyitems",
          Arrays.asList
                   ("minecraft:paper;400;100", "minecraft:book;600;10"),
          s -> s instanceof String);
        configListRecruitmentItems = defineList(builder, "configlistrecruitmentitems",
          Arrays.asList
                   ("minecraft:hay_block;2",
                     "minecraft:book;2",
                     "minecraft:enchanted_book;9",
                     "minecraft:diamond;9",
                     "minecraft:emerald;8",
                     "minecraft:baked_potato;1",
                     "minecraft:gold_ingot;2",
                     "minecraft:redstone;2",
                     "minecraft:lapis_lazuli;2",
                     "minecraft:cake;7",
                     "minecraft:sunflower;5",
                     "minecraft:honeycomb;6",
                     "minecraft:quartz;3"),
          s -> s instanceof String);
        luckyOres = defineList(builder, "luckyores",
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
                     "minecraft:sand!minecraft:clay"),
          s -> s instanceof String);
        sifterMeshes = defineList(builder, "siftermeshes",
          Arrays.asList
                   ("minecraft:string,0",
                     "minecraft:flint,0.1",
                     "minecraft:iron_ingot,0.1",
                     "minecraft:diamond,0.1"),
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
                     "biomesoplenty:small_flowers",
                     "minecraft:small_flowers"
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
                     "3,minecolonies:raider_damage_enchant,1,15",

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
                     "5,minecolonies:raider_damage_enchant,2,3",
                     "5,minecraft:unbreaking,5,1"
                   ),
          s -> s instanceof String);

        enchanterExperienceMultiplier = defineDouble(builder, "enchanterexperiencemultiplier", 2, 1, 10);

        dynamicTreeHarvestSize = defineInteger(builder, "dynamictreeharvestsize", 5, 1, 5);

        fishingRodDurabilityAdjustT2 = defineInteger(builder, "fishingroddurabilityadjustt2", 6, -249, 250000);
        fishingRodDurabilityAdjustT1 = defineInteger(builder, "fishingroddurabilityadjustt1", 22, -58, 250000);

        diseases = defineList(builder, "diseases",
          Arrays.asList("Influenza,100,minecraft:carrot,minecraft:potato",
            "Measles,10,minecraft:dandelion,minecraft:kelp,minecraft:poppy",
            "Smallpox,1,minecraft:honeycomb,minecraft:golden_apple"),
          s -> s instanceof String);

        debugInventories = defineBoolean(builder, "debuginventories", false);

        swapToCategory(builder, "pathfinding");

        pathfindingDebugVerbosity = defineInteger(builder, "pathfindingdebugverbosity", 0, 0, 10);
        minimumRailsToPath = defineInteger(builder, "minimumrailstopath", 5, 5, 100);
        pathfindingMaxThreadCount = defineInteger(builder, "pathfindingmaxthreadcount", 2, 1, 10);
        pathfindingMaxNodes = defineInteger(builder, "pathfindingmaxnodes", 5000, 1, 10000);

        swapToCategory(builder, "requestSystem");

        enableDebugLogging = defineBoolean(builder, "enabledebuglogging", false);
        maximalRetries = defineInteger(builder, "maximalretries", 3, 1, 10);
        delayBetweenRetries = defineInteger(builder, "delaybetweenretries", 1200, 30, 10000);
        creativeResolve = defineBoolean(builder, "creativeresolve", false);
        canPlayerUseResetCommand = defineBoolean(builder, "canplayeruseresetcommand", false);

        swapToCategory(builder, "research");

        tactictraining = defineList(builder, "tactictraining",
          Collections.singletonList("minecraft:iron_block*3"),
          s -> s instanceof String);

        improvedswords = defineList(builder, "improvedswords",
          Collections.singletonList("minecraft:iron_block*6"),
          s -> s instanceof String);
        squiretraining = defineList(builder, "squiretraining",
          Collections.singletonList("minecraft:shield*4"),
          s -> s instanceof String);
        knighttraining = defineList(builder, "knighttraining",
          Collections.singletonList("minecraft:shield*8"),
          s -> s instanceof String);
        captaintraining = defineList(builder, "captaintraining",
          Collections.singletonList("minecraft:shield*16"),
          s -> s instanceof String);
        captainoftheguard = defineList(builder, "captainoftheguard",
          Collections.singletonList("minecraft:shield*27"),
          s -> s instanceof String);

        improvedbows = defineList(builder, "improvedbows",
          Collections.singletonList("minecraft:iron_block*6"),
          s -> s instanceof String);
        tickshot = defineList(builder, "tickshot",
          Collections.singletonList("minecraft:bow*5"),
          s -> s instanceof String);
        multishot = defineList(builder, "multishot",
          Collections.singletonList("minecraft:bow*9"),
          s -> s instanceof String);
        rapidshot = defineList(builder, "rapidshot",
          Collections.singletonList("minecraft:bow*18"),
          s -> s instanceof String);
        masterbowman = defineList(builder, "masterbowman",
          Collections.singletonList("minecraft:bow*27"),
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
        this.evade = defineList(builder, "evade",
          Collections.singletonList("minecraft:emerald*16"),
          s -> s instanceof String);
        this.flee = defineList(builder, "flee",
          Collections.singletonList("minecraft:emerald*32"),
          s -> s instanceof String);
        this.hotfoot = defineList(builder, "hotfoot",
          Collections.singletonList("minecraft:emerald*64"),
          s -> s instanceof String);

        this.accuracy = defineList(builder, "accuracy",
          Collections.singletonList("minecraft:iron_ingot*16"),
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

        this.higherlearning = defineList(builder, "higherlearning",
          Collections.singletonList("minecraft:book*3"),
          s -> s instanceof String);

        this.morebooks = defineList(builder, "morebooks",
          Collections.singletonList("minecraft:book*6"),
          s -> s instanceof String);
        this.bookworm = defineList(builder, "bookworm",
          Collections.singletonList("minecraft:bookshelf*6"),
          s -> s instanceof String);
        this.bachelor = defineList(builder, "bachelor",
          Collections.singletonList("minecraft:bookshelf*12"),
          s -> s instanceof String);
        this.master = defineList(builder, "master",
          Collections.singletonList("minecraft:bookshelf*32"),
          s -> s instanceof String);
        this.phd = defineList(builder, "phd",
          Collections.singletonList("minecraft:bookshelf*64"),
          s -> s instanceof String);

        this.nurture = defineList(builder, "nurture",
          Collections.singletonList("minecraft:cooked_chicken*32"),
          s -> s instanceof String);
        this.hormones = defineList(builder, "hormones",
          Collections.singletonList("minecraft:cooked_chicken*64"),
          s -> s instanceof String);
        this.puberty = defineList(builder, "puberty",
          Collections.singletonList("minecraft:cooked_chicken*128"),
          s -> s instanceof String);
        this.growth = defineList(builder, "growth",
          Collections.singletonList("minecraft:cooked_chicken*256"),
          s -> s instanceof String);
        this.beanstalk = defineList(builder, "beanstalk",
          Collections.singletonList("minecraft:cooked_chicken*512"),
          s -> s instanceof String);

        this.keen = defineList(builder, "keen",
          Collections.singletonList("minecraft:book*3"),
          s -> s instanceof String);
        this.outpost = defineList(builder, "outpost",
          Collections.singletonList("minecraft:cooked_beef*64"),
          s -> s instanceof String);
        this.hamlet = defineList(builder, "hamlet",
          Collections.singletonList("minecraft:cooked_beef*128"),
          s -> s instanceof String);
        this.village = defineList(builder, "village",
          Collections.singletonList("minecraft:cooked_beef*256"),
          s -> s instanceof String);
        this.city = defineList(builder, "city",
          Collections.singletonList("minecraft:cooked_beef*512"),
          s -> s instanceof String);

        this.diligent = defineList(builder, "diligent",
          Collections.singletonList("minecraft:book*6"),
          s -> s instanceof String);
        this.studious = defineList(builder, "studious",
          Collections.singletonList("minecraft:book*12"),
          s -> s instanceof String);
        this.scholarly = defineList(builder, "scholarly",
          Collections.singletonList("minecraft:book*24"),
          s -> s instanceof String);
        this.reflective = defineList(builder, "reflective",
          Collections.singletonList("minecraft:book*48"),
          s -> s instanceof String);
        this.academic = defineList(builder, "academic",
          Collections.singletonList("minecraft:book*128"),
          s -> s instanceof String);

        this.rails = defineList(builder, "rails",
          Collections.singletonList("minecraft:rail*64"),
          s -> s instanceof String);
        this.nimble = defineList(builder, "nimble",
          Collections.singletonList("minecraft:rabbit_foot*1"),
          s -> s instanceof String);
        this.agile = defineList(builder, "agile",
          Collections.singletonList("minecraft:rabbit_foot*10"),
          s -> s instanceof String);
        this.swift = defineList(builder, "swift",
          Collections.singletonList("minecraft:rabbit_foot*32"),
          s -> s instanceof String);
        this.athlete = defineList(builder, "athlete",
          Collections.singletonList("minecraft:rabbit_foot*64"),
          s -> s instanceof String);

        this.stamina = defineList(builder, "stamina",
          Collections.singletonList("minecraft:carrot*1"),
          s -> s instanceof String);

        this.resistance = defineList(builder, "resistance",
          Collections.singletonList("minecraft:golden_apple*1"),
          s -> s instanceof String);
        this.resilience = defineList(builder, "resilience",
          Collections.singletonList("minecraft:golden_apple*8"),
          s -> s instanceof String);
        this.vitality = defineList(builder, "vitality",
          Collections.singletonList("minecraft:golden_apple*16"),
          s -> s instanceof String);
        this.fortitude = defineList(builder, "fortitude",
          Collections.singletonList("minecraft:golden_apple*32"),
          s -> s instanceof String);
        this.indefatigability = defineList(builder, "indefatigability",
          Collections.singletonList("minecraft:golden_apple*64"),
          s -> s instanceof String);

        this.bandaid = defineList(builder, "bandaid",
          Collections.singletonList("minecraft:golden_carrot*1"),
          s -> s instanceof String);
        this.healingcream = defineList(builder, "healingcream",
          Collections.singletonList("minecraft:golden_carrot*8"),
          s -> s instanceof String);
        this.bandages = defineList(builder, "bandages",
          Collections.singletonList("minecraft:golden_carrot*16"),
          s -> s instanceof String);
        this.compress = defineList(builder, "compress",
          Collections.singletonList("minecraft:golden_carrot*32"),
          s -> s instanceof String);
        this.cast = defineList(builder, "cast",
          Collections.singletonList("minecraft:golden_carrot*64"),
          s -> s instanceof String);

        this.gourmand = defineList(builder, "gourmand",
          Collections.singletonList("minecraft:cookie*32"),
          s -> s instanceof String);
        this.gorger = defineList(builder, "gorger",
          Collections.singletonList("minecraft:cookie*64"),
          s -> s instanceof String);
        this.stuffer = defineList(builder, "stuffer",
          Collections.singletonList("minecraft:cookie*128"),
          s -> s instanceof String);
        this.epicure = defineList(builder, "epicure",
          Collections.singletonList("minecraft:cookie*256"),
          s -> s instanceof String);
        this.glutton = defineList(builder, "glutton",
          Collections.singletonList("minecraft:cookie*512"),
          s -> s instanceof String);

        this.circus = defineList(builder, "circus",
          Collections.singletonList("minecraft:cake*1"),
          s -> s instanceof String);
        this.festival = defineList(builder, "festival",
          Collections.singletonList("minecraft:cake*9"),
          s -> s instanceof String);
        this.nightowl = defineList(builder, "nightowl",
          Collections.singletonList("minecraft:golden_carrot*25"),
          s -> s instanceof String);
        this.spectacle = defineList(builder, "spectacle",
          Collections.singletonList("minecraft:cake*18"),
          s -> s instanceof String);
        this.nightowl2 = defineList(builder, "nightowl2",
          Collections.singletonList("minecraft:golden_carrot*75"),
          s -> s instanceof String);
        this.opera = defineList(builder, "opera",
          Collections.singletonList("minecraft:cake*27"),
          s -> s instanceof String);
        this.theater = defineList(builder, "theater",
          Collections.singletonList("minecraft:enchanted_golden_apple*16"),
          s -> s instanceof String);

        this.firstaid = defineList(builder, "firstaid",
          Collections.singletonList("minecraft:hay_block*8"),
          s -> s instanceof String);
        this.firstaid2 = defineList(builder, "firstaid2",
          Collections.singletonList("minecraft:hay_block*16"),
          s -> s instanceof String);
        this.livesaver = defineList(builder, "livesaver",
          Collections.singletonList("minecraft:hay_block*32"),
          s -> s instanceof String);
        this.livesaver2 = defineList(builder, "livesaver2",
          Collections.singletonList("minecraft:hay_block*64"),
          s -> s instanceof String);
        this.guardianangel = defineList(builder, "guardianangel",
          Collections.singletonList("minecraft:hay_block*128"),
          s -> s instanceof String);
        this.guardianangel2 = defineList(builder, "guardianangel2",
          Collections.singletonList("minecraft:hay_block*256"),
          s -> s instanceof String);

        whatyaneed = defineList(builder, "whatyaneed",
          Collections.singletonList("minecraft:redstone*64"),
          s -> s instanceof String);
        enhanced_gates1 = defineList(builder, "enhanced_gates1",
          Arrays.asList("minecolonies:gate_wood*64", "minecolonies:ancienttome*2", "minecraft:iron_block*5"),
          s -> s instanceof String);
        enhanced_gates2 = defineList(builder, "enhanced_gates2",
          Arrays.asList("minecolonies:gate_iron*64", "minecolonies:ancienttome*2", "minecraft:obsidian*32"), s -> s instanceof String);
        stringwork = defineList(builder, "stringwork",
          Collections.singletonList("minecraft:string*16"),
          s -> s instanceof String);
        thoselungs = defineList(builder, "thoselungs",
          Collections.singletonList("minecraft:glass*64"),
          s -> s instanceof String);
        rainbowheaven = defineList(builder, "rainbowheaven",
          Collections.singletonList("minecraft:poppy*64"),
          s -> s instanceof String);

        this.veinminer = defineList(builder, "veinminer",
          Collections.singletonList("minecraft:iron_ore*32"),
          s -> s instanceof String);
        this.goodveins = defineList(builder, "goodveins",
          Collections.singletonList("minecraft:iron_ore*64"),
          s -> s instanceof String);
        this.richveins = defineList(builder, "richveins",
          Collections.singletonList("minecraft:gold_ore*32"),
          s -> s instanceof String);
        this.amazingveins = defineList(builder, "amazingveins",
          Collections.singletonList("minecraft:gold_ore*64"),
          s -> s instanceof String);
        this.motherlode = defineList(builder, "motherlode",
          Collections.singletonList("minecraft:diamond_ore*64"),
          s -> s instanceof String);

        this.ability = defineList(builder, "ability",
          Collections.singletonList("minecraft:iron_ingot*64"),
          s -> s instanceof String);
        this.skills = defineList(builder, "skills",
          Collections.singletonList("minecraft:iron_ingot*128"),
          s -> s instanceof String);
        this.tools = defineList(builder, "tools",
          Collections.singletonList("minecraft:iron_ingot*256"),
          s -> s instanceof String);
        this.seemsautomatic = defineList(builder, "seemsautomatic",
          Collections.singletonList("minecraft:iron_ingot*512"),
          s -> s instanceof String);
        this.madness = defineList(builder, "madness",
          Collections.singletonList("minecraft:iron_ingot*1024"),
          s -> s instanceof String);

        this.hittingiron = defineList(builder, "hittingiron",
          Collections.singletonList("minecraft:anvil*1"),
          s -> s instanceof String);
        this.stonecake = defineList(builder, "stonecake",
          Collections.singletonList("minecraft:chiseled_stone_bricks*64"),
          s -> s instanceof String);
        this.strong = defineList(builder, "strong",
          Collections.singletonList("minecraft:diamond*8"),
          s -> s instanceof String);
        this.hardened = defineList(builder, "hardened",
          Collections.singletonList("minecraft:diamond*16"),
          s -> s instanceof String);
        this.reinforced = defineList(builder, "reinforced",
          Collections.singletonList("minecraft:diamond*32"),
          s -> s instanceof String);
        this.steelbracing = defineList(builder, "steelbracing",
          Collections.singletonList("minecraft:diamond*64"),
          s -> s instanceof String);
        this.diamondcoated = defineList(builder, "diamondcoated",
          Collections.singletonList("minecraft:diamond*128"),
          s -> s instanceof String);

        this.memoryaid = defineList(builder, "memoryaid",
          Collections.singletonList("minecraft:paper*32"),
          s -> s instanceof String);
        this.cheatsheet = defineList(builder, "cheatsheet",
          Collections.singletonList("minecraft:paper*64"),
          s -> s instanceof String);
        this.recipebook = defineList(builder, "recipebook",
          Collections.singletonList("minecraft:paper*128"),
          s -> s instanceof String);
        this.rtm = defineList(builder, "rtm",
          Collections.singletonList("minecraft:paper*256"),
          s -> s instanceof String);
        this.rainman = defineList(builder, "rainman",
          Collections.singletonList("minecraft:salmon_bucket*27"),
          s -> s instanceof String);

        this.woodwork = defineList(builder, "woodwork",
          Collections.singletonList("minecraft:oak_planks*64"),
          s -> s instanceof String);
        this.sieving = defineList(builder, "sieving",
          Collections.singletonList("minecraft:string*64"),
          s -> s instanceof String);
        this.space = defineList(builder, "space",
          Collections.singletonList("minecolonies:blockminecoloniesrack*16"),
          s -> s instanceof String);
        this.capacity = defineList(builder, "capacity",
          Collections.singletonList("minecolonies:blockminecoloniesrack*32"),
          s -> s instanceof String);
        this.fullstock = defineList(builder, "fullstock",
          Collections.singletonList("minecolonies:blockminecoloniesrack*64"),
          s -> s instanceof String);

        this.theflintstones = defineList(builder, "theflintstones",
          Collections.singletonList("minecraft:stone_bricks*64"),
          s -> s instanceof String);
        this.rockingroll = defineList(builder, "rockingroll",
          Collections.singletonList("minecraft:stone*64"),
          s -> s instanceof String);

        this.hot = defineList(builder, "hot",
          Collections.singletonList("minecraft:lava_bucket*4"),
          s -> s instanceof String);
        this.isthisredstone = defineList(builder, "isthisredstone",
          Collections.singletonList("minecraft:redstone*128"),
          s -> s instanceof String);
        this.redstonepowered = defineList(builder, "redstonepowered",
          Collections.singletonList("minecraft:redstone*256"),
          s -> s instanceof String);
        this.heavymachinery = defineList(builder, "heavymachinery",
          Collections.singletonList("minecraft:redstone*512"),
          s -> s instanceof String);
        this.whatisthisspeed = defineList(builder, "whatisthisspeed",
          Collections.singletonList("minecraft:redstone*1024"),
          s -> s instanceof String);
        this.lightning = defineList(builder, "lightning",
          Collections.singletonList("minecraft:redstone*2048"),
          s -> s instanceof String);

        this.biodegradable = defineList(builder, "biodegradable",
          Collections.singletonList("minecraft:bone_meal*64"),
          s -> s instanceof String);
        this.flowerpower = defineList(builder, "flowerpower",
          Collections.singletonList("minecolonies:compost*64"),
          s -> s instanceof String);

        this.letitgrow = defineList(builder, "letitgrow",
          Collections.singletonList("minecolonies:compost*16"),
          s -> s instanceof String);

        this.bonemeal = defineList(builder, "bonemeal",
          Collections.singletonList("minecraft:wheat_seeds*64"),
          s -> s instanceof String);
        this.dung = defineList(builder, "dung",
          Collections.singletonList("minecraft:wheat_seeds*128"),
          s -> s instanceof String);
        this.compost = defineList(builder, "compost",
          Collections.singletonList("minecraft:wheat_seeds*256"),
          s -> s instanceof String);
        this.fertilizer = defineList(builder, "fertilizer",
          Collections.singletonList("minecraft:wheat_seeds*512"),
          s -> s instanceof String);
        this.magiccompost = defineList(builder, "magiccompost",
          Collections.singletonList("minecraft:wheat_seeds*2048"),
          s -> s instanceof String);

        this.loaded = defineList(builder, "loaded",
          Collections.singletonList("minecraft:emerald*64"),
          s -> s instanceof String);
        this.heavilyloaded = defineList(builder, "heavilyloaded",
          Collections.singletonList("minecraft:emerald*128"),
          s -> s instanceof String);
        this.deeppockets = defineList(builder, "deeppockets",
          Collections.singletonList("minecraft:emerald*256"),
          s -> s instanceof String);

        taunt = defineList(builder, "taunt",
          Arrays.asList("minecraft:rotten_flesh*8", "minecraft:bone*8", "minecraft:spider_eye*8"),
          s -> s instanceof String);
        arrowuse = defineList(builder, "arrowuse",
          Collections.singletonList("minecraft:arrow*64"),
          s -> s instanceof String);
        arrowpierce = defineList(builder, "arrowpierce",
          Arrays.asList("minecraft:arrow*64", "minecraft:redstone*64"),
          s -> s instanceof String);
        knockbackaoe = defineList(builder, "knockbackaoe",
          Arrays.asList("minecraft:redstone*64", "minecraft:gold_ingot*64", "minecraft:lapis_lazuli*128"),
          s -> s instanceof String);

        this.knowtheend = defineList(builder, "knowtheend",
          Arrays.asList("minecraft:chorus_fruit*64"),
          s -> s instanceof String);

        this.morescrolls = defineList(builder, "morescrolls",
          Arrays.asList("minecraft:paper*64", "minecolonies:ancienttome*1", "minecraft:lapis_lazuli*64"),
          s -> s instanceof String);

        this.gildedhammer = defineList(builder, "gildedhammer",
          Arrays.asList("minecraft:gravel*64", "minecraft:sand*64", "minecraft:clay*64"),
          s -> s instanceof String);
        this.doubletrouble = defineList(builder, "doubletrouble",
          Arrays.asList("minecraft:bamboo*64", "minecraft:sugar_cane*64", "minecraft:cactus*64"),
          s -> s instanceof String);
        this.hotboots = defineList(builder, "hotboots",
          Arrays.asList("minecraft:leather*32", "minecraft:iron_ingot*16"),
          s -> s instanceof String);

        this.pavetheroad = defineList(builder, "pavetheroad",
          Collections.singletonList("minecraft:white_concrete*32"),
          s -> s instanceof String);

        finishCategory(builder);
    }
}
