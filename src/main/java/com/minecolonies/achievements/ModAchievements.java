package com.minecolonies.achievements;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * Achievement collection
 *
 * @author Isfirs
 * @since 0.2
 */
public final class ModAchievements
{

    // Achievements
    /**
     * Place a supply chest
     */
    public static final Achievement achievementGetSupply      = new AchievementGetSupply("supply", "supply", -2, -2).registerStat();
    /**
     * Use the building tool
     */
    public static final Achievement achievementWandOfbuilding = new AchievementWandOfBuilding("wandofbuilding", "wandofbuilding", 0, -2).registerStat();

    // Buildings
    /**
     * Place a townhall
     */
    public static final Achievement achievementBuildingTownhall = new AchievementBuildingTownhall("townhall", "townhall", -2, 0).registerStat();

    /**
     * Upgrade a builder to lv 1
     */
    public static final Achievement achievementBuildingBuilder   = new AchievementBuildingBuilder("upgrade.builder.first", "upgrade.builder.first", 0, 1).registerStat();
    /**
     * Max out a builder
     */
    public static final Achievement achievementUpgradeBuilderMax = new AchievementUpgradeBuilderMax("upgrade.builder.max", "upgrade.builder.max", 2, 1).registerStat();

    /**
     * Upgrade a builder to lv 1
     */
    public static final Achievement achievementBuildingColonist   = new AchievementBuildingColonist("upgrade.colonist.first", "upgrade.colonist.first", 0, 2).registerStat();
    /**
     * Max out a builder
     */
    public static final Achievement achievementUpgradeColonistMax = new AchievementUpgradeColonistMax("upgrade.colonist.max", "upgrade.colonist.max", 2, 2).registerStat();

    /**
     * Upgrade a lumberjack to lv 1
     */
    public static final Achievement achievementBuildingLumberjack   = new AchievementBuildingLumberjack("upgrade.lumberjack.first", "upgrade.lumberjack.first", 0, 3).registerStat();
    /**
     * Max out a lumberjack
     */
    public static final Achievement achievementUpgradeLumberjackMax = new AchievementUpgradeLumberjackMax("upgrade.lumberjack.max", "upgrade.lumberjack.max", 2, 3).registerStat();

    /**
     * Upgrade a miner to lv 1
     */
    public static final Achievement achievementBuildingMiner   = new AchievementBuildingMiner("upgrade.miner.first", "upgrade.miner.first", 0, 4).registerStat();
    /**
     * Max out a miner
     */
    public static final Achievement achievementUpgradeMinerMax = new AchievementUpgradeMinerMax("upgrade.miner.max", "upgrade.miner.max", 2, 4).registerStat();

    /**
     * Upgrade a fisher to lv 1
     */
    public static final Achievement achievementBuildingFisher   = new AchievementBuildingFisher("upgrade.fisher.first", "upgrade.fisher.first", 0, 5).registerStat();
    /**
     * Max out a fisher
     */
    public static final Achievement achievementUpgradeFisherMax = new AchievementUpgradeFisherMax("upgrade.fisher.max", "upgrade.fisher.max", 2, 5).registerStat();

    // Sizes
    /**
     * Reach {@link AchievementSizeSettlement#SIZE} citizens
     */
    public static final Achievement achievementSizeSettlement = new AchievementSizeSettlement("size.pioneer", "size.settlement", 2, -2).registerStat();
    /**
     * Reach {@link AchievementSizeTown#SIZE} citizens
     */
    public static final Achievement achievementSizeTown       = new AchievementSizeTown("size.town", "size.town", 4, -2).registerStat();
    /**
     * Reach {@link AchievementSizeCity#SIZE} citizens
     */
    public static final Achievement achievementsizeCity       = new AchievementSizeCity("size.city", "size.city", 6, -2).registerStat();

    // Achievement pages#+
    /**
     * The MineColonies achievement page
     */
    public static final AchievementPage achievementPageMineColonies = new AchievementPageMineColonies(
            achievementGetSupply, achievementWandOfbuilding, achievementBuildingTownhall, achievementBuildingBuilder, achievementBuildingColonist,
            achievementBuildingLumberjack, achievementBuildingMiner, achievementBuildingFisher, achievementSizeSettlement, achievementSizeTown,
            achievementsizeCity, achievementUpgradeColonistMax, achievementUpgradeBuilderMax, achievementUpgradeLumberjackMax, achievementUpgradeMinerMax,
            achievementUpgradeFisherMax
    );

    /**
     * private constructor to hide the implicit public one.
     */
    private ModAchievements()
    {
    }

    /**
     * Init.
     * 
     * Registers the page
     */
    public static void init()
    {
        AchievementPage.registerAchievementPage(achievementPageMineColonies);
    }

}
