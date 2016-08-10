package com.minecolonies.achievements;

import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * Achievement collection
 *
 * @author Isfirs
 * @since 0.2
 */
public class ModAchievements
{

    // Achievements
    public static Achievement achGetSupply      = new AchGetSupply("supply", "supply", -2, -2).registerStat();
    public static Achievement achWandOfbuilding = new AchWandOfBuilding("wandofbuilding", "wandofbuilding", 0, -2)
            .registerStat();

    // Buildings
    public static Achievement achBuildingTownhall  = new AchBuildingTownhall("townhall", "townhall", -2, 0)
            .registerStat();
    public static Achievement achBuildingBuilder   = new AchBuildingBuilder("building.builder", "building.builder", 0, 1)
            .registerStat();
    public static Achievement achUpgradeBuilderMax = new AchUpgradeBuilderMax("upgrade.builder.max", "upgrade.builder.max", 2, 1)
            .registerStat();

    public static Achievement achBuildingColonist   = new AchBuildingColonist("building.colonist", "building.colonist", 0, 2)
            .registerStat();
    public static Achievement achUpgradeColonistMax = new AchUpgradeColonistMax("upgrade.colonist.max", "upgrade.colonist.max", 2, 2)
            .registerStat();

    public static Achievement achBuildingLumberjack   = new AchBuildingLumberjack("building.lumberjack", "building.lumberjack", 0, 3)
            .registerStat();
    public static Achievement achUpgradeLumberjackMax = new AchUpgradeLumberjackMax("upgrade.lumberjack.max", "upgrade.lumberjack.max", 2, 3)
            .registerStat();

    public static Achievement achBuildingMiner   = new AchBuildingMiner("building.miner", "building.miner", 0, 4)
            .registerStat();
    public static Achievement achUpgradeMinerMax = new AchUpgradeMinerMax("upgrade.miner.max", "upgrade.miner.max", 2, 4)
            .registerStat();

    // Sizes
    public static Achievement achSizeSettlement = new AchSizeSettlement("size.pioneer", "size.settlement", 2, -2, AchSizeSettlement.size)
            .registerStat();
    public static Achievement achSizeTown       = new AchSizeTown("size.town", "size.town", 4, -2, AchSizeTown.size)
            .registerStat();
    public static Achievement achsizeCity       = new AchSizeCity("size.city", "size.city", 6, -2, AchSizeCity.size)
            .registerStat();

    // Achievement pages
    public static AchievementPage pageMineColonies = new AchievementPageMineColonies(achGetSupply, achWandOfbuilding, achBuildingTownhall, achBuildingBuilder, achBuildingColonist, achBuildingLumberjack, achBuildingMiner, achSizeSettlement, achsizeCity, achSizeTown, achUpgradeColonistMax, achUpgradeBuilderMax, achUpgradeLumberjackMax, achUpgradeMinerMax);

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
