package com.minecolonies.api.configuration;

import com.minecolonies.api.colony.permissions.Explosions;
import com.minecolonies.api.util.constant.CitizenConstants;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Mod server configuration. Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
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
    public final ForgeConfigSpec.IntValue     builderBuildBlockDelay;
    public final ForgeConfigSpec.IntValue     blockMiningDelayModifier;
    public final ForgeConfigSpec.BooleanValue enableInDevelopmentFeatures;
    public final ForgeConfigSpec.BooleanValue alwaysRenderNameTag;
    public final ForgeConfigSpec.BooleanValue workersAlwaysWorkInRain;
    public final ForgeConfigSpec.BooleanValue sendEnteringLeavingMessages;
    public final ForgeConfigSpec.IntValue     allowGlobalNameChanges;
    public final ForgeConfigSpec.BooleanValue holidayFeatures;
    public final ForgeConfigSpec.IntValue     dirtFromCompost;
    public final ForgeConfigSpec.IntValue     luckyBlockChance;
    public final ForgeConfigSpec.IntValue     minThLevelToTeleport;
    public final ForgeConfigSpec.BooleanValue suggestBuildToolPlacement;
    public final ForgeConfigSpec.DoubleValue  foodModifier;
    public final ForgeConfigSpec.IntValue     diseaseModifier;
    public final ForgeConfigSpec.BooleanValue forceLoadColony;
    public final ForgeConfigSpec.IntValue     loadtime;
    public final ForgeConfigSpec.IntValue     colonyLoadStrictness;
    public final ForgeConfigSpec.IntValue     badVisitorsChance;
    public final ForgeConfigSpec.IntValue     maxTreeSize;
    public final ForgeConfigSpec.BooleanValue noSupplyPlacementRestrictions;
    public final ForgeConfigSpec.BooleanValue skyRaiders;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Research settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */
    public final ForgeConfigSpec.BooleanValue                        researchCreativeCompletion;
    public final ForgeConfigSpec.BooleanValue                        researchDebugLog;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> researchResetCost;

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
    public final ForgeConfigSpec.IntValue     maxBarbarianSize;
    public final ForgeConfigSpec.BooleanValue doBarbariansBreakThroughWalls;
    public final ForgeConfigSpec.IntValue     averageNumberOfNightsBetweenRaids;
    public final ForgeConfigSpec.IntValue     minimumNumberOfNightsBetweenRaids;
    public final ForgeConfigSpec.BooleanValue shouldRaidersBreakDoors;
    public final ForgeConfigSpec.BooleanValue mobAttackCitizens;
    public final ForgeConfigSpec.BooleanValue citizenCallForHelp;
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
    public final ForgeConfigSpec.EnumValue<Explosions>               turnOffExplosionsInColonies;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> specialPermGroup;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> freeToInteractBlocks;
    public final ForgeConfigSpec.IntValue                            secondsBetweenPermissionMessages;
    public final ForgeConfigSpec.IntValue                            maxkeptbackups;

    /*  -------------------------------------------------------------------------------- *
     *  ------------------- ######## Compatibility Settings ######## ------------------- *
     *  -------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> configListStudyItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> configListRecruitmentItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> luckyOres;
    public final ForgeConfigSpec.IntValue                            dynamicTreeHarvestSize;
    public final ForgeConfigSpec.IntValue                            fishingRodDurabilityAdjustT1;
    public final ForgeConfigSpec.IntValue                            fishingRodDurabilityAdjustT2;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> diseases;
    public final ForgeConfigSpec.BooleanValue                        auditCraftingTags;
    public final ForgeConfigSpec.BooleanValue                        debugInventories;
    public final ForgeConfigSpec.BooleanValue                        blueprintBuildMode;


    /*  ------------------------------------------------------------------------------ *
     *  ------------------- ######## Pathfinding Settings ######## ------------------- *
     *  ------------------------------------------------------------------------------ */

    public final ForgeConfigSpec.IntValue pathfindingDebugVerbosity;
    public final ForgeConfigSpec.IntValue pathfindingMaxThreadCount;
    public final ForgeConfigSpec.IntValue minimumRailsToPath;

    /*  --------------------------------------------------------------------------------- *
     *  ------------------- ######## Request System Settings ######## ------------------- *
     *  --------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue     maximalRetries;
    public final ForgeConfigSpec.IntValue     delayBetweenRetries;
    public final ForgeConfigSpec.BooleanValue creativeResolve;
    public final ForgeConfigSpec.BooleanValue canPlayerUseResetCommand;

    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");

        initialCitizenAmount = defineInteger(builder, "initialcitizenamount", 4, 1, 10);
        builderPlaceConstructionTape = defineBoolean(builder, "builderplaceconstructiontape", true);
        allowInfiniteSupplyChests = defineBoolean(builder, "allowinfinitesupplychests", false);
        allowInfiniteColonies = defineBoolean(builder, "allowinfinitecolonies", false);
        allowOtherDimColonies = defineBoolean(builder, "allowotherdimcolonies", true);
        citizenRespawnInterval = defineInteger(builder, "citizenrespawninterval", 60, CITIZEN_RESPAWN_INTERVAL_MIN, CITIZEN_RESPAWN_INTERVAL_MAX);
        maxCitizenPerColony = defineInteger(builder, "maxcitizenpercolony", 250, 4, CitizenConstants.CITIZEN_LIMIT_MAX);
        builderBuildBlockDelay = defineInteger(builder, "builderbuildblockdelay", 15, 1, 500);
        blockMiningDelayModifier = defineInteger(builder, "blockminingdelaymodifier", 500, 1, 10000);
        enableInDevelopmentFeatures = defineBoolean(builder, "enableindevelopmentfeatures", false);
        alwaysRenderNameTag = defineBoolean(builder, "alwaysrendernametag", true);
        workersAlwaysWorkInRain = defineBoolean(builder, "workersalwaysworkinrain", false);
        sendEnteringLeavingMessages = defineBoolean(builder, "sendenteringleavingmessages", true);
        allowGlobalNameChanges = defineInteger(builder, "allowglobalnamechanges", 1, -1, 1);
        holidayFeatures = defineBoolean(builder, "holidayfeatures", true);
        dirtFromCompost = defineInteger(builder, "dirtfromcompost", 1, 0, 100);
        luckyBlockChance = defineInteger(builder, "luckyblockchance", 1, 0, 100);
        minThLevelToTeleport = defineInteger(builder, "minthleveltoteleport", 3, 0, 5);
        suggestBuildToolPlacement = defineBoolean(builder, "suggestbuildtoolplacement", true);
        foodModifier = defineDouble(builder, "foodmodifier", 1.0, 0.1, 100);
        diseaseModifier = defineInteger(builder, "diseasemodifier", 5, 1, 100);
        forceLoadColony = defineBoolean(builder, "forceloadcolony", false);
        loadtime = defineInteger(builder, "loadtime", 10,1,1440);
        colonyLoadStrictness = defineInteger(builder, "colonyloadstrictness", 3, 1, 15);
        badVisitorsChance = defineInteger(builder, "badvisitorchance", 2, 1, 100);
        maxTreeSize = defineInteger(builder, "maxtreesize", 400, 1, 1000);
        noSupplyPlacementRestrictions = defineBoolean(builder, "nosupplyplacementrestrictions", false);
        skyRaiders = defineBoolean(builder, "skyraiders", false);

        swapToCategory(builder, "research");
        researchCreativeCompletion = defineBoolean(builder, "researchcreativecompletion", true);
        researchDebugLog = defineBoolean(builder, "researchdebuglog", false);
        researchResetCost = defineList(builder, "researchresetcost", Arrays.asList("minecolonies:ancienttome:1"), s -> s instanceof String);

        swapToCategory(builder, "commands");

        canPlayerUseRTPCommand = defineBoolean(builder, "canplayerusertpcommand", false);
        canPlayerUseColonyTPCommand = defineBoolean(builder, "canplayerusecolonytpcommand", false);
        canPlayerUseAllyTHTeleport = defineBoolean(builder, "canplayeruseallytownhallteleport", true);
        canPlayerUseHomeTPCommand = defineBoolean(builder, "canplayerusehometpcommand", false);
        canPlayerUseShowColonyInfoCommand = defineBoolean(builder, "canplayeruseshowcolonyinfocommand", true);
        canPlayerUseKillCitizensCommand = defineBoolean(builder, "canplayerusekillcitizenscommand", false);
        canPlayerUseAddOfficerCommand = defineBoolean(builder, "canplayeruseaddofficercommand", true);
        canPlayerUseDeleteColonyCommand = defineBoolean(builder, "canplayerusedeletecolonycommand", true);
        numberOfAttemptsForSafeTP = defineInteger(builder, "numberofattemptsforsafetp", 4, 1, 10);


        swapToCategory(builder, "claims");

        maxColonySize = defineInteger(builder, "maxColonySize", 20, 1, 50);
        minColonyDistance = defineInteger(builder, "minColonyDistance", 8, 1, 200);
        initialColonySize = defineInteger(builder, "initialColonySize", 4, 1, 15);
        restrictColonyPlacement = defineBoolean(builder, "restrictcolonyplacement", false);
        maxDistanceFromWorldSpawn = defineInteger(builder, "maxdistancefromworldspawn", 8000, 1000, 100000);
        minDistanceFromWorldSpawn = defineInteger(builder, "mindistancefromworldspawn", 512, 1, 1000);
        officersReceiveAdvancements = defineBoolean(builder, "officersreceiveadvancements", true);

        swapToCategory(builder, "combat");

        doBarbariansSpawn = defineBoolean(builder, "dobarbariansspawn", true);
        barbarianHordeDifficulty = defineInteger(builder, "barbarianhordedifficulty", DEFAULT_BARBARIAN_DIFFICULTY, MIN_BARBARIAN_DIFFICULTY, MAX_BARBARIAN_DIFFICULTY);
        maxBarbarianSize = defineInteger(builder, "maxBarbarianSize", 80, MIN_BARBARIAN_HORDE_SIZE, MAX_BARBARIAN_HORDE_SIZE);
        doBarbariansBreakThroughWalls = defineBoolean(builder, "dobarbariansbreakthroughwalls", true);
        averageNumberOfNightsBetweenRaids = defineInteger(builder, "averagenumberofnightsbetweenraids", 14, 1, 50);
        minimumNumberOfNightsBetweenRaids = defineInteger(builder, "minimumnumberofnightsbetweenraids", 10, 1, 30);
        mobAttackCitizens = defineBoolean(builder, "mobattackcitizens", true);
        shouldRaidersBreakDoors = defineBoolean(builder, "shouldraiderbreakdoors", true);
        citizenCallForHelp = defineBoolean(builder, "citizencallforhelp", true);
        rangerDamageMult = defineDouble(builder, "rangerdamagemult", 1.0, 0.1, 5.0);
        knightDamageMult = defineDouble(builder, "knightdamagemult", 1.0, 0.1, 5.0);
        guardHealthMult = defineDouble(builder, "guardhealthmult", 1.0, 0.1, 5.0);
        pvp_mode = defineBoolean(builder, "pvp_mode", false);
        daysUntilPirateshipsDespawn = defineInteger(builder, "daysuntilpirateshipsdespawn", 3, 1, 10);
        maxYForBarbarians = defineInteger(builder, "maxyforbarbarians", 200, 1, 500);

        swapToCategory(builder, "permissions");

        enableColonyProtection = defineBoolean(builder, "enablecolonyprotection", true);
        maxkeptbackups = defineInteger(builder, "maxkeptbackups", 50, 3, 5000);
        turnOffExplosionsInColonies = defineEnum(builder, "turnoffexplosionsincolonies", Explosions.DAMAGE_ENTITIES);
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

        configListStudyItems = defineList(builder, "configliststudyitems",
          Arrays.asList
                  ("minecraft:paper;400;100", "minecraft:book;600;10"),
          s -> s instanceof String);

        configListRecruitmentItems = defineList(builder, "configlistrecruitmentitems",
          Arrays.asList
                  ("minecraft:hay_block;3",
                    "minecraft:book;2",
                    "minecraft:enchanted_book;9",
                    "minecraft:diamond;9",
                    "minecraft:emerald;8",
                    "minecraft:baked_potato;1",
                    "minecraft:gold_ingot;2",
                    "minecraft:redstone;2",
                    "minecraft:lapis_lazuli;2",
                    "minecraft:cake;11",
                    "minecraft:sunflower;5",
                    "minecraft:honeycomb;6",
                    "minecraft:quartz;3"),
          s -> s instanceof String);
        luckyOres = defineList(builder, "luckyores",
          Arrays.asList
                  ("minecraft:coal_ore!64",
                    "minecraft:copper_ore!48",
                    "minecraft:iron_ore!32",
                    "minecraft:gold_ore!16",
                    "minecraft:redstone_ore!8",
                    "minecraft:lapis_ore!4",
                    "minecraft:diamond_ore!2",
                    "minecraft:emerald_ore!1"),
          s -> s instanceof String);

        dynamicTreeHarvestSize = defineInteger(builder, "dynamictreeharvestsize", 5, 1, 8);

        fishingRodDurabilityAdjustT2 = defineInteger(builder, "fishingroddurabilityadjustt2", 6, -249, 250000);
        fishingRodDurabilityAdjustT1 = defineInteger(builder, "fishingroddurabilityadjustt1", 22, -58, 250000);

        diseases = defineList(builder, "diseases",
          Arrays.asList("Influenza,100,minecraft:carrot,minecraft:potato",
            "Measles,10,minecraft:dandelion,minecraft:kelp,minecraft:poppy",
            "Smallpox,1,minecraft:honey_bottle,minecraft:golden_apple"),
          s -> s instanceof String);

        auditCraftingTags = defineBoolean(builder, "auditcraftingtags", false);
        debugInventories = defineBoolean(builder, "debuginventories", false);
        blueprintBuildMode = defineBoolean(builder, "blueprintbuildmode", false);

        swapToCategory(builder, "pathfinding");

        pathfindingDebugVerbosity = defineInteger(builder, "pathfindingdebugverbosity", 0, 0, 10);
        minimumRailsToPath = defineInteger(builder, "minimumrailstopath", 8, 5, 100);
        pathfindingMaxThreadCount = defineInteger(builder, "pathfindingmaxthreadcount", 2, 1, 10);

        swapToCategory(builder, "requestSystem");

        maximalRetries = defineInteger(builder, "maximalretries", 3, 1, 10);
        delayBetweenRetries = defineInteger(builder, "delaybetweenretries", 1200, 30, 10000);
        creativeResolve = defineBoolean(builder, "creativeresolve", false);
        canPlayerUseResetCommand = defineBoolean(builder, "canplayeruseresetcommand", false);

        finishCategory(builder);
    }
}
