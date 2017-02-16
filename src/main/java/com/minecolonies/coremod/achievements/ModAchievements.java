package com.minecolonies.coremod.achievements;

import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.lib.Constants;
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
     * Do something for the first time.
     */
    public static final Achievement achievementKillOneMob = new MineColoniesAchievement("guard.mobkill.one", 4, 7, Items.BONE, achievementBuildingGuard).registerStat();
    public static final Achievement achievementMineOneOre = new MineColoniesAchievement("miner.OreMined.one", 4, 9, Blocks.COAL_ORE, achievementBuildingMiner).registerStat();
    public static final Achievement achievementMineOneDiamond = new MineColoniesAchievement("miner.DiamondMined.one", 4, 10, Items.DIAMOND, achievementBuildingMiner).registerStat();

    /**
     * Do something for the 25th time.
     */
    public static final Achievement achievementKill25Mobs = new MineColoniesAchievement("guard.mobkill.25", 6, 7, Items.ROTTEN_FLESH, achievementKillOneMob).registerStat();
    public static final Achievement achievementMine25Ores = new MineColoniesAchievement("miner.OreMined.25", 6, 9, Blocks.IRON_ORE, achievementMineOneOre).registerStat();
    public static final Achievement achievementMine25Diamonds = new MineColoniesAchievement("miner.DiamondMined.25", 6, 10, Items.DIAMOND, achievementMineOneDiamond).registerStat();

    /**
     * Do something for the 100th time.
     */
    public static final Achievement achievementKill100Mobs = new MineColoniesAchievement("guard.mobkill.100", 8, 7, Items.GUNPOWDER, achievementKill25Mobs).registerStat();
    public static final Achievement achievementMine100Ores = new MineColoniesAchievement("miner.OreMined.100", 8, 9, Blocks.REDSTONE_ORE, achievementMine25Ores).registerStat();
    public static final Achievement achievementMine100Diamonds = new MineColoniesAchievement("miner.DiamondMined.100", 8, 10, Items.DIAMOND, achievementMine25Diamonds).registerStat();

    /**
     * Do something for the 500th time.
     */
    public static final Achievement achievementKill500Mobs = new MineColoniesAchievement("guard.mobkill.500", 10, 7, Items.ENDER_PEARL, achievementKill100Mobs).registerStat();
    public static final Achievement achievementMine500Ores = new MineColoniesAchievement("miner.OreMined.500", 10, 9, Blocks.GOLD_ORE, achievementMine100Ores).registerStat();
    public static final Achievement achievementMine500Diamonds = new MineColoniesAchievement("miner.DiamondMined.500", 10, 10, Items.DIAMOND, achievementMine100Diamonds).registerStat();

    /**
     * Do something for the 1000th time.
     */
    public static final Achievement achievementKill1000Mobs = new MineColoniesAchievement("guard.mobkill.1000", 12, 7, Items.ENDER_EYE, achievementKill500Mobs).registerStat();
    public static final Achievement achievementMine1000Ores = new MineColoniesAchievement("miner.OreMined.1000", 12, 9, Blocks.LAPIS_ORE, achievementMine500Ores).registerStat();
    public static final Achievement achievementMine1000Diamonds = new MineColoniesAchievement("miner.DiamondMined.1000", 12, 10, Items.DIAMOND, achievementMine500Diamonds).registerStat();


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
                                                                                                        achievementMine1000Diamonds


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
