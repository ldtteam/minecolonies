package com.minecolonies.api.configuration;

import com.minecolonies.api.colony.permissions.Explosions;
import com.minecolonies.api.util.constant.CitizenConstants;
import java.util.Arrays;
import java.util.List;
import net.neoforged.neoforge.common.NeoForgeConfigSpec;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Mod server configuration. Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Gameplay settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final NeoForgeConfigSpec.IntValue     initialCitizenAmount;
    public final NeoForgeConfigSpec.BooleanValue allowInfiniteSupplyChests;
    public final NeoForgeConfigSpec.BooleanValue allowInfiniteColonies;
    public final NeoForgeConfigSpec.BooleanValue allowOtherDimColonies;
    public final NeoForgeConfigSpec.IntValue     maxCitizenPerColony;
    public final NeoForgeConfigSpec.BooleanValue enableInDevelopmentFeatures;
    public final NeoForgeConfigSpec.BooleanValue alwaysRenderNameTag;
    public final NeoForgeConfigSpec.BooleanValue workersAlwaysWorkInRain;
    public final NeoForgeConfigSpec.BooleanValue holidayFeatures;
    public final NeoForgeConfigSpec.IntValue     luckyBlockChance;
    public final NeoForgeConfigSpec.IntValue     minThLevelToTeleport;
    public final NeoForgeConfigSpec.DoubleValue  foodModifier;
    public final NeoForgeConfigSpec.IntValue     diseaseModifier;
    public final NeoForgeConfigSpec.BooleanValue forceLoadColony;
    public final NeoForgeConfigSpec.IntValue     loadtime;
    public final NeoForgeConfigSpec.IntValue     colonyLoadStrictness;
    public final NeoForgeConfigSpec.IntValue     maxTreeSize;
    public final NeoForgeConfigSpec.BooleanValue noSupplyPlacementRestrictions;
    public final NeoForgeConfigSpec.BooleanValue skyRaiders;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Research settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */
    public final NeoForgeConfigSpec.BooleanValue                        researchCreativeCompletion;
    public final NeoForgeConfigSpec.BooleanValue                        researchDebugLog;
    public final NeoForgeConfigSpec.ConfigValue<List<? extends String>> researchResetCost;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Command settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final NeoForgeConfigSpec.BooleanValue canPlayerUseRTPCommand;
    public final NeoForgeConfigSpec.BooleanValue canPlayerUseColonyTPCommand;
    public final NeoForgeConfigSpec.BooleanValue canPlayerUseAllyTHTeleport;
    public final NeoForgeConfigSpec.BooleanValue canPlayerUseHomeTPCommand;
    public final NeoForgeConfigSpec.BooleanValue canPlayerUseShowColonyInfoCommand;
    public final NeoForgeConfigSpec.BooleanValue canPlayerUseKillCitizensCommand;
    public final NeoForgeConfigSpec.BooleanValue canPlayerUseAddOfficerCommand;
    public final NeoForgeConfigSpec.BooleanValue canPlayerUseDeleteColonyCommand;
    public final NeoForgeConfigSpec.BooleanValue canPlayerUseResetCommand;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Claim settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final NeoForgeConfigSpec.IntValue     maxColonySize;
    public final NeoForgeConfigSpec.IntValue     minColonyDistance;
    public final NeoForgeConfigSpec.IntValue     initialColonySize;
    public final NeoForgeConfigSpec.IntValue     maxDistanceFromWorldSpawn;
    public final NeoForgeConfigSpec.IntValue     minDistanceFromWorldSpawn;

    /*  ------------------------------------------------------------------------- *
     *  ------------------- ######## Combat Settings ######## ------------------- *
     *  ------------------------------------------------------------------------- */

    public final NeoForgeConfigSpec.BooleanValue enableColonyRaids;
    public final NeoForgeConfigSpec.IntValue     raidDifficulty;
    public final NeoForgeConfigSpec.IntValue     maxRaiders;
    public final NeoForgeConfigSpec.BooleanValue raidersbreakblocks;
    public final NeoForgeConfigSpec.IntValue     averageNumberOfNightsBetweenRaids;
    public final NeoForgeConfigSpec.IntValue     minimumNumberOfNightsBetweenRaids;
    public final NeoForgeConfigSpec.BooleanValue raidersbreakdoors;
    public final NeoForgeConfigSpec.BooleanValue mobAttackCitizens;
    public final NeoForgeConfigSpec.DoubleValue  guardDamageMultiplier;
    public final NeoForgeConfigSpec.DoubleValue  guardHealthMult;
    public final NeoForgeConfigSpec.BooleanValue pvp_mode;

    /*  ----------------------------------------------------------------------------- *
     *  ------------------- ######## Permission Settings ######## ------------------- *
     *  ----------------------------------------------------------------------------- */

    public final NeoForgeConfigSpec.BooleanValue                        enableColonyProtection;
    public final NeoForgeConfigSpec.EnumValue<Explosions>               turnOffExplosionsInColonies;
    public final NeoForgeConfigSpec.ConfigValue<List<? extends String>> freeToInteractBlocks;

    /*  -------------------------------------------------------------------------------- *
     *  ------------------- ######## Compatibility Settings ######## ------------------- *
     *  -------------------------------------------------------------------------------- */

    public final NeoForgeConfigSpec.ConfigValue<List<? extends String>> configListStudyItems;
    public final NeoForgeConfigSpec.ConfigValue<List<? extends String>> configListRecruitmentItems;
    public final NeoForgeConfigSpec.ConfigValue<List<? extends String>> luckyOres;
    public final NeoForgeConfigSpec.ConfigValue<List<? extends String>> diseases;
    public final NeoForgeConfigSpec.BooleanValue                        auditCraftingTags;
    public final NeoForgeConfigSpec.BooleanValue                        debugInventories;
    public final NeoForgeConfigSpec.BooleanValue                        blueprintBuildMode;

    /*  ------------------------------------------------------------------------------ *
     *  ------------------- ######## Pathfinding Settings ######## ------------------- *
     *  ------------------------------------------------------------------------------ */

    public final NeoForgeConfigSpec.IntValue pathfindingDebugVerbosity;
    public final NeoForgeConfigSpec.IntValue pathfindingMaxThreadCount;
    public final NeoForgeConfigSpec.IntValue minimumRailsToPath;

    /*  --------------------------------------------------------------------------------- *
     *  ------------------- ######## Request System Settings ######## ------------------- *
     *  --------------------------------------------------------------------------------- */

    public final NeoForgeConfigSpec.BooleanValue creativeResolve;


    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    protected ServerConfiguration(final NeoForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");

        initialCitizenAmount = defineInteger(builder, "initialcitizenamount", 4, 1, 10);
        allowInfiniteSupplyChests = defineBoolean(builder, "allowinfinitesupplychests", false);
        allowInfiniteColonies = defineBoolean(builder, "allowinfinitecolonies", false);
        allowOtherDimColonies = defineBoolean(builder, "allowotherdimcolonies", true);
        maxCitizenPerColony = defineInteger(builder, "maxcitizenpercolony", 250, 100, CitizenConstants.CITIZEN_LIMIT_MAX);
        enableInDevelopmentFeatures = defineBoolean(builder, "enableindevelopmentfeatures", false);
        alwaysRenderNameTag = defineBoolean(builder, "alwaysrendernametag", true);
        workersAlwaysWorkInRain = defineBoolean(builder, "workersalwaysworkinrain", false);
        holidayFeatures = defineBoolean(builder, "holidayfeatures", true);
        luckyBlockChance = defineInteger(builder, "luckyblockchance", 1, 0, 100);
        minThLevelToTeleport = defineInteger(builder, "minthleveltoteleport", 3, 0, 5);
        foodModifier = defineDouble(builder, "foodmodifier", 1.0, 0.1, 100);
        diseaseModifier = defineInteger(builder, "diseasemodifier", 5, 1, 100);
        forceLoadColony = defineBoolean(builder, "forceloadcolony", true);
        loadtime = defineInteger(builder, "loadtime", 10, 1, 1440);
        colonyLoadStrictness = defineInteger(builder, "colonyloadstrictness", 3, 1, 15);
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
        canPlayerUseDeleteColonyCommand = defineBoolean(builder, "canplayerusedeletecolonycommand", false);
        canPlayerUseResetCommand = defineBoolean(builder, "canplayeruseresetcommand", false);

        swapToCategory(builder, "claims");

        maxColonySize = defineInteger(builder, "maxColonySize", 20, 1, 250);
        minColonyDistance = defineInteger(builder, "minColonyDistance", 8, 1, 200);
        initialColonySize = defineInteger(builder, "initialColonySize", 4, 1, 15);
        maxDistanceFromWorldSpawn = defineInteger(builder, "maxdistancefromworldspawn", 30000, 1000, Integer.MAX_VALUE);
        minDistanceFromWorldSpawn = defineInteger(builder, "mindistancefromworldspawn", 0, 0, 1000);

        swapToCategory(builder, "combat");

        enableColonyRaids = defineBoolean(builder, "dobarbariansspawn", true);
        raidDifficulty = defineInteger(builder, "barbarianhordedifficulty", DEFAULT_BARBARIAN_DIFFICULTY, MIN_BARBARIAN_DIFFICULTY, MAX_BARBARIAN_DIFFICULTY);
        maxRaiders = defineInteger(builder, "maxBarbarianSize", 80, MIN_BARBARIAN_HORDE_SIZE, MAX_BARBARIAN_HORDE_SIZE);
        raidersbreakblocks = defineBoolean(builder, "dobarbariansbreakthroughwalls", true);
        averageNumberOfNightsBetweenRaids = defineInteger(builder, "averagenumberofnightsbetweenraids", 14, 1, 50);
        minimumNumberOfNightsBetweenRaids = defineInteger(builder, "minimumnumberofnightsbetweenraids", 10, 1, 30);
        mobAttackCitizens = defineBoolean(builder, "mobattackcitizens", true);
        raidersbreakdoors = defineBoolean(builder, "shouldraiderbreakdoors", true);
        guardDamageMultiplier = defineDouble(builder, "guardDamageMultiplier", 1.0, 0.1, 15.0);
        guardHealthMult = defineDouble(builder, "guardhealthmult", 1.0, 0.1, 5.0);
        pvp_mode = defineBoolean(builder, "pvp_mode", false);

        swapToCategory(builder, "permissions");

        enableColonyProtection = defineBoolean(builder, "enablecolonyprotection", true);
        turnOffExplosionsInColonies = defineEnum(builder, "turnoffexplosionsincolonies", Explosions.DAMAGE_ENTITIES);
        freeToInteractBlocks = defineList(builder, "freetointeractblocks",
          Arrays.asList
                  ("dirt",
                    "0 0 0"),
          s -> s instanceof String);

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

        creativeResolve = defineBoolean(builder, "creativeresolve", false);

        finishCategory(builder);
    }
}
