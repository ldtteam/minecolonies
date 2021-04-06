package com.minecolonies.api.configuration;

import com.minecolonies.api.util.constant.NameConstants;
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
    public final ForgeConfigSpec.IntValue     colonyLoadStrictness;
    public final ForgeConfigSpec.IntValue     badVisitorsChance;
    public final ForgeConfigSpec.BooleanValue generateSupplyLoot;

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
        colonyLoadStrictness = defineInteger(builder, "colonyloadstrictness", 3, 1, 15);
        badVisitorsChance = defineInteger(builder, "badvisitorchance", 2, 1, 100);
        generateSupplyLoot = defineBoolean(builder, "generatesupplyloot", true);

        swapToCategory(builder, "research");
        researchCreativeCompletion = defineBoolean(builder, "researchcreativecompletion", true);
        researchDebugLog = defineBoolean(builder, "researchdebuglog", false);
        researchResetCost = defineList(builder, "researchresetcost", Arrays.asList("minecolonies:ancienttome:1"), s-> s instanceof String);

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
        initialColonySize = defineInteger(builder, "initialColonySize", 4, 1, 15);
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
            "Smallpox,1,minecraft:honey_bottle,minecraft:golden_apple"),
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

        finishCategory(builder);
    }
}
