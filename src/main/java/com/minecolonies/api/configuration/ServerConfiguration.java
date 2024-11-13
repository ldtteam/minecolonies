package com.minecolonies.api.configuration;

import com.ldtteam.common.config.AbstractConfiguration;
import com.minecolonies.api.colony.permissions.Explosions;
import com.minecolonies.api.util.constant.CitizenConstants;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Mod server configuration. Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Gameplay settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final IntValue     initialCitizenAmount;
    public final BooleanValue allowInfiniteSupplyChests;
    public final BooleanValue allowInfiniteColonies;
    public final BooleanValue allowOtherDimColonies;
    public final IntValue     maxCitizenPerColony;
    public final BooleanValue enableInDevelopmentFeatures;
    public final BooleanValue alwaysRenderNameTag;
    public final BooleanValue workersAlwaysWorkInRain;
    public final BooleanValue holidayFeatures;
    public final IntValue     luckyBlockChance;
    public final IntValue     minThLevelToTeleport;
    public final DoubleValue  foodModifier;
    public final IntValue     diseaseModifier;
    public final BooleanValue forceLoadColony;
    public final IntValue     loadtime;
    public final IntValue     colonyLoadStrictness;
    public final IntValue     maxTreeSize;
    public final BooleanValue noSupplyPlacementRestrictions;
    public final BooleanValue skyRaiders;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Research settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */
    public final BooleanValue                        researchCreativeCompletion;
    public final BooleanValue                        researchDebugLog;
    public final ConfigValue<List<? extends String>> researchResetCost;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Command settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final BooleanValue canPlayerUseRTPCommand;
    public final BooleanValue canPlayerUseColonyTPCommand;
    public final BooleanValue canPlayerUseAllyTHTeleport;
    public final BooleanValue canPlayerUseHomeTPCommand;
    public final BooleanValue canPlayerUseShowColonyInfoCommand;
    public final BooleanValue canPlayerUseKillCitizensCommand;
    public final BooleanValue canPlayerUseAddOfficerCommand;
    public final BooleanValue canPlayerUseDeleteColonyCommand;
    public final BooleanValue canPlayerUseResetCommand;

    /*  --------------------------------------------------------------------------- *
     *  ------------------- ######## Claim settings ######## ------------------- *
     *  --------------------------------------------------------------------------- */

    public final IntValue     maxColonySize;
    public final IntValue     minColonyDistance;
    public final IntValue     initialColonySize;
    public final IntValue     maxDistanceFromWorldSpawn;
    public final IntValue     minDistanceFromWorldSpawn;

    /*  ------------------------------------------------------------------------- *
     *  ------------------- ######## Combat Settings ######## ------------------- *
     *  ------------------------------------------------------------------------- */

    public final BooleanValue enableColonyRaids;
    public final IntValue     raidDifficulty;
    public final IntValue     maxRaiders;
    public final BooleanValue raidersbreakblocks;
    public final IntValue     averageNumberOfNightsBetweenRaids;
    public final IntValue     minimumNumberOfNightsBetweenRaids;
    public final BooleanValue raidersbreakdoors;
    public final BooleanValue mobAttackCitizens;
    public final DoubleValue  guardDamageMultiplier;
    public final DoubleValue  guardHealthMult;
    public final BooleanValue pvp_mode;

    /*  ----------------------------------------------------------------------------- *
     *  ------------------- ######## Permission Settings ######## ------------------- *
     *  ----------------------------------------------------------------------------- */

    public final BooleanValue                        enableColonyProtection;
    public final EnumValue<Explosions>               turnOffExplosionsInColonies;

    /*  -------------------------------------------------------------------------------- *
     *  ------------------- ######## Compatibility Settings ######## ------------------- *
     *  -------------------------------------------------------------------------------- */

    public final ConfigValue<List<? extends String>> configListStudyItems;
    public final ConfigValue<List<? extends String>> configListRecruitmentItems;
    public final ConfigValue<List<? extends String>> luckyOres;
    public final ConfigValue<List<? extends String>> diseases;
    public final BooleanValue                        auditCraftingTags;
    public final BooleanValue                        debugInventories;
    public final BooleanValue                        blueprintBuildMode;

    /*  ------------------------------------------------------------------------------ *
     *  ------------------- ######## Pathfinding Settings ######## ------------------- *
     *  ------------------------------------------------------------------------------ */

    public final IntValue pathfindingDebugVerbosity;
    public final IntValue pathfindingMaxThreadCount;
    public final IntValue minimumRailsToPath;

    /*  --------------------------------------------------------------------------------- *
     *  ------------------- ######## Request System Settings ######## ------------------- *
     *  --------------------------------------------------------------------------------- */

    public final BooleanValue creativeResolve;


    /**
     * Builds server configuration.
     *
     * @param builder config builder
     */
    public ServerConfiguration(final Builder builder)
    {
        super(builder, MOD_ID);
        final Predicate<Object> stringValidator = s -> s instanceof String;

        createCategory("gameplay");

        initialCitizenAmount = defineInteger("initialcitizenamount", 4, 1, 10);
        allowInfiniteSupplyChests = defineBoolean("allowinfinitesupplychests", false);
        allowInfiniteColonies = defineBoolean("allowinfinitecolonies", false);
        allowOtherDimColonies = defineBoolean("allowotherdimcolonies", true);
        maxCitizenPerColony = defineInteger("maxcitizenpercolony", 250, 100, CitizenConstants.CITIZEN_LIMIT_MAX);
        enableInDevelopmentFeatures = defineBoolean("enableindevelopmentfeatures", false);
        alwaysRenderNameTag = defineBoolean("alwaysrendernametag", true);
        workersAlwaysWorkInRain = defineBoolean("workersalwaysworkinrain", false);
        holidayFeatures = defineBoolean("holidayfeatures", true);
        luckyBlockChance = defineInteger("luckyblockchance", 1, 0, 100);
        minThLevelToTeleport = defineInteger("minthleveltoteleport", 3, 0, 5);
        foodModifier = defineDouble("foodmodifier", 1.0, 0.1, 100);
        diseaseModifier = defineInteger("diseasemodifier", 5, 1, 100);
        forceLoadColony = defineBoolean("forceloadcolony", true);
        loadtime = defineInteger("loadtime", 10, 1, 1440);
        colonyLoadStrictness = defineInteger("colonyloadstrictness", 3, 1, 15);
        maxTreeSize = defineInteger("maxtreesize", 400, 1, 1000);
        noSupplyPlacementRestrictions = defineBoolean("nosupplyplacementrestrictions", false);
        skyRaiders = defineBoolean("skyraiders", false);

        swapToCategory("research");
        researchCreativeCompletion = defineBoolean("researchcreativecompletion", true);
        researchDebugLog = defineBoolean("researchdebuglog", false);
        researchResetCost = defineList("researchresetcost", () -> "item ID, possibly with data", stringValidator, "minecolonies:ancienttome:1");

        swapToCategory("commands");

        canPlayerUseRTPCommand = defineBoolean("canplayerusertpcommand", false);
        canPlayerUseColonyTPCommand = defineBoolean("canplayerusecolonytpcommand", false);
        canPlayerUseAllyTHTeleport = defineBoolean("canplayeruseallytownhallteleport", true);
        canPlayerUseHomeTPCommand = defineBoolean("canplayerusehometpcommand", false);
        canPlayerUseShowColonyInfoCommand = defineBoolean("canplayeruseshowcolonyinfocommand", true);
        canPlayerUseKillCitizensCommand = defineBoolean("canplayerusekillcitizenscommand", false);
        canPlayerUseAddOfficerCommand = defineBoolean("canplayeruseaddofficercommand", true);
        canPlayerUseDeleteColonyCommand = defineBoolean("canplayerusedeletecolonycommand", false);
        canPlayerUseResetCommand = defineBoolean("canplayeruseresetcommand", false);

        swapToCategory("claims");

        maxColonySize = defineInteger("maxColonySize", 20, 1, 250);
        minColonyDistance = defineInteger("minColonyDistance", 8, 1, 200);
        initialColonySize = defineInteger("initialColonySize", 4, 1, 15);
        maxDistanceFromWorldSpawn = defineInteger("maxdistancefromworldspawn", 30000, 1000, Integer.MAX_VALUE);
        minDistanceFromWorldSpawn = defineInteger("mindistancefromworldspawn", 0, 0, 1000);

        swapToCategory("combat");

        enableColonyRaids = defineBoolean("dobarbariansspawn", true);
        raidDifficulty = defineInteger("barbarianhordedifficulty", DEFAULT_BARBARIAN_DIFFICULTY, MIN_BARBARIAN_DIFFICULTY, MAX_BARBARIAN_DIFFICULTY);
        maxRaiders = defineInteger("maxBarbarianSize", 80, MIN_BARBARIAN_HORDE_SIZE, MAX_BARBARIAN_HORDE_SIZE);
        raidersbreakblocks = defineBoolean("dobarbariansbreakthroughwalls", true);
        averageNumberOfNightsBetweenRaids = defineInteger("averagenumberofnightsbetweenraids", 14, 1, 50);
        minimumNumberOfNightsBetweenRaids = defineInteger("minimumnumberofnightsbetweenraids", 10, 1, 30);
        mobAttackCitizens = defineBoolean("mobattackcitizens", true);
        raidersbreakdoors = defineBoolean("shouldraiderbreakdoors", true);
        guardDamageMultiplier = defineDouble("guardDamageMultiplier", 1.0, 0.1, 15.0);
        guardHealthMult = defineDouble("guardhealthmult", 1.0, 0.1, 5.0);
        pvp_mode = defineBoolean("pvp_mode", false);

        swapToCategory("permissions");

        enableColonyProtection = defineBoolean("enablecolonyprotection", true);
        turnOffExplosionsInColonies = defineEnum("turnoffexplosionsincolonies", Explosions.DAMAGE_ENTITIES);

        swapToCategory("compatibility");

        configListStudyItems = defineList("configliststudyitems",
            () -> "item ID;skillChance;breakChance",
            stringValidator,
            "minecraft:paper;400;100",
            "minecraft:book;600;10");

        configListRecruitmentItems = defineList("configlistrecruitmentitems",
            () -> "item ID;rarity",
            stringValidator,
            "minecraft:hay_block;3",
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
            "minecraft:quartz;3");
        luckyOres = defineList("luckyores",
            () -> "item ID!rarity!building level",
            stringValidator,
            "minecraft:coal_ore!64",
            "minecraft:copper_ore!48",
            "minecraft:iron_ore!32",
            "minecraft:gold_ore!16",
            "minecraft:redstone_ore!8",
            "minecraft:lapis_ore!4",
            "minecraft:diamond_ore!2",
            "minecraft:emerald_ore!1");

        diseases = defineList("diseases",
            () -> "name,rarity,item IDs",
            stringValidator,
            "Influenza,100,minecraft:carrot,minecraft:potato",
            "Measles,10,minecraft:dandelion,minecraft:kelp,minecraft:poppy",
            "Smallpox,1,minecraft:honey_bottle,minecraft:golden_apple");

        auditCraftingTags = defineBoolean("auditcraftingtags", false);
        debugInventories = defineBoolean("debuginventories", false);
        blueprintBuildMode = defineBoolean("blueprintbuildmode", false);

        swapToCategory("pathfinding");

        pathfindingDebugVerbosity = defineInteger("pathfindingdebugverbosity", 0, 0, 10);
        minimumRailsToPath = defineInteger("minimumrailstopath", 8, 5, 100);
        pathfindingMaxThreadCount = defineInteger("pathfindingmaxthreadcount", 1, 1, 10);

        swapToCategory("requestSystem");

        creativeResolve = defineBoolean("creativeresolve", false);

        finishCategory();
    }
}
