package com.minecolonies.coremod.achievements;

import net.minecraft.advancements.Advancement;

/**
 * This class is the superclass of our achievement pages.
 * <p>
 * Constructors exist to make creating new achievement pages easy.
 *
 * @since 0.2
 */
public class MineColoniesAchievementPage
{
    //todo have to extend advancement at some point

    /**
     * Create a new achievement page.
     *  @param name         The name this page should have
     * @param achievementGetSupply
     * @param achievementWandOfbuilding
     * @param achievementTownhall
     * @param achievementBuildingTownhall
     * @param achievementUpgradeTownhallMax
     * @param achievementBuildingBuilder
     * @param achievementBuildingColonist
     * @param achievementBuildingLumberjack
     * @param achievementBuildingMiner
     * @param achievementBuildingFisher
     * @param achievementSizeSettlement
     * @param achievementSizeTown
     * @param achievementSizeCity
     * @param achievementUpgradeColonistMax
     * @param achievementUpgradeBuilderMax
     * @param achievementUpgradeLumberjackMax
     * @param achievementUpgradeMinerMax
     * @param achievementUpgradeFisherMax
     * @param achievementSizeMetropolis
     * @param achievementBuildingFarmer
     * @param achievementUpgradeFarmerMax
     * @param achievementBuildingGuard
     * @param achievementUpgradeGuardMax
     * @param achievementKillOneMob
     * @param achievementKill25Mobs
     * @param achievementKill100Mobs
     * @param achievementKill500Mobs
     * @param achievementKill1000Mobs
     * @param achievementMineOneOre
     * @param achievementMine25Ores
     * @param achievementMine100Ores
     * @param achievementMine500Ores
     * @param achievementMine1000Ores
     * @param achievementMineOneDiamond
     * @param achievementMine25Diamonds
     * @param achievementMine100Diamonds
     * @param achievementMine500Diamonds
     * @param achievementMine1000Diamonds
     * @param achievementBuildOneHut
     * @param achievementBuild25Huts
     * @param achievementBuild100Huts
     * @param achievementBuild500Huts
     * @param achievementBuild1000Huts
     * @param achievementCatchOneFish
     * @param achievementCatch25Fish
     * @param achievementCatch100Fish
     * @param achievementCatch500Fish
     * @param achievementCatch1000Fish
     * @param achievementHarvestOneCarrot
     * @param achievementHarvest25Carrots
     * @param achievementHarvest100Carrots
     * @param achievementHarvest500Carrots
     * @param achievementHarvest1000Carrots
     * @param achievementHarvestOnePotato
     * @param achievementHarvest25Potatoes
     * @param achievementHarvest100Potatoes
     * @param achievementHarvest500Potatoes
     * @param achievementHarvest1000Potatoes
     * @param achievementHarvestOneWheat
     * @param achievementHarvest25Wheat
     * @param achievementHarvest100Wheat
     * @param achievementHarvest500Wheat
     * @param achievementHarvest1000Wheat
     * @param achievementFellOneTree
     * @param achievementFell25Trees
     * @param achievementFell100Trees
     * @param achievementFell500Trees
     * @param achievementFell1000Trees
     * @param achievementPlantOneSapling
     * @param achievementPlant25Saplings
     * @param achievementPlant100Saplings
     * @param achievementPlant500Saplings
     * @param achievementPlant1000Saplings
     * @param achievementMinerDeathLava
     * @param achievementMinerDeathFall
     * @param achievementLumberjackDeathTree
     * @param achievementFisherDeathGuardian
     * @param achievementGuardDeathEnderman
     * @param achievementPlayerDeathGuard
     * @param achievements A list of achievements to display
     */
    public MineColoniesAchievementPage(
            final String name,
            final MineColoniesAchievement achievementGetSupply,
            final MineColoniesAchievement achievementTownhall,
            final MineColoniesAchievement achievementBuildingTownhall,
            final MineColoniesAchievement achievementUpgradeTownhallMax,
            final MineColoniesAchievement achievementBuildingBuilder,
            final MineColoniesAchievement achievementBuildingColonist,
            final MineColoniesAchievement achievementBuildingLumberjack,
            final MineColoniesAchievement achievementBuildingMiner,
            final MineColoniesAchievement achievementBuildingFisher,
            final MineColoniesAchievement achievementSizeSettlement,
            final MineColoniesAchievement achievementSizeTown,
            final MineColoniesAchievement achievementSizeCity,
            final MineColoniesAchievement achievementUpgradeColonistMax,
            final MineColoniesAchievement achievementUpgradeBuilderMax,
            final MineColoniesAchievement achievementUpgradeLumberjackMax,
            final MineColoniesAchievement achievementUpgradeMinerMax,
            final MineColoniesAchievement achievementUpgradeFisherMax,
            final MineColoniesAchievement achievementSizeMetropolis,
            final MineColoniesAchievement achievementBuildingFarmer,
            final MineColoniesAchievement achievementUpgradeFarmerMax,
            final MineColoniesAchievement achievementBuildingGuard,
            final MineColoniesAchievement achievementUpgradeGuardMax,
            final MineColoniesAchievement achievementKillOneMob,
            final MineColoniesAchievement achievementKill25Mobs,
            final MineColoniesAchievement achievementKill100Mobs,
            final MineColoniesAchievement achievementKill500Mobs,
            final MineColoniesAchievement achievementKill1000Mobs,
            final MineColoniesAchievement achievementMineOneOre,
            final MineColoniesAchievement achievementMine25Ores,
            final MineColoniesAchievement achievementMine100Ores,
            final MineColoniesAchievement achievementMine500Ores,
            final MineColoniesAchievement achievementMine1000Ores,
            final MineColoniesAchievement achievementMineOneDiamond,
            final MineColoniesAchievement achievementMine25Diamonds,
            final MineColoniesAchievement achievementMine100Diamonds,
            final MineColoniesAchievement achievementMine500Diamonds,
            final MineColoniesAchievement achievementMine1000Diamonds,
            final MineColoniesAchievement achievementBuildOneHut,
            final MineColoniesAchievement achievementBuild25Huts,
            final MineColoniesAchievement achievementBuild100Huts,
            final MineColoniesAchievement achievementBuild500Huts,
            final MineColoniesAchievement achievementBuild1000Huts,
            final MineColoniesAchievement achievementCatchOneFish,
            final MineColoniesAchievement achievementCatch25Fish,
            final MineColoniesAchievement achievementCatch100Fish,
            final MineColoniesAchievement achievementCatch500Fish,
            final MineColoniesAchievement achievementCatch1000Fish,
            final MineColoniesAchievement achievementHarvestOneCarrot,
            final MineColoniesAchievement achievementHarvest25Carrots,
            final MineColoniesAchievement achievementHarvest100Carrots,
            final MineColoniesAchievement achievementHarvest500Carrots,
            final MineColoniesAchievement achievementHarvest1000Carrots,
            final MineColoniesAchievement achievementHarvestOnePotato,
            final MineColoniesAchievement achievementHarvest25Potatoes,
            final MineColoniesAchievement achievementHarvest100Potatoes,
            final MineColoniesAchievement achievementHarvest500Potatoes,
            final MineColoniesAchievement achievementHarvest1000Potatoes,
            final MineColoniesAchievement achievementHarvestOneWheat,
            final MineColoniesAchievement achievementHarvest25Wheat,
            final MineColoniesAchievement achievementHarvest100Wheat,
            final MineColoniesAchievement achievementHarvest500Wheat,
            final MineColoniesAchievement achievementHarvest1000Wheat,
            final MineColoniesAchievement achievementFellOneTree,
            final MineColoniesAchievement achievementFell25Trees,
            final MineColoniesAchievement achievementFell100Trees,
            final MineColoniesAchievement achievementFell500Trees,
            final MineColoniesAchievement achievementFell1000Trees,
            final MineColoniesAchievement achievementPlantOneSapling,
            final MineColoniesAchievement achievementPlant25Saplings,
            final MineColoniesAchievement achievementPlant100Saplings,
            final MineColoniesAchievement achievementPlant500Saplings,
            final MineColoniesAchievement achievementPlant1000Saplings,
            final MineColoniesAchievement achievementMinerDeathLava,
            final MineColoniesAchievement achievementMinerDeathFall,
            final MineColoniesAchievement achievementLumberjackDeathTree,
            final MineColoniesAchievement achievementFisherDeathGuardian,
            final MineColoniesAchievement achievementGuardDeathEnderman,
            final MineColoniesAchievement achievementPlayerDeathGuard,
            final Advancement... achievements)
    {
        //todo will have to call super
    }
}
