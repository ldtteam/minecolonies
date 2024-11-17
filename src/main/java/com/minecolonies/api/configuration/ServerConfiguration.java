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
    public final ForgeConfigSpec.BooleanValue allowInfiniteSupplyChests;
    public final ForgeConfigSpec.BooleanValue allowInfiniteColonies;
    public final ForgeConfigSpec.BooleanValue allowOtherDimColonies;
    public final ForgeConfigSpec.IntValue     maxCitizenPerColony;
    public final ForgeConfigSpec.BooleanValue enableInDevelopmentFeatures;
    public final ForgeConfigSpec.BooleanValue alwaysRenderNameTag;
    public final ForgeConfigSpec.BooleanValue workersAlwaysWorkInRain;
    public final ForgeConfigSpec.BooleanValue holidayFeatures;
    public final ForgeConfigSpec.IntValue     luckyBlockChance;
    public final ForgeConfigSpec.IntValue     minThLevelToTeleport;
    public final ForgeConfigSpec.DoubleValue  foodModifier;
    public final ForgeConfigSpec.IntValue     diseaseModifier;
    public final ForgeConfigSpec.BooleanValue forceLoadColony;
    public final ForgeConfigSpec.IntValue     loadtime;
    public final ForgeConfigSpec.IntValue     colonyLoadStrictness;
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
    public final ForgeConfigSpec.BooleanValue canPlayerUseResetCommand;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Claim settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final ForgeConfigSpec.IntValue     maxColonySize;
    public final ForgeConfigSpec.IntValue     minColonyDistance;
    public final ForgeConfigSpec.IntValue     initialColonySize;
    public final ForgeConfigSpec.IntValue     maxDistanceFromWorldSpawn;
    public final ForgeConfigSpec.IntValue     minDistanceFromWorldSpawn;

    /*  ------------------------------------------------------------------------- *
     *  ------------------- ######## Combat Settings ######## ------------------- *
     *  ------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue enableColonyRaids;
    public final ForgeConfigSpec.IntValue     raidDifficulty;
    public final ForgeConfigSpec.IntValue     maxRaiders;
    public final ForgeConfigSpec.BooleanValue raidersbreakblocks;
    public final ForgeConfigSpec.IntValue     averageNumberOfNightsBetweenRaids;
    public final ForgeConfigSpec.IntValue     minimumNumberOfNightsBetweenRaids;
    public final ForgeConfigSpec.BooleanValue raidersbreakdoors;
    public final ForgeConfigSpec.BooleanValue mobAttackCitizens;
    public final ForgeConfigSpec.DoubleValue  guardDamageMultiplier;
    public final ForgeConfigSpec.DoubleValue  guardHealthMult;
    public final ForgeConfigSpec.BooleanValue pvp_mode;

    /*  ----------------------------------------------------------------------------- *
     *  ------------------- ######## Permission Settings ######## ------------------- *
     *  ----------------------------------------------------------------------------- */

    public final ForgeConfigSpec.BooleanValue                        enableColonyProtection;
    public final ForgeConfigSpec.EnumValue<Explosions>               turnOffExplosionsInColonies;

    /*  -------------------------------------------------------------------------------- *
     *  ------------------- ######## Compatibility Settings ######## ------------------- *
     *  -------------------------------------------------------------------------------- */

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> configListStudyItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> configListRecruitmentItems;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> luckyOres;
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

    public final ForgeConfigSpec.BooleanValue creativeResolve;


    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "gameplay");

        initialCitizenAmount = defineInteger(builder, "initialcitizenamount", 4, 1, 10);
        allowInfiniteSupplyChests = defineBoolean(builder, "allowinfinitesupplychests", false);
        allowInfiniteColonies = defineBoolean(builder, "allowinfinitecolonies", false);
        allowOtherDimColonies = defineBoolean(builder, "allowotherdimcolonies", true);
        maxCitizenPerColony = defineInteger(builder, "maxcitizenpercolony", 250, 30, CitizenConstants.CITIZEN_LIMIT_MAX);
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
        pathfindingMaxThreadCount = defineInteger(builder, "pathfindingmaxthreadcount", 1, 1, 10);

        swapToCategory(builder, "requestSystem");

        creativeResolve = defineBoolean(builder, "creativeresolve", false);

        finishCategory(builder);
    }
}
