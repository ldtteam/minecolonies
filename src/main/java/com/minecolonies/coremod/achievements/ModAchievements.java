package com.minecolonies.coremod.achievements;

import com.minecolonies.api.achievements.IMineColoniesAchievement;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

/**
 * MineColoniesAchievement collection.
 *
 * @since 0.2
 */
public final class ModAchievements
{
    /**
     * Population size to achieve {@link ModAchievements#achievementSizeSettlement}.
     */
    public static final int ACHIEVEMENT_SIZE_SETTLEMENT = 5;

    /**
     * Population size to achieve {@link ModAchievements#achievementSizeTown}.
     */
    public static final int ACHIEVEMENT_SIZE_TOWN = 10;

    /**
     * Population size to achieve {@link ModAchievements#achievementSizeCity}.
     */
    public static final int ACHIEVEMENT_SIZE_CITY = 20;

    /**
     * Population size to achieve {@link ModAchievements#achievementSizeMetropolis}.
     */
    public static final int ACHIEVEMENT_SIZE_METROPOLIS = 50;

    /**
     * Place a supply chest.
     */
    public static final MineColoniesAchievement achievementGetSupply      = new MineColoniesAchievement("supply", 0, -2, ModItems.supplyChest, null);

    /**
     * Place a townhall.
     */
    public static final MineColoniesAchievement achievementTownhall         = new MineColoniesAchievement("townhall", 0, 0, ModBlocks.blockHutTownHall,null);
    /**
     * Upgrade a townhall to lv 1.
     */
    public static final MineColoniesAchievement achievementBuildingTownhall = new MineColoniesAchievement("upgrade.townhall.first", 2, 0, ModBlocks.blockHutTownHall,
            achievementTownhall);

    /**
     * Max out a townhall.
     */
    public static final MineColoniesAchievement achievementUpgradeTownhallMax = new MineColoniesAchievement("upgrade.townhall.max", 4, 0, ModBlocks.blockHutTownHall,
            achievementBuildingTownhall);

    /**
     * Upgrade a builder to lv 1.
     */
    public static final MineColoniesAchievement achievementBuildingBuilder   = new MineColoniesAchievement("upgrade.builder.first", 2, 2, ModBlocks.blockHutBuilder,
            achievementTownhall);
    /**
     * Max out a builder.
     */
    public static final MineColoniesAchievement achievementUpgradeBuilderMax = new MineColoniesAchievement("upgrade.builder.max", 4, 2, ModBlocks.blockHutBuilder,
            achievementBuildingBuilder);

    /**
     * Upgrade a builder to lv 1.
     */
    public static final MineColoniesAchievement achievementBuildingColonist   = new MineColoniesAchievement("upgrade.colonist.first",
            2,
            18,
            ModBlocks.blockHutCitizen,
            achievementTownhall);
    /**
     * Max out a builder.
     */
    public static final MineColoniesAchievement achievementUpgradeColonistMax = new MineColoniesAchievement("upgrade.colonist.max", 4, 18, ModBlocks.blockHutCitizen,
            achievementBuildingColonist);

    /**
     * Upgrade a lumberjack to lv 1.
     */
    public static final MineColoniesAchievement achievementBuildingLumberjack   = new MineColoniesAchievement("upgrade.lumberjack.first",
            2,
            11,
            ModBlocks.blockHutLumberjack,
            achievementTownhall);
    /**
     * Max out a lumberjack.
     */
    public static final MineColoniesAchievement achievementUpgradeLumberjackMax = new MineColoniesAchievement("upgrade.lumberjack.max",
            4,
            11,
            ModBlocks.blockHutLumberjack,
            achievementBuildingLumberjack);

    /**
     * Upgrade a miner to lv 1.
     */
    public static final MineColoniesAchievement achievementBuildingMiner   = new MineColoniesAchievement("upgrade.miner.first", 2, 8, ModBlocks.blockHutMiner, achievementTownhall);
    /**
     * Max out a miner.
     */
    public static final MineColoniesAchievement achievementUpgradeMinerMax
            = new MineColoniesAchievement("upgrade.miner.max", 4, 8, ModBlocks.blockHutMiner, achievementBuildingMiner);

    /**
     * Upgrade a fisher to lv 1.
     */
    public static final MineColoniesAchievement achievementBuildingFisher
            = new MineColoniesAchievement("upgrade.fisher.first", 2, 4, ModBlocks.blockHutFisherman, achievementTownhall);
    /**
     * Max out a fisher.
     */
    public static final MineColoniesAchievement achievementUpgradeFisherMax
            = new MineColoniesAchievement("upgrade.fisher.max", 4, 4, ModBlocks.blockHutFisherman, achievementBuildingFisher);

    /**
     * Upgrade a farmer to lv 1.
     */
    public static final MineColoniesAchievement achievementBuildingFarmer
            = new MineColoniesAchievement("upgrade.farmer.first", 2, 14, ModBlocks.blockHutFarmer, achievementTownhall);
    /**
     * Max out a farmer.
     */
    public static final MineColoniesAchievement achievementUpgradeFarmerMax
            = new MineColoniesAchievement("upgrade.farmer.max", 4, 14, ModBlocks.blockHutFarmer, achievementBuildingFarmer);

    /**
     * Upgrade a guard to lv 1.
     */
    public static final MineColoniesAchievement  achievementBuildingGuard
                                                                                = new MineColoniesAchievement("upgrade.guard.first", 2, 6, ModBlocks.blockHutGuardTower, achievementTownhall);
    /**
     * Max out a guard.
     */
    public static final MineColoniesAchievement  achievementUpgradeGuardMax     = new MineColoniesAchievement("upgrade.guard.max", 4, 6, ModBlocks.blockHutGuardTower,
            achievementBuildingGuard);
    /**
     * Death achievements.
     */
    public static final IMineColoniesAchievement achievementMinerDeathLava      = new MineColoniesAchievement("miner.death.lava", -2, 8, Items.LAVA_BUCKET,
            achievementBuildingMiner);
    public static final IMineColoniesAchievement achievementMinerDeathFall      = new MineColoniesAchievement("miner.death.fall", -4, 8, Items.FEATHER,
            achievementBuildingMiner);
    public static final IMineColoniesAchievement achievementLumberjackDeathTree = new MineColoniesAchievement("lumberjack.death.tree", -2, 11, Blocks.SAPLING,
            achievementBuildingLumberjack);
    public static final MineColoniesAchievement  achievementFisherDeathGuardian = new MineColoniesAchievement("fisher.death.guardian", -2, 4, Blocks.SEA_LANTERN,
            achievementBuildingFisher);
    public static final IMineColoniesAchievement achievementGuardDeathEnderman  = new MineColoniesAchievement("guard.death.enderman", -2, 6, Items.ENDER_PEARL,
            achievementBuildingGuard);
    public static final MineColoniesAchievement  achievementPlayerDeathGuard    = new MineColoniesAchievement("player.death.guard", -4, 6, Items.ARROW,
            achievementBuildingGuard);

    /**
     * Do something for the first time.
     */
    public static final MineColoniesAchievement achievementBuildOneHut      = new MineColoniesAchievement("builder.hutsBuilt.one", 4, 3, ModBlocks.blockHutCitizen,
            achievementBuildingBuilder);
    public static final MineColoniesAchievement achievementCatchOneFish     = new MineColoniesAchievement("fisher.fishCaught.one", 4, 5, Items.FISH,
            achievementBuildingFisher);
    public static final MineColoniesAchievement achievementKillOneMob       = new MineColoniesAchievement("guard.mobsKilled.one", 4, 7, Items.BONE,
            achievementBuildingGuard);
    public static final MineColoniesAchievement achievementMineOneOre       = new MineColoniesAchievement("miner.oresMined.one", 4, 9, Blocks.COAL_ORE,
            achievementBuildingMiner);
    public static final MineColoniesAchievement achievementMineOneDiamond   = new MineColoniesAchievement("miner.diamondsMined.one", 4, 10, Items.DIAMOND,
            achievementBuildingMiner);
    public static final MineColoniesAchievement achievementFellOneTree      = new MineColoniesAchievement("lumberjack.treesFelled.one", 4, 12, Blocks.LOG,
            achievementBuildingLumberjack);
    public static final MineColoniesAchievement achievementPlantOneSapling  = new MineColoniesAchievement("lumberjack.saplingsPlanted.one", 4, 13, Blocks.SAPLING,
            achievementBuildingLumberjack);
    public static final MineColoniesAchievement achievementHarvestOneCarrot = new MineColoniesAchievement("farmer.carrotsHarvested.one", 4, 15, Items.CARROT,
            achievementBuildingFarmer);
    public static final MineColoniesAchievement achievementHarvestOnePotato = new MineColoniesAchievement("farmer.potatoesHarvested.one", 4, 16, Items.POTATO,
            achievementBuildingFarmer);
    public static final MineColoniesAchievement achievementHarvestOneWheat  = new MineColoniesAchievement("farmer.wheatHarvested.one", 4, 17, Items.WHEAT_SEEDS,
            achievementBuildingFarmer);

    /**
     * Do something for the 25th time.
     */
    public static final MineColoniesAchievement achievementBuild25Huts       = new MineColoniesAchievement("builder.hutsBuilt.25", 6, 3, ModBlocks.blockHutCitizen,
            achievementBuildOneHut);
    public static final MineColoniesAchievement achievementCatch25Fish       = new MineColoniesAchievement("fisher.fishCaught.25", 6, 5, Items.FISH,
            achievementCatchOneFish);
    public static final MineColoniesAchievement achievementKill25Mobs        = new MineColoniesAchievement("guard.mobsKilled.25", 6, 7, Items.ROTTEN_FLESH,
            achievementKillOneMob);
    public static final MineColoniesAchievement achievementMine25Ores        = new MineColoniesAchievement("miner.oresMined.25", 6, 9, Blocks.IRON_ORE,
            achievementMineOneOre);
    public static final MineColoniesAchievement achievementMine25Diamonds    = new MineColoniesAchievement("miner.diamondsMined.25", 6, 10, Items.DIAMOND,
            achievementMineOneDiamond);
    public static final MineColoniesAchievement achievementFell25Trees       = new MineColoniesAchievement("lumberjack.treesFelled.25", 6, 12, Blocks.LOG,
            achievementFellOneTree);
    public static final MineColoniesAchievement achievementPlant25Saplings   = new MineColoniesAchievement("lumberjack.saplingsPlanted.25", 6, 13, Blocks.SAPLING,
            achievementPlantOneSapling);
    public static final MineColoniesAchievement achievementHarvest25Carrots  = new MineColoniesAchievement("farmer.carrotsHarvested.25", 6, 15, Items.CARROT,
            achievementHarvestOneCarrot);
    public static final MineColoniesAchievement achievementHarvest25Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.25", 6, 16, Items.POTATO,
            achievementHarvestOnePotato);
    public static final MineColoniesAchievement achievementHarvest25Wheat    = new MineColoniesAchievement("farmer.wheatHarvested.25", 6, 17, Items.WHEAT,
            achievementHarvestOneWheat);

    /**
     * Do something for the 100th time.
     */
    public static final MineColoniesAchievement achievementBuild100Huts       = new MineColoniesAchievement("builder.hutsBuilt.100", 8, 3, ModBlocks.blockHutCitizen,
            achievementBuild25Huts);
    public static final MineColoniesAchievement achievementCatch100Fish       = new MineColoniesAchievement("fisher.fishCaught.100", 8, 5, Items.FISH,
            achievementCatch25Fish);
    public static final MineColoniesAchievement achievementKill100Mobs        = new MineColoniesAchievement("guard.mobsKilled.100", 8, 7, Items.GUNPOWDER,
            achievementKill25Mobs);
    public static final MineColoniesAchievement achievementMine100Ores        = new MineColoniesAchievement("miner.oresMined.100", 8, 9, Blocks.GOLD_ORE,
            achievementMine25Ores);
    public static final MineColoniesAchievement achievementMine100Diamonds    = new MineColoniesAchievement("miner.diamondsMined.100", 8, 10, Items.DIAMOND,
            achievementMine25Diamonds);
    public static final MineColoniesAchievement achievementFell100Trees       = new MineColoniesAchievement("lumberjack.treesFelled.100", 8, 12, Blocks.LOG,
            achievementFell25Trees);
    public static final MineColoniesAchievement achievementPlant100Saplings   = new MineColoniesAchievement("lumberjack.saplingsPlanted.100", 8, 13, Blocks.SAPLING,
            achievementPlant25Saplings);
    public static final MineColoniesAchievement achievementHarvest100Carrots  = new MineColoniesAchievement("farmer.carrotsHarvested.100", 8, 15, Items.CARROT,
            achievementHarvest25Carrots);
    public static final MineColoniesAchievement achievementHarvest100Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.100", 8, 16, Items.POTATO,
            achievementHarvest25Potatoes);
    public static final MineColoniesAchievement achievementHarvest100Wheat    = new MineColoniesAchievement("farmer.wheatHarvested.100", 8, 17, Items.WHEAT,
            achievementHarvest25Wheat);

    /**
     * Do something for the 500th time.
     */
    public static final MineColoniesAchievement achievementBuild500Huts       = new MineColoniesAchievement("builder.hutsBuilt.500", 10, 3, ModBlocks.blockHutCitizen,
            achievementBuild100Huts);
    public static final MineColoniesAchievement achievementCatch500Fish       = new MineColoniesAchievement("fisher.fishCaught.500", 10, 5, Items.FISH,
            achievementCatch100Fish);
    public static final MineColoniesAchievement achievementKill500Mobs        = new MineColoniesAchievement("guard.mobsKilled.500", 10, 7, Items.ENDER_PEARL,
            achievementKill100Mobs);
    public static final MineColoniesAchievement achievementMine500Ores        = new MineColoniesAchievement("miner.oresMined.500", 10, 9, Blocks.REDSTONE_ORE,
            achievementMine100Ores);
    public static final MineColoniesAchievement achievementMine500Diamonds    = new MineColoniesAchievement("miner.diamondsMined.500", 10, 10, Items.DIAMOND,
            achievementMine100Diamonds);
    public static final MineColoniesAchievement achievementFell500Trees       = new MineColoniesAchievement("lumberjack.treesFelled.500", 10, 12, Blocks.LOG,
            achievementFell100Trees);
    public static final MineColoniesAchievement achievementPlant500Saplings   = new MineColoniesAchievement("lumberjack.saplingsPlanted.500", 10, 13, Blocks.SAPLING,
            achievementPlant100Saplings);
    public static final MineColoniesAchievement achievementHarvest500Carrots  = new MineColoniesAchievement("farmer.carrotsHarvested.500", 10, 15, Items.CARROT,
            achievementHarvest100Carrots);
    public static final MineColoniesAchievement achievementHarvest500Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.500", 10, 16, Items.POTATO,
            achievementHarvest100Potatoes);
    public static final MineColoniesAchievement achievementHarvest500Wheat    = new MineColoniesAchievement("farmer.wheatHarvested.500", 10, 17, Items.WHEAT,
            achievementHarvest100Wheat);

    /**
     * Do something for the 1000th time.
     */
    public static final MineColoniesAchievement achievementBuild1000Huts       = new MineColoniesAchievement("builder.hutsBuilt.1000", 12, 3, ModBlocks.blockHutCitizen,
            achievementBuild500Huts);
    public static final MineColoniesAchievement achievementCatch1000Fish       = new MineColoniesAchievement("fisher.fishCaught.1000", 12, 5, Items.FISH,
            achievementCatch500Fish);
    public static final MineColoniesAchievement achievementKill1000Mobs        = new MineColoniesAchievement("guard.mobsKilled.1000", 12, 7, Items.ENDER_EYE,
            achievementKill500Mobs);
    public static final MineColoniesAchievement achievementMine1000Ores        = new MineColoniesAchievement("miner.oresMined.1000", 12, 9, Blocks.LAPIS_ORE,
            achievementMine500Ores);
    public static final MineColoniesAchievement achievementMine1000Diamonds    = new MineColoniesAchievement("miner.diamondsMined.1000", 12, 10, Items.DIAMOND,
            achievementMine500Diamonds);
    public static final MineColoniesAchievement achievementFell1000Trees       = new MineColoniesAchievement("lumberjack.treesFelled.1000", 12, 12, Blocks.LOG,
            achievementFell500Trees);
    public static final MineColoniesAchievement achievementPlant1000Saplings   = new MineColoniesAchievement("lumberjack.saplingsPlanted.1000", 12, 13, Blocks.SAPLING,
            achievementPlant500Saplings);
    public static final MineColoniesAchievement achievementHarvest1000Carrots  = new MineColoniesAchievement("farmer.carrotsHarvested.1000", 12, 15, Items.GOLDEN_CARROT,
            achievementHarvest500Carrots);
    public static final MineColoniesAchievement achievementHarvest1000Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.1000", 12, 16, Items.POTATO,
            achievementHarvest500Potatoes);
    public static final MineColoniesAchievement achievementHarvest1000Wheat    = new MineColoniesAchievement("farmer.wheatHarvested.1000", 12, 17, Blocks.HAY_BLOCK,
            achievementHarvest500Wheat);

    // Sizes
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_SETTLEMENT} citizens.
     */
    public static final IMineColoniesAchievement achievementSizeSettlement
                                                                     = new MineColoniesAchievement("size.settlement", 4, 1, ModItems.itemAchievementProxySettlement, achievementTownhall);
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_TOWN} citizens.
     */
    public static final IMineColoniesAchievement achievementSizeTown =
            new MineColoniesAchievement("size.town", 6, 1, ModItems.itemAchievementProxyTown, achievementSizeSettlement);
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_CITY} citizens.
     */
    public static final IMineColoniesAchievement achievementSizeCity = new MineColoniesAchievement("size.city", 8, 1, ModItems.itemAchievementProxyCity, achievementSizeTown);

    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_METROPOLIS} citizens.
     */
    public static final IMineColoniesAchievement achievementSizeMetropolis
            = new MineColoniesAchievement("size.metropolis", 10, 1, ModItems.itemAchievementProxyMetropolis, achievementSizeCity);

    // MineColoniesAchievement pages
    /**
     * The MineColonies achievement page.
     */
    private static final MineColoniesAchievementPage achievementPageMineColonies = new MineColoniesAchievementPage(
                                                                                                        Constants.MOD_NAME,
                                                                                                        achievementGetSupply,
                                                                                                        achievementTownhall,
                                                                                                        achievementBuildingTownhall,
                                                                                                        achievementUpgradeTownhallMax,
                                                                                                        achievementBuildingBuilder,
                                                                                                        achievementBuildingColonist,
                                                                                                        achievementBuildingLumberjack,
                                                                                                        achievementBuildingMiner,
                                                                                                        achievementBuildingFisher,
                                                                                                        achievementSizeSettlement,
                                                                                                        achievementSizeTown,
                                                                                                        achievementSizeCity,
                                                                                                        achievementUpgradeColonistMax,
                                                                                                        achievementUpgradeBuilderMax,
                                                                                                        achievementUpgradeLumberjackMax,
                                                                                                        achievementUpgradeMinerMax,
                                                                                                        achievementUpgradeFisherMax,
                                                                                                        achievementSizeMetropolis,
                                                                                                        achievementBuildingFarmer,
                                                                                                        achievementUpgradeFarmerMax,
                                                                                                        achievementBuildingGuard,
                                                                                                        achievementUpgradeGuardMax,
                                                                                                        achievementKillOneMob,
                                                                                                        achievementKill25Mobs,
                                                                                                        achievementKill100Mobs,
                                                                                                        achievementKill500Mobs,
                                                                                                        achievementKill1000Mobs,
                                                                                                        achievementMineOneOre,
                                                                                                        achievementMine25Ores,
                                                                                                        achievementMine100Ores,
                                                                                                        achievementMine500Ores,
                                                                                                        achievementMine1000Ores,
                                                                                                        achievementMineOneDiamond,
                                                                                                        achievementMine25Diamonds,
                                                                                                        achievementMine100Diamonds,
                                                                                                        achievementMine500Diamonds,
                                                                                                        achievementMine1000Diamonds,
                                                                                                        achievementBuildOneHut,
                                                                                                        achievementBuild25Huts,
                                                                                                        achievementBuild100Huts,
                                                                                                        achievementBuild500Huts,
                                                                                                        achievementBuild1000Huts,
                                                                                                        achievementCatchOneFish,
                                                                                                        achievementCatch25Fish,
                                                                                                        achievementCatch100Fish,
                                                                                                        achievementCatch500Fish,
                                                                                                        achievementCatch1000Fish,
                                                                                                        achievementHarvestOneCarrot,
                                                                                                        achievementHarvest25Carrots,
                                                                                                        achievementHarvest100Carrots,
                                                                                                        achievementHarvest500Carrots,
                                                                                                        achievementHarvest1000Carrots,
                                                                                                        achievementHarvestOnePotato,
                                                                                                        achievementHarvest25Potatoes,
                                                                                                        achievementHarvest100Potatoes,
                                                                                                        achievementHarvest500Potatoes,
                                                                                                        achievementHarvest1000Potatoes,
                                                                                                        achievementHarvestOneWheat,
                                                                                                        achievementHarvest25Wheat,
                                                                                                        achievementHarvest100Wheat,
                                                                                                        achievementHarvest500Wheat,
                                                                                                        achievementHarvest1000Wheat,
                                                                                                        achievementFellOneTree,
                                                                                                        achievementFell25Trees,
                                                                                                        achievementFell100Trees,
                                                                                                        achievementFell500Trees,
                                                                                                        achievementFell1000Trees,
                                                                                                        achievementPlantOneSapling,
                                                                                                        achievementPlant25Saplings,
                                                                                                        achievementPlant100Saplings,
                                                                                                        achievementPlant500Saplings,
                                                                                                        achievementPlant1000Saplings,
                                                                                                        achievementMinerDeathLava,
                                                                                                        achievementMinerDeathFall,
                                                                                                        achievementLumberjackDeathTree,
                                                                                                        achievementFisherDeathGuardian,
                                                                                                        achievementGuardDeathEnderman,
                                                                                                        achievementPlayerDeathGuard
    );

    /**
     * private constructor to hide the implicit public one.
     */
    private ModAchievements()
    {

    }

    /**
     * Init.
     * <p>
     * Registers the page.
     */
    public static void init()
    {
        //todo add to an advancement page
        //(achievementPageMineColonies);
    }

}

