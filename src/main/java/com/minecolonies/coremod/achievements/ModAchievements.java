package com.minecolonies.coremod.achievements;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * Achievement collection.
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
    public static final Achievement achievementGetSupply      = new MineColoniesAchievement("supply", 0, -2, ModItems.supplyChest, null).registerStat();
    /**
     * Use the building tool.
     */
    public static final Achievement achievementWandOfbuilding = new MineColoniesAchievement("wandofbuilding", 0, -2, ModItems.buildTool, null)
                                                                  .registerStat();

    /**
     * Place a townhall.
     */
    public static final Achievement achievementTownhall         = new MineColoniesAchievement("townhall", 0, 0, ModBlocks.blockHutTownHall, null)
                                                                    .registerStat();
    /**
     * Upgrade a townhall to lv 1.
     */
    public static final Achievement achievementBuildingTownhall = new MineColoniesAchievement("upgrade.townhall.first", 2, 0, ModBlocks.blockHutTownHall,
                                                                                               achievementTownhall).registerStat();

    /**
     * Max out a townhall.
     */
    public static final Achievement achievementUpgradeTownhallMax = new MineColoniesAchievement("upgrade.townhall.max", 4, 0, ModBlocks.blockHutTownHall,
                                                                                                 achievementBuildingTownhall).registerStat();

    /**
     * Upgrade a builder to lv 1.
     */
    public static final Achievement achievementBuildingBuilder   = new MineColoniesAchievement("upgrade.builder.first", 2, 2, ModBlocks.blockHutBuilder,
                                                                                                achievementTownhall).registerStat();
    /**
     * Max out a builder.
     */
    public static final Achievement achievementUpgradeBuilderMax = new MineColoniesAchievement("upgrade.builder.max", 4, 2, ModBlocks.blockHutBuilder,
                                                                                                achievementBuildingBuilder).registerStat();

    /**
     * Upgrade a builder to lv 1.
     */
    public static final Achievement achievementBuildingColonist   = new MineColoniesAchievement("upgrade.colonist.first",
                                                                                                 2,
                                                                                                 18,
                                                                                                 ModBlocks.blockHutCitizen,
                                                                                                 achievementTownhall).registerStat();
    /**
     * Max out a builder.
     */
    public static final Achievement achievementUpgradeColonistMax = new MineColoniesAchievement("upgrade.colonist.max", 4, 18, ModBlocks.blockHutCitizen,
                                                                                                 achievementBuildingColonist).registerStat();

    /**
     * Upgrade a lumberjack to lv 1.
     */
    public static final Achievement achievementBuildingLumberjack   = new MineColoniesAchievement("upgrade.lumberjack.first",
                                                                                                   2,
                                                                                                   11,
                                                                                                   ModBlocks.blockHutLumberjack,
                                                                                                   achievementTownhall).registerStat();
    /**
     * Max out a lumberjack.
     */
    public static final Achievement achievementUpgradeLumberjackMax = new MineColoniesAchievement("upgrade.lumberjack.max",
                                                                                                   4,
                                                                                                   11,
                                                                                                   ModBlocks.blockHutLumberjack,
                                                                                                   achievementBuildingLumberjack).registerStat();

    /**
     * Upgrade a miner to lv 1.
     */
    public static final Achievement achievementBuildingMiner   = new MineColoniesAchievement("upgrade.miner.first", 2, 8, ModBlocks.blockHutMiner,
                                                                                              achievementTownhall).registerStat();
    /**
     * Max out a miner.
     */
    public static final Achievement achievementUpgradeMinerMax = new MineColoniesAchievement("upgrade.miner.max", 4, 8, ModBlocks.blockHutMiner,
                                                                                              achievementBuildingMiner).registerStat();

    /**
     * Upgrade a fisher to lv 1.
     */
    public static final Achievement achievementBuildingFisher   = new MineColoniesAchievement("upgrade.fisher.first", 2, 4, ModBlocks.blockHutFisherman,
                                                                                               achievementTownhall).registerStat();
    /**
     * Max out a fisher.
     */
    public static final Achievement achievementUpgradeFisherMax = new MineColoniesAchievement("upgrade.fisher.max", 4, 4, ModBlocks.blockHutFisherman,
                                                                                               achievementBuildingFisher).registerStat();

    /**
     * Upgrade a farmer to lv 1.
     */
    public static final Achievement achievementBuildingFarmer   = new MineColoniesAchievement("upgrade.farmer.first", 2, 14, ModBlocks.blockHutFarmer,
                                                                                               achievementTownhall).registerStat();
    /**
     * Max out a farmer.
     */
    public static final Achievement achievementUpgradeFarmerMax = new MineColoniesAchievement("upgrade.farmer.max", 4, 14, ModBlocks.blockHutFarmer,
                                                                                               achievementBuildingFarmer).registerStat();

    /**
     * Upgrade a guard to lv 1.
     */
    public static final Achievement achievementBuildingGuard   = new MineColoniesAchievement("upgrade.guard.first", 2, 6, ModBlocks.blockHutGuardTower,
                                                                                              achievementTownhall).registerStat();
    /**
     * Max out a guard.
     */
    public static final Achievement achievementUpgradeGuardMax = new MineColoniesAchievement("upgrade.guard.max", 4, 6, ModBlocks.blockHutGuardTower,
                                                                                              achievementBuildingGuard).registerStat();
    /**
     * Death achievements.
     */
    public static final Achievement achievementMinerDeathLava = new MineColoniesAchievement("miner.death.lava", -2, 8, Items.LAVA_BUCKET,
                                                                                             achievementBuildingMiner).registerStat();
    public static final Achievement achievementMinerDeathFall = new MineColoniesAchievement("miner.death.fall", -4, 8, Items.FEATHER,
                                                                                             achievementBuildingMiner).registerStat();
    public static final Achievement achievementLumberjackDeathTree = new MineColoniesAchievement("lumberjack.death.tree", -2, 11, Blocks.SAPLING,
                                                                                             achievementBuildingLumberjack).registerStat();
    public static final Achievement achievementFisherDeathGuardian = new MineColoniesAchievement("fisher.death.guardian", -2, 4, Blocks.SEA_LANTERN,
                                                                                             achievementBuildingFisher).registerStat();
    public static final Achievement achievementGuardDeathEnderman = new MineColoniesAchievement("guard.death.enderman", -2, 6, Items.ENDER_PEARL,
                                                                                             achievementBuildingGuard).registerStat();
    public static final Achievement achievementPlayerDeathGuard = new MineColoniesAchievement("player.death.guard", -4, 6, Items.ARROW,
                                                                                             achievementBuildingGuard).registerStat();

    /**
     * Do something for the first time.
     */
    public static final Achievement achievementBuildOneHut = new MineColoniesAchievement("builder.hutsBuilt.one", 4, 3, ModBlocks.blockHutCitizen,
                                                                                          achievementBuildingBuilder).registerStat();
    public static final Achievement achievementCatchOneFish = new MineColoniesAchievement("fisher.fishCaught.one", 4, 5, Items.FISH,
                                                                                           achievementBuildingFisher).registerStat();
    public static final Achievement achievementKillOneMob = new MineColoniesAchievement("guard.mobsKilled.one", 4, 7, Items.BONE,
                                                                                         achievementBuildingGuard).registerStat();
    public static final Achievement achievementMineOneOre = new MineColoniesAchievement("miner.oresMined.one", 4, 9, Blocks.COAL_ORE,
                                                                                         achievementBuildingMiner).registerStat();
    public static final Achievement achievementMineOneDiamond = new MineColoniesAchievement("miner.diamondsMined.one", 4, 10, Items.DIAMOND,
                                                                                             achievementBuildingMiner).registerStat();
    public static final Achievement achievementFellOneTree = new MineColoniesAchievement("lumberjack.treesFelled.one", 4, 12, Blocks.LOG,
                                                                                          achievementBuildingLumberjack).registerStat();
    public static final Achievement achievementPlantOneSapling = new MineColoniesAchievement("lumberjack.saplingsPlanted.one", 4, 13, Blocks.SAPLING,
                                                                                              achievementBuildingLumberjack).registerStat();
    public static final Achievement achievementHarvestOneCarrot = new MineColoniesAchievement("farmer.carrotsHarvested.one", 4, 15, Items.CARROT,
                                                                                               achievementBuildingFarmer).registerStat();
    public static final Achievement achievementHarvestOnePotato = new MineColoniesAchievement("farmer.potatoesHarvested.one", 4, 16, Items.POTATO,
                                                                                               achievementBuildingFarmer).registerStat();
    public static final Achievement achievementHarvestOneWheat = new MineColoniesAchievement("farmer.wheatHarvested.one", 4, 17, Items.WHEAT_SEEDS,
                                                                                              achievementBuildingFarmer).registerStat();

    /**
     * Do something for the 25th time.
     */
    public static final Achievement achievementBuild25Huts = new MineColoniesAchievement("builder.hutsBuilt.25", 6, 3, ModBlocks.blockHutCitizen,
                                                                                          achievementBuildOneHut).registerStat();
    public static final Achievement achievementCatch25Fish = new MineColoniesAchievement("fisher.fishCaught.25", 6, 5, Items.FISH,
                                                                                          achievementCatchOneFish).registerStat();
    public static final Achievement achievementKill25Mobs = new MineColoniesAchievement("guard.mobsKilled.25", 6, 7, Items.ROTTEN_FLESH,
                                                                                         achievementKillOneMob).registerStat();
    public static final Achievement achievementMine25Ores = new MineColoniesAchievement("miner.oresMined.25", 6, 9, Blocks.IRON_ORE,
                                                                                         achievementMineOneOre).registerStat();
    public static final Achievement achievementMine25Diamonds = new MineColoniesAchievement("miner.diamondsMined.25", 6, 10, Items.DIAMOND,
                                                                                             achievementMineOneDiamond).registerStat();
    public static final Achievement achievementFell25Trees = new MineColoniesAchievement("lumberjack.treesFelled.25", 6, 12, Blocks.LOG,
                                                                                          achievementFellOneTree).registerStat();
    public static final Achievement achievementPlant25Saplings = new MineColoniesAchievement("lumberjack.saplingsPlanted.25", 6, 13, Blocks.SAPLING,
                                                                                              achievementPlantOneSapling).registerStat();
    public static final Achievement achievementHarvest25Carrots = new MineColoniesAchievement("farmer.carrotsHarvested.25", 6, 15, Items.CARROT,
                                                                                               achievementHarvestOneCarrot).registerStat();
    public static final Achievement achievementHarvest25Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.25", 6, 16, Items.POTATO,
                                                                                                achievementHarvestOnePotato).registerStat();
    public static final Achievement achievementHarvest25Wheat = new MineColoniesAchievement("farmer.wheatHarvested.25", 6, 17, Items.WHEAT,
                                                                                             achievementHarvestOneWheat).registerStat();

    /**
     * Do something for the 100th time.
     */
    public static final Achievement achievementBuild100Huts       = new MineColoniesAchievement("builder.hutsBuilt.100", 8, 3, ModBlocks.blockHutCitizen,
                                                                                                 achievementBuild25Huts).registerStat();
    public static final Achievement achievementCatch100Fish       = new MineColoniesAchievement("fisher.fishCaught.100", 8, 5, Items.FISH,
                                                                                                 achievementCatch25Fish).registerStat();
    public static final Achievement achievementKill100Mobs        = new MineColoniesAchievement("guard.mobsKilled.100", 8, 7, Items.GUNPOWDER,
                                                                                                 achievementKill25Mobs).registerStat();
    public static final Achievement achievementMine100Ores        = new MineColoniesAchievement("miner.oresMined.100", 8, 9, Blocks.GOLD_ORE,
                                                                                                 achievementMine25Ores).registerStat();
    public static final Achievement achievementMine100Diamonds    = new MineColoniesAchievement("miner.diamondsMined.100", 8, 10, Items.DIAMOND,
                                                                                                 achievementMine25Diamonds).registerStat();
    public static final Achievement achievementFell100Trees       = new MineColoniesAchievement("lumberjack.treesFelled.100", 8, 12, Blocks.LOG,
                                                                                                 achievementFell25Trees).registerStat();
    public static final Achievement achievementPlant100Saplings   = new MineColoniesAchievement("lumberjack.saplingsPlanted.100", 8, 13, Blocks.SAPLING,
                                                                                                 achievementPlant25Saplings).registerStat();
    public static final Achievement achievementHarvest100Carrots  = new MineColoniesAchievement("farmer.carrotsHarvested.100", 8, 15, Items.CARROT,
                                                                                                 achievementHarvest25Carrots).registerStat();
    public static final Achievement achievementHarvest100Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.100", 8, 16, Items.POTATO,
                                                                                                 achievementHarvest25Potatoes).registerStat();
    public static final Achievement achievementHarvest100Wheat = new MineColoniesAchievement("farmer.wheatHarvested.100", 8, 17, Items.WHEAT,
                                                                                              achievementHarvest25Wheat).registerStat();

    /**
     * Do something for the 500th time.
     */
    public static final Achievement achievementBuild500Huts       = new MineColoniesAchievement("builder.hutsBuilt.500", 10, 3, ModBlocks.blockHutCitizen,
                                                                                                 achievementBuild100Huts).registerStat();
    public static final Achievement achievementCatch500Fish       = new MineColoniesAchievement("fisher.fishCaught.500", 10, 5, Items.FISH,
                                                                                                 achievementCatch100Fish).registerStat();
    public static final Achievement achievementKill500Mobs        = new MineColoniesAchievement("guard.mobsKilled.500", 10, 7, Items.ENDER_PEARL,
                                                                                                 achievementKill100Mobs).registerStat();
    public static final Achievement achievementMine500Ores        = new MineColoniesAchievement("miner.oresMined.500", 10, 9, Blocks.REDSTONE_ORE,
                                                                                                 achievementMine100Ores).registerStat();
    public static final Achievement achievementMine500Diamonds    = new MineColoniesAchievement("miner.diamondsMined.500", 10, 10, Items.DIAMOND,
                                                                                                 achievementMine100Diamonds).registerStat();
    public static final Achievement achievementFell500Trees       = new MineColoniesAchievement("lumberjack.treesFelled.500", 10, 12, Blocks.LOG,
                                                                                                 achievementFell100Trees).registerStat();
    public static final Achievement achievementPlant500Saplings   = new MineColoniesAchievement("lumberjack.saplingsPlanted.500", 10, 13, Blocks.SAPLING,
                                                                                                 achievementPlant100Saplings).registerStat();
    public static final Achievement achievementHarvest500Carrots  = new MineColoniesAchievement("farmer.carrotsHarvested.500", 10, 15, Items.CARROT,
                                                                                                 achievementHarvest100Carrots).registerStat();
    public static final Achievement achievementHarvest500Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.500", 10, 16, Items.POTATO,
                                                                                                 achievementHarvest100Potatoes).registerStat();
    public static final Achievement achievementHarvest500Wheat = new MineColoniesAchievement("farmer.wheatHarvested.500", 10, 17, Items.WHEAT,
                                                                                              achievementHarvest100Wheat).registerStat();

    /**
     * Do something for the 1000th time.
     */
    public static final Achievement achievementBuild1000Huts = new MineColoniesAchievement("builder.hutsBuilt.1000", 12, 3, ModBlocks.blockHutCitizen,
                                                                                            achievementBuild500Huts).registerStat();
    public static final Achievement achievementCatch1000Fish = new MineColoniesAchievement("fisher.fishCaught.1000", 12, 5, Items.FISH,
                                                                                            achievementCatch500Fish).registerStat();
    public static final Achievement achievementKill1000Mobs = new MineColoniesAchievement("guard.mobsKilled.1000", 12, 7, Items.ENDER_EYE,
                                                                                           achievementKill500Mobs).registerStat();
    public static final Achievement achievementMine1000Ores = new MineColoniesAchievement("miner.oresMined.1000", 12, 9, Blocks.LAPIS_ORE,
                                                                                           achievementMine500Ores).registerStat();
    public static final Achievement achievementMine1000Diamonds = new MineColoniesAchievement("miner.diamondsMined.1000", 12, 10, Items.DIAMOND,
                                                                                               achievementMine500Diamonds).registerStat();
    public static final Achievement achievementFell1000Trees = new MineColoniesAchievement("lumberjack.treesFelled.1000", 12, 12, Blocks.LOG,
                                                                                            achievementFell500Trees).registerStat();
    public static final Achievement achievementPlant1000Saplings = new MineColoniesAchievement("lumberjack.saplingsPlanted.1000", 12, 13, Blocks.SAPLING,
                                                                                                achievementPlant500Saplings).registerStat();
    public static final Achievement achievementHarvest1000Carrots = new MineColoniesAchievement("farmer.carrotsHarvested.1000", 12, 15, Items.GOLDEN_CARROT,
                                                                                                 achievementHarvest500Carrots).registerStat();
    public static final Achievement achievementHarvest1000Potatoes = new MineColoniesAchievement("farmer.potatoesHarvested.1000", 12, 16, Items.POTATO,
                                                                                                  achievementHarvest500Potatoes).registerStat();
    public static final Achievement achievementHarvest1000Wheat = new MineColoniesAchievement("farmer.wheatHarvested.1000", 12, 17, Blocks.HAY_BLOCK,
                                                                                               achievementHarvest500Wheat).registerStat();

    // Sizes
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_SETTLEMENT} citizens.
     */
    public static final Achievement achievementSizeSettlement = new MineColoniesAchievement("size.settlement", 4, 1, ModItems.itemAchievementProxySettlement,
                                                                                             achievementTownhall).registerStat();
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_TOWN} citizens.
     */
    public static final Achievement achievementSizeTown       = new MineColoniesAchievement("size.town", 6, 1, ModItems.itemAchievementProxyTown, achievementSizeSettlement)
                                                                  .registerStat();
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_CITY} citizens.
     */
    public static final Achievement achievementSizeCity       = new MineColoniesAchievement("size.city", 8, 1, ModItems.itemAchievementProxyCity, achievementSizeTown)
                                                                  .registerStat();

    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_METROPOLIS} citizens.
     */
    public static final Achievement achievementSizeMetropolis = new MineColoniesAchievement("size.metropolis", 10, 1, ModItems.itemAchievementProxyMetropolis, achievementSizeCity)
                                                                  .registerStat();

    // Achievement pages
    /**
     * The MineColonies achievement page.
     */
    private static final AchievementPage achievementPageMineColonies = new MineColoniesAchievementPage(
                                                                                                        Constants.MOD_NAME,
                                                                                                        achievementGetSupply,
                                                                                                        achievementWandOfbuilding,
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
        AchievementPage.registerAchievementPage(achievementPageMineColonies);
    }
}
