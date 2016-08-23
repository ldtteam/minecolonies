package com.minecolonies.achievements;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.items.ModItems;
import com.minecolonies.lib.Constants;
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
     * Place a supply chest.
     */
    public static final Achievement achievementGetSupply      = new MineColoniesAchievement("supply", "supply", -2, -2, ModItems.supplyChest, null).registerStat();
    /**
     * Use the building tool.
     */
    public static final Achievement achievementWandOfbuilding = new MineColoniesAchievement("wandofbuilding", "wandofbuilding", 0, -2, ModItems.buildTool, achievementGetSupply)
            .registerStat();

    /**
     * Place a townhall.
     */
    public static final Achievement achievementBuildingTownhall = new MineColoniesAchievement("townhall", "townhall", -2, 0, ModBlocks.blockHutTownHall, achievementGetSupply)
            .registerStat();

    /**
     * Upgrade a builder to lv 1.
     */
    public static final Achievement achievementBuildingBuilder   = new MineColoniesAchievement("upgrade.builder.first", "upgrade.builder.first", 0, 1, ModBlocks.blockHutBuilder,
            achievementBuildingTownhall).registerStat();
    /**
     * Max out a builder.
     */
    public static final Achievement achievementUpgradeBuilderMax = new MineColoniesAchievement("upgrade.builder.max", "upgrade.builder.max", 2, 1, ModBlocks.blockHutBuilder,
            achievementBuildingBuilder).registerStat();

    /**
     * Upgrade a builder to lv 1.
     */
    public static final Achievement achievementBuildingColonist   = new MineColoniesAchievement("upgrade.colonist.first", "upgrade.colonist.first", 0, 2, ModBlocks
            .blockHutCitizen, achievementGetSupply).registerStat();
    /**
     * Max out a builder.
     */
    public static final Achievement achievementUpgradeColonistMax = new MineColoniesAchievement("upgrade.colonist.max", "upgrade.colonist.max", 2, 2, ModBlocks.blockHutCitizen,
            achievementBuildingColonist).registerStat();

    /**
     * Upgrade a lumberjack to lv 1.
     */
    public static final Achievement achievementBuildingLumberjack   = new MineColoniesAchievement("upgrade.lumberjack.first", "upgrade.lumberjack.first", 0, 3, ModBlocks
            .blockHutLumberjack, achievementGetSupply).registerStat();
    /**
     * Max out a lumberjack.
     */
    public static final Achievement achievementUpgradeLumberjackMax = new MineColoniesAchievement("upgrade.lumberjack.max", "upgrade.lumberjack.max", 2, 3, ModBlocks
            .blockHutLumberjack, achievementBuildingLumberjack).registerStat();

    /**
     * Upgrade a miner to lv 1.
     */
    public static final Achievement achievementBuildingMiner   = new MineColoniesAchievement("upgrade.miner.first", "upgrade.miner.first", 0, 4, ModBlocks.blockHutMiner,
            achievementGetSupply).registerStat();
    /**
     * Max out a miner.
     */
    public static final Achievement achievementUpgradeMinerMax = new MineColoniesAchievement("upgrade.miner.max", "upgrade.miner.max", 2, 4, ModBlocks.blockHutMiner,
            achievementBuildingMiner).registerStat();

    /**
     * Upgrade a fisher to lv 1.
     */
    public static final Achievement achievementBuildingFisher   = new MineColoniesAchievement("upgrade.fisher.first", "upgrade.fisher.first", 0, 5, ModBlocks.blockHutFisherman,
            achievementGetSupply).registerStat();
    /**
     * Max out a fisher.
     */
    public static final Achievement achievementUpgradeFisherMax = new MineColoniesAchievement("upgrade.fisher.max", "upgrade.fisher.max", 2, 5, ModBlocks.blockHutFisherman,
            achievementBuildingFisher).registerStat();

    // Sizes
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_SETTLEMENT} citizens.
     */
    public static final Achievement achievementSizeSettlement = new MineColoniesAchievement("size.pioneer", "size.settlement", 2, -2, Items.IRON_INGOT, null).registerStat();
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_TOWN} citizens.
     */
    public static final Achievement achievementSizeTown       = new MineColoniesAchievement("size.town", "size.town", 4, -2, Items.GOLD_INGOT, achievementSizeSettlement)
            .registerStat();
    /**
     * Reach {@link ModAchievements#ACHIEVEMENT_SIZE_CITY} citizens.
     */
    public static final Achievement achievementSizeCity       = new MineColoniesAchievement("size.city", "size.city", 6, -2, Items.DIAMOND, achievementSizeTown).registerStat();

    // Achievement pages#+
    /**
     * The MineColonies achievement page.
     */
    private static final AchievementPage achievementPageMineColonies = new MineColoniesAchievementPage(
            Constants.MOD_NAME,
            achievementGetSupply, achievementWandOfbuilding, achievementBuildingTownhall, achievementBuildingBuilder, achievementBuildingColonist, achievementBuildingLumberjack,
            achievementBuildingMiner, achievementBuildingFisher, achievementSizeSettlement, achievementSizeTown, achievementSizeCity, achievementUpgradeColonistMax,
            achievementUpgradeBuilderMax, achievementUpgradeLumberjackMax, achievementUpgradeMinerMax, achievementUpgradeFisherMax
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
