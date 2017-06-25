package com.minecolonies.api.reference;

import com.minecolonies.api.lib.Constants;
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
     * Place a supply chest.
     */
    public static Achievement achievementGetSupply;
    /**
     * Use the building tool.
     */
    public static Achievement achievementWandOfbuilding;

    /**
     * Place a townhall.
     */
    public static Achievement achievementTownhall;
    /**
     * Upgrade a townhall to lv 1.
     */
    public static Achievement achievementBuildingTownhall;

    /**
     * Max out a townhall.
     */
    public static Achievement achievementUpgradeTownhallMax;

    /**
     * Upgrade a builder to lv 1.
     */
    public static Achievement achievementBuildingBuilder;
    /**
     * Max out a builder.
     */
    public static Achievement achievementUpgradeBuilderMax;

    /**
     * Upgrade a builder to lv 1.
     */
    public static Achievement achievementBuildingColonist;
    /**
     * Max out a builder.
     */
    public static Achievement achievementUpgradeColonistMax;

    /**
     * Upgrade a lumberjack to lv 1.
     */
    public static Achievement achievementBuildingLumberjack;
    /**
     * Max out a lumberjack.
     */
    public static Achievement achievementUpgradeLumberjackMax;

    /**
     * Upgrade a miner to lv 1.
     */
    public static Achievement achievementBuildingMiner;
    /**
     * Max out a miner.
     */
    public static Achievement achievementUpgradeMinerMax;

    /**
     * Upgrade a fisher to lv 1.
     */
    public static Achievement achievementBuildingFisher;
    /**
     * Max out a fisher.
     */
    public static Achievement achievementUpgradeFisherMax;

    /**
     * Upgrade a farmer to lv 1.
     */
    public static Achievement achievementBuildingFarmer;
    /**
     * Max out a farmer.
     */
    public static Achievement achievementUpgradeFarmerMax;

    /**
     * Upgrade a guard to lv 1.
     */
    public static Achievement achievementBuildingGuard;
    /**
     * Max out a guard.
     */
    public static Achievement achievementUpgradeGuardMax;
    /**
     * Death achievements.
     */
    public static Achievement achievementMinerDeathLava;
    public static Achievement achievementMinerDeathFall;
    public static Achievement achievementLumberjackDeathTree;
    public static Achievement achievementFisherDeathGuardian;
    public static Achievement achievementGuardDeathEnderman;
    public static Achievement achievementPlayerDeathGuard;

    /**
     * Do something for the first time.
     */
    public static Achievement achievementBuildOneHut;
    public static Achievement achievementCatchOneFish;
    public static Achievement achievementKillOneMob;
    public static Achievement achievementMineOneOre;
    public static Achievement achievementMineOneDiamond;
    public static Achievement achievementFellOneTree;
    public static Achievement achievementPlantOneSapling;
    public static Achievement achievementHarvestOneCarrot;
    public static Achievement achievementHarvestOnePotato;
    public static Achievement achievementHarvestOneWheat;

    /**
     * Do something for the 25th time.
     */
    public static Achievement achievementBuild25Huts;
    public static Achievement achievementCatch25Fish;
    public static Achievement achievementKill25Mobs;
    public static Achievement achievementMine25Ores;
    public static Achievement achievementMine25Diamonds;
    public static Achievement achievementFell25Trees;
    public static Achievement achievementPlant25Saplings;
    public static Achievement achievementHarvest25Carrots;
    public static Achievement achievementHarvest25Potatoes;
    public static Achievement achievementHarvest25Wheat;

    /**
     * Do something for the 100th time.
     */
    public static Achievement achievementBuild100Huts;
    public static Achievement achievementCatch100Fish;
    public static Achievement achievementKill100Mobs;
    public static Achievement achievementMine100Ores;
    public static Achievement achievementMine100Diamonds;
    public static Achievement achievementFell100Trees;
    public static Achievement achievementPlant100Saplings;
    public static Achievement achievementHarvest100Carrots;
    public static Achievement achievementHarvest100Potatoes;
    public static Achievement achievementHarvest100Wheat;

    /**
     * Do something for the 500th time.
     */
    public static Achievement achievementBuild500Huts;
    public static Achievement achievementCatch500Fish;
    public static Achievement achievementKill500Mobs;
    public static Achievement achievementMine500Ores;
    public static Achievement achievementMine500Diamonds;
    public static Achievement achievementFell500Trees;
    public static Achievement achievementPlant500Saplings;
    public static Achievement achievementHarvest500Carrots;
    public static Achievement achievementHarvest500Potatoes;
    public static Achievement achievementHarvest500Wheat;

    /**
     * Do something for the 1000th time.
     */
    public static Achievement achievementBuild1000Huts;
    public static Achievement achievementCatch1000Fish;
    public static Achievement achievementKill1000Mobs;
    public static Achievement achievementMine1000Ores;
    public static Achievement achievementMine1000Diamonds;
    public static Achievement achievementFell1000Trees;
    public static Achievement achievementPlant1000Saplings;
    public static Achievement achievementHarvest1000Carrots;
    public static Achievement achievementHarvest1000Potatoes;
    public static Achievement achievementHarvest1000Wheat;

    // Sizes
    /**
     * Reach {@link Constants#ACHIEVEMENT_SIZE_SETTLEMENT} citizens.
     */
    public static Achievement achievementSizeSettlement;
    /**
     * Reach {@link Constants#ACHIEVEMENT_SIZE_TOWN} citizens.
     */
    public static Achievement achievementSizeTown;
    /**
     * Reach {@link Constants#ACHIEVEMENT_SIZE_CITY} citizens.
     */
    public static Achievement achievementSizeCity;
    /**
     * Reach {@link Constants#ACHIEVEMENT_SIZE_METROPOLIS} citizens.
     */
    public static Achievement achievementSizeMetropolis;

    // Achievement pages
    /**
     * The MineColonies achievement page.
     */
    public static AchievementPage achievementPageMineColonies;

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
