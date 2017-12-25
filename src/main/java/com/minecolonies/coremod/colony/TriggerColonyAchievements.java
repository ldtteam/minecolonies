package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.achievements.ModAchievements;
import org.jetbrains.annotations.NotNull;

/**
 * Trigger the corresponding colony achievement.
 */
public final class TriggerColonyAchievements
{
    private static final String TAG_LUMBERJACK_SAPLINGS = "saplings";
    private static final String TAG_LUMBERJACK_TREES    = "trees";
    private static final String TAG_FISHERMAN_FISH      = "fish";
    private static final String TAG_BUILDER_HUTS        = "huts";
    private static final String TAG_GUARD_MOBS          = "mobs";
    private static final String TAG_FARMER_CARROTS      = "carrots";
    private static final String TAG_FARMER_POTATOES     = "potatoes";
    private static final String TAG_FARMER_WHEAT        = "wheat";
    private static final String TAG_MINER_DIAMONDS      = "diamonds";
    private static final String TAG_MINER_ORES          = "ores";

    private TriggerColonyAchievements()
    {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Trigger fifth achievement.
     *
     * @param statistic the statistic.
     * @param colony    the colony.
     */
    public static void triggerFirstAchievement(@NotNull final String statistic, @NotNull final Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementKillOneMob, colony);
                break;
            case TAG_MINER_ORES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMineOneOre, colony);
                break;
            case TAG_MINER_DIAMONDS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMineOneDiamond, colony);
                break;
            case TAG_BUILDER_HUTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementBuildOneHut, colony);
                break;
            case TAG_FISHERMAN_FISH:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementCatchOneFish, colony);
                break;
            case TAG_FARMER_WHEAT:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvestOneWheat, colony);
                break;
            case TAG_FARMER_POTATOES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvestOnePotato, colony);
                break;
            case TAG_FARMER_CARROTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvestOneCarrot, colony);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementPlantOneSapling, colony);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementFellOneTree, colony);
                break;
            default:
                break;
        }
    }

    /**
     * Trigger fifth achievement.
     *
     * @param statistic the statistic.
     * @param colony    the colony.
     */
    public static void triggerSecondAchievement(@NotNull final String statistic, @NotNull final Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementKill25Mobs, colony);
                break;
            case TAG_MINER_ORES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMine25Ores, colony);
                break;
            case TAG_MINER_DIAMONDS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMine25Diamonds, colony);
                break;
            case TAG_BUILDER_HUTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementBuild25Huts, colony);
                break;
            case TAG_FISHERMAN_FISH:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementCatch25Fish, colony);
                break;
            case TAG_FARMER_WHEAT:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest25Wheat, colony);
                break;
            case TAG_FARMER_POTATOES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest25Potatoes, colony);
                break;
            case TAG_FARMER_CARROTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest25Carrots, colony);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementPlant25Saplings, colony);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementFell25Trees, colony);
                break;
            default:
                break;
        }
    }

    /**
     * Trigger fifth achievement.
     *
     * @param statistic the statistic.
     * @param colony    the colony.
     */
    public static void triggerThirdAchievement(@NotNull final String statistic, @NotNull final Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementKill100Mobs, colony);
                break;
            case TAG_MINER_ORES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMine100Ores, colony);
                break;
            case TAG_MINER_DIAMONDS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMine100Diamonds, colony);
                break;
            case TAG_BUILDER_HUTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementBuild100Huts, colony);
                break;
            case TAG_FISHERMAN_FISH:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementCatch100Fish, colony);
                break;
            case TAG_FARMER_WHEAT:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest100Wheat, colony);
                break;
            case TAG_FARMER_POTATOES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest100Potatoes, colony);
                break;
            case TAG_FARMER_CARROTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest100Carrots, colony);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementPlant100Saplings, colony);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementFell100Trees, colony);
                break;
            default:
                break;
        }
    }

    /**
     * Trigger fifth achievement.
     *
     * @param statistic the statistic.
     * @param colony    the colony.
     */
    public static void triggerFourthAchievement(@NotNull final String statistic, @NotNull final Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementKill500Mobs, colony);
                break;
            case TAG_MINER_ORES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMine500Ores, colony);
                break;
            case TAG_MINER_DIAMONDS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMine500Diamonds, colony);
                break;
            case TAG_BUILDER_HUTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementBuild500Huts, colony);
                break;
            case TAG_FISHERMAN_FISH:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementCatch500Fish, colony);
                break;
            case TAG_FARMER_WHEAT:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest500Wheat, colony);
                break;
            case TAG_FARMER_POTATOES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest500Potatoes, colony);
                break;
            case TAG_FARMER_CARROTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest500Carrots, colony);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementPlant500Saplings, colony);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementFell500Trees, colony);
                break;
            default:
                break;
        }
    }

    /**
     * Trigger fifth achievement.
     *
     * @param statistic the statistic.
     * @param colony    the colony.
     */
    public static void triggerFifthAchievement(@NotNull final String statistic, @NotNull final Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementKill1000Mobs, colony);
                break;
            case TAG_MINER_ORES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMine1000Ores, colony);
                break;
            case TAG_MINER_DIAMONDS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementMine1000Diamonds, colony);
                break;
            case TAG_BUILDER_HUTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementBuild1000Huts, colony);
                break;
            case TAG_FISHERMAN_FISH:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementCatch1000Fish, colony);
                break;
            case TAG_FARMER_WHEAT:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest1000Wheat, colony);
                break;
            case TAG_FARMER_POTATOES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest1000Potatoes, colony);
                break;
            case TAG_FARMER_CARROTS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementHarvest1000Carrots, colony);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementPlant1000Saplings, colony);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.getStatsManager().triggerAchievement(ModAchievements.achievementFell1000Trees, colony);
                break;
            default:
                break;
        }
    }
}
