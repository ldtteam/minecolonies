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
    public static final Achievement achGetSupply      = new AchievementGetSupply("supply", "supply", -2, -2).registerStat();
    /**
     * Use the building tool
     */
    public static final Achievement achWandOfbuilding = new AchievementWandOfBuilding("wandofbuilding", "wandofbuilding", 0, -2).registerStat();

    // Buildings
    /**
     * Place a townhall
     */
    public static final Achievement achBuildingTownhall = new AchievementBuildingTownhall("townhall", "townhall", -2, 0).registerStat();

    /**
     * Upgrade a builder to lv 1
     */
    public static final Achievement achBuildingBuilder   = new AchievementBuildingBuilder("upgrade.builder.first", "upgrade.builder.first", 0, 1).registerStat();
    /**
     * Max out a builder
     */
    public static final Achievement achUpgradeBuilderMax = new AchievementUpgradeBuilderMax("upgrade.builder.max", "upgrade.builder.max", 2, 1).registerStat();

    /**
     * Upgrade a builder to lv 1
     */
    public static final Achievement achBuildingColonist   = new AchievementBuildingColonist("upgrade.colonist.first", "upgrade.colonist.first", 0, 2).registerStat();
    /**
     * Max out a builder
     */
    public static final Achievement achUpgradeColonistMax = new AchievementUpgradeColonistMax("upgrade.colonist.max", "upgrade.colonist.max", 2, 2).registerStat();

    /**
     * Upgrade a lumberjack to lv 1
     */
    public static final Achievement achBuildingLumberjack   = new AchievementBuildingLumberjack("upgrade.lumberjack.first", "upgrade.lumberjack.first", 0, 3).registerStat();
    /**
     * Max out a lumberjack
     */
    public static final Achievement achUpgradeLumberjackMax = new AchievementUpgradeLumberjackMax("upgrade.lumberjack.max", "upgrade.lumberjack.max", 2, 3).registerStat();

    /**
     * Upgrade a miner to lv 1
     */
    public static final Achievement achBuildingMiner   = new AchievementBuildingMiner("upgrade.miner.first", "upgrade.miner.first", 0, 4).registerStat();
    /**
     * Max out a miner
     */
    public static final Achievement achUpgradeMinerMax = new AchievementUpgradeMinerMax("upgrade.miner.max", "upgrade.miner.max", 2, 4).registerStat();

    /**
     * Upgrade a fisher to lv 1
     */
    public static final Achievement achBuildingFisher   = new AchievementBuildingFisher("upgrade.fisher.first", "upgrade.fisher.first", 0, 5).registerStat();
    /**
     * Max out a fisher
     */
    public static final Achievement achUpgradeFisherMax = new AchievementUpgradeFisherMax("upgrade.fisher.max", "upgrade.fisher.max", 2, 5).registerStat();

    // Sizes
    /**
     * Reach {@link AchievementSizeSettlement#SIZE} citizens
     */
    public static final Achievement achSizeSettlement = new AchievementSizeSettlement("size.pioneer", "size.settlement", 2, -2).registerStat();
    /**
     * Reach {@link AchievementSizeTown#SIZE} citizens
     */
    public static final Achievement achSizeTown       = new AchievementSizeTown("size.town", "size.town", 4, -2).registerStat();
    /**
     * Reach {@link AchievementSizeCity#SIZE} citizens
     */
    public static final Achievement achsizeCity       = new AchievementSizeCity("size.city", "size.city", 6, -2).registerStat();

    // Achievement pages#+
    /**
     * The MineColonies achievement page
     */
    public static final AchievementPage pageMineColonies = new AchievementPageMineColonies(
            achGetSupply, achWandOfbuilding, achBuildingTownhall, achBuildingBuilder, achBuildingColonist,
            achBuildingLumberjack, achBuildingMiner, achBuildingFisher, achSizeSettlement, achSizeTown,
            achsizeCity, achUpgradeColonistMax, achUpgradeBuilderMax, achUpgradeLumberjackMax, achUpgradeMinerMax,
            achUpgradeFisherMax);

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
        AchievementPage.registerAchievementPage(pageMineColonies);
    }

}
