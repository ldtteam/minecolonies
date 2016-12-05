package com.minecolonies.coremod.achievements;

import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.lib.Constants;
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
    public static final Achievement achievementGetSupply      = new MineColoniesAchievement("supply", -2, -2, ModItems.supplyChest, null).registerStat();
    /**
     * Use the building tool.
     */
    public static final Achievement achievementWandOfbuilding = new MineColoniesAchievement("wandofbuilding", 0, -2, ModItems.buildTool, null)
                                                                  .registerStat();

    /**
     * Place a townhall.
     */
    public static final Achievement achievementTownhall         = new MineColoniesAchievement("townhall", -2, 0, ModBlocks.blockHutTownHall, null)
                                                                    .registerStat();
    /**
     * Upgrade a townhall to lv 1.
     */
    public static final Achievement achievementBuildingTownhall = new MineColoniesAchievement("upgrade.townhall.first", 0, 0, ModBlocks.blockHutTownHall,
                                                                                               achievementTownhall).registerStat();

    /**
     * Max out a townhall.
     */
    public static final Achievement achievementUpgradeTownhallMax = new MineColoniesAchievement("upgrade.townhall.max", 2, 0, ModBlocks.blockHutTownHall,
                                                                                                 achievementBuildingTownhall).registerStat();

    /**
     * Upgrade a builder to lv 1.
     */
    public static final Achievement achievementBuildingBuilder   = new MineColoniesAchievement("upgrade.builder.first", 0, 1, ModBlocks.blockHutBuilder,
                                                                                                achievementTownhall).registerStat();
    /**
     * Max out a builder.
     */
    public static final Achievement achievementUpgradeBuilderMax = new MineColoniesAchievement("upgrade.builder.max", 2, 1, ModBlocks.blockHutBuilder,
                                                                                                achievementBuildingBuilder).registerStat();

    /**
     * Upgrade a builder to lv 1.
     */
    public static final Achievement achievementBuildingColonist   = new MineColoniesAchievement("upgrade.colonist.first",
                                                                                                 0,
                                                                                                 2,
                                                                                                 ModBlocks.blockHutCitizen,
                                                                                                 achievementTownhall).registerStat();
    /**
     * Max out a builder.
     */
    public static final Achievement achievementUpgradeColonistMax = new MineColoniesAchievement("upgrade.colonist.max", 2, 2, ModBlocks.blockHutCitizen,
                                                                                                 achievementBuildingColonist).registerStat();

    /**
     * Upgrade a lumberjack to lv 1.
     */
    public static final Achievement achievementBuildingLumberjack   = new MineColoniesAchievement("upgrade.lumberjack.first",
                                                                                                   0,
                                                                                                   3,
                                                                                                   ModBlocks.blockHutLumberjack,
                                                                                                   achievementTownhall).registerStat();
    /**
     * Max out a lumberjack.
     */
    public static final Achievement achievementUpgradeLumberjackMax = new MineColoniesAchievement("upgrade.lumberjack.max",
                                                                                                   2,
                                                                                                   3,
                                                                                                   ModBlocks.blockHutLumberjack,
                                                                                                   achievementBuildingLumberjack).registerStat();

    /**
     * Upgrade a miner to lv 1.
     */
    public static final Achievement achievementBuildingMiner   = new MineColoniesAchievement("upgrade.miner.first", 0, 4, ModBlocks.blockHutMiner,
                                                                                              achievementTownhall).registerStat();
    /**
     * Max out a miner.
     */
    public static final Achievement achievementUpgradeMinerMax = new MineColoniesAchievement("upgrade.miner.max", 2, 4, ModBlocks.blockHutMiner,
                                                                                              achievementBuildingMiner).registerStat();

    /**
     * Upgrade a fisher to lv 1.
     */
    public static final Achievement achievementBuildingFisher   = new MineColoniesAchievement("upgrade.fisher.first", 0, 5, ModBlocks.blockHutFisherman,
                                                                                               achievementTownhall).registerStat();
    /**
     * Max out a fisher.
     */
    public static final Achievement achievementUpgradeFisherMax = new MineColoniesAchievement("upgrade.fisher.max", 2, 5, ModBlocks.blockHutFisherman,
                                                                                               achievementBuildingFisher).registerStat();

    /**
     * Upgrade a farmer to lv 1.
     */
    public static final Achievement achievementBuildingFarmer   = new MineColoniesAchievement("upgrade.farmer.first", 0, 6, ModBlocks.blockHutFarmer,
                                                                                               achievementTownhall).registerStat();
    /**
     * Max out a farmer.
     */
    public static final Achievement achievementUpgradeFarmerMax = new MineColoniesAchievement("upgrade.farmer.max", 2, 6, ModBlocks.blockHutFarmer,
                                                                                               achievementBuildingFarmer).registerStat();

    /**
     * Upgrade a guard to lv 1.
     */
    public static final Achievement achievementBuildingGuard   = new MineColoniesAchievement("upgrade.guard.first", 0, 7, ModBlocks.blockHutGuardTower,
                                                                                              achievementTownhall).registerStat();
    /**
     * Max out a guard.
     */
    public static final Achievement achievementUpgradeGuardMax = new MineColoniesAchievement("upgrade.guard.max", 2, 7, ModBlocks.blockHutGuardTower,
                                                                                              achievementBuildingGuard).registerStat();

    /**
     * Kill one mob.
     */
    public static final Achievement achievementKillOneMob = new MineColoniesAchievement("guard.mobkill.one", 2, 8, Items.BONE, achievementBuildingGuard).registerStat();

    /**
     * Kill 25 mobs.
     */
    public static final Achievement achievementKill25Mobs = new MineColoniesAchievement("guard.mobkill.25", 4, 8, Items.ROTTEN_FLESH, achievementKillOneMob).registerStat();

    /**
     * Kill 100 mobs.
     */
    public static final Achievement achievementKill100Mobs = new MineColoniesAchievement("guard.mobkill.100", 6, 8, Items.GUNPOWDER, achievementKill25Mobs).registerStat();

    /**
     * Kill 500 mobs.
     */
    public static final Achievement achievementKill500Mobs = new MineColoniesAchievement("guard.mobkill.500", 8, 8, Items.ENDER_PEARL, achievementKill100Mobs).registerStat();

    /**
     * Kill 1000 mobs.
     */
    public static final Achievement achievementKill1000Mobs = new MineColoniesAchievement("guard.mobkill.1000", 10, 8, Items.ENDER_EYE, achievementKill500Mobs).registerStat();


    // Sizes
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_SETTLEMENT} citizens.
     */
    public static final Achievement achievementSizeSettlement = new MineColoniesAchievement("size.settlement", -4, 0, ModItems.itemAchievementProxySettlement,
                                                                                             achievementTownhall).registerStat();
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_TOWN} citizens.
     */
    public static final Achievement achievementSizeTown       = new MineColoniesAchievement("size.town", -6, 0, ModItems.itemAchievementProxyTown, achievementSizeSettlement)
                                                                  .registerStat();
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_CITY} citizens.
     */
    public static final Achievement achievementSizeCity       = new MineColoniesAchievement("size.city", -8, 0, ModItems.itemAchievementProxyCity, achievementSizeTown)
                                                                  .registerStat();

    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_METROPOLIS} citizens.
     */
    public static final Achievement achievementSizeMetropolis = new MineColoniesAchievement("size.metropolis", -10, 0, ModItems.itemAchievementProxyMetropolis, achievementSizeCity)
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
                                                                                                        achievementKill1000Mobs


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
