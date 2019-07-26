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
            final IMineColoniesAchievement achievementGetSupply,
            final IMineColoniesAchievement achievementTownhall,
            final IMineColoniesAchievement achievementBuildingTownhall,
            final IMineColoniesAchievement achievementUpgradeTownhallMax,
            final IMineColoniesAchievement achievementBuildingBuilder,
            final IMineColoniesAchievement achievementBuildingColonist,
            final IMineColoniesAchievement achievementBuildingLumberjack,
            final IMineColoniesAchievement achievementBuildingMiner,
            final IMineColoniesAchievement achievementBuildingFisher,
            final IMineColoniesAchievement achievementSizeSettlement,
            final IMineColoniesAchievement achievementSizeTown,
            final IMineColoniesAchievement achievementSizeCity,
            final IMineColoniesAchievement achievementUpgradeColonistMax,
            final IMineColoniesAchievement achievementUpgradeBuilderMax,
            final IMineColoniesAchievement achievementUpgradeLumberjackMax,
            final IMineColoniesAchievement achievementUpgradeMinerMax,
            final IMineColoniesAchievement achievementUpgradeFisherMax,
            final IMineColoniesAchievement achievementSizeMetropolis,
            final IMineColoniesAchievement achievementBuildingFarmer,
            final IMineColoniesAchievement achievementUpgradeFarmerMax,
            final IMineColoniesAchievement achievementBuildingGuard,
            final IMineColoniesAchievement achievementUpgradeGuardMax,
            final IMineColoniesAchievement achievementKillOneMob,
            final IMineColoniesAchievement achievementKill25Mobs,
            final IMineColoniesAchievement achievementKill100Mobs,
            final IMineColoniesAchievement achievementKill500Mobs,
            final IMineColoniesAchievement achievementKill1000Mobs,
            final IMineColoniesAchievement achievementMineOneOre,
            final IMineColoniesAchievement achievementMine25Ores,
            final IMineColoniesAchievement achievementMine100Ores,
            final IMineColoniesAchievement achievementMine500Ores,
            final IMineColoniesAchievement achievementMine1000Ores,
            final IMineColoniesAchievement achievementMineOneDiamond,
            final IMineColoniesAchievement achievementMine25Diamonds,
            final IMineColoniesAchievement achievementMine100Diamonds,
            final IMineColoniesAchievement achievementMine500Diamonds,
            final IMineColoniesAchievement achievementMine1000Diamonds,
            final IMineColoniesAchievement achievementBuildOneHut,
            final IMineColoniesAchievement achievementBuild25Huts,
            final IMineColoniesAchievement achievementBuild100Huts,
            final IMineColoniesAchievement achievementBuild500Huts,
            final IMineColoniesAchievement achievementBuild1000Huts,
            final IMineColoniesAchievement achievementCatchOneFish,
            final IMineColoniesAchievement achievementCatch25Fish,
            final IMineColoniesAchievement achievementCatch100Fish,
            final IMineColoniesAchievement achievementCatch500Fish,
            final IMineColoniesAchievement achievementCatch1000Fish,
            final IMineColoniesAchievement achievementHarvestOneCarrot,
            final IMineColoniesAchievement achievementHarvest25Carrots,
            final IMineColoniesAchievement achievementHarvest100Carrots,
            final IMineColoniesAchievement achievementHarvest500Carrots,
            final IMineColoniesAchievement achievementHarvest1000Carrots,
            final IMineColoniesAchievement achievementHarvestOnePotato,
            final IMineColoniesAchievement achievementHarvest25Potatoes,
            final IMineColoniesAchievement achievementHarvest100Potatoes,
            final IMineColoniesAchievement achievementHarvest500Potatoes,
            final IMineColoniesAchievement achievementHarvest1000Potatoes,
            final IMineColoniesAchievement achievementHarvestOneWheat,
            final IMineColoniesAchievement achievementHarvest25Wheat,
            final IMineColoniesAchievement achievementHarvest100Wheat,
            final IMineColoniesAchievement achievementHarvest500Wheat,
            final IMineColoniesAchievement achievementHarvest1000Wheat,
            final IMineColoniesAchievement achievementFellOneTree,
            final IMineColoniesAchievement achievementFell25Trees,
            final IMineColoniesAchievement achievementFell100Trees,
            final IMineColoniesAchievement achievementFell500Trees,
            final IMineColoniesAchievement achievementFell1000Trees,
            final IMineColoniesAchievement achievementPlantOneSapling,
            final IMineColoniesAchievement achievementPlant25Saplings,
            final IMineColoniesAchievement achievementPlant100Saplings,
            final IMineColoniesAchievement achievementPlant500Saplings,
            final IMineColoniesAchievement achievementPlant1000Saplings,
            final IMineColoniesAchievement achievementMinerDeathLava,
            final IMineColoniesAchievement achievementMinerDeathFall,
            final IMineColoniesAchievement achievementLumberjackDeathTree,
            final IMineColoniesAchievement achievementFisherDeathGuardian,
            final IMineColoniesAchievement achievementGuardDeathEnderman,
            final IMineColoniesAchievement achievementPlayerDeathGuard,
            final Advancement... achievements)
    {
        //todo will have to call super
    }
}
