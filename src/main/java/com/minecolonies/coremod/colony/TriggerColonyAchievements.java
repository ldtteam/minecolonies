package com.minecolonies.coremod.colony;

import com.minecolonies.coremod.achievements.ModAchievements;
import org.jetbrains.annotations.NotNull;

/**
 * Trigger the corresponding colony achievement.
 */
public final class TriggerColonyAchievements
{
    private static final String TAG_LUMBERJACK_SAPLINGS   = "saplings";
    private static final String TAG_LUMBERJACK_TREES      = "trees";
    private static final String TAG_FISHERMAN_FISH        = "fish";
    private static final String TAG_BUILDER_HUTS          = "huts";
    private static final String TAG_GUARD_MOBS            = "mobs";
    private static final String TAG_FARMER_CARROTS        = "carrots";
    private static final String TAG_FARMER_POTATOES       = "potatoes";
    private static final String TAG_FARMER_WHEAT          = "wheat";
    private static final String TAG_MINER_DIAMONDS        = "diamonds";
    private static final String TAG_MINER_ORES            = "ores";

    private TriggerColonyAchievements()
    {
        throw new IllegalAccessError("Utility class");
    }

    /**
     *Trigger fifth achievement.
     * @param statistic the statistic.
     * @param colony the colony.
     */
    public static void triggerFirstAchievement(@NotNull String statistic, @NotNull Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.triggerAchievement(ModAchievements.achievementKillOneMob);
                break;
            case TAG_MINER_ORES:
                colony.triggerAchievement(ModAchievements.achievementMineOneOre);
                break;
            case TAG_MINER_DIAMONDS:
                colony.triggerAchievement(ModAchievements.achievementMineOneDiamond);
                break;
            case TAG_BUILDER_HUTS:
                colony.triggerAchievement(ModAchievements.achievementBuildOneHut);
                break;
            case TAG_FISHERMAN_FISH:
                colony.triggerAchievement(ModAchievements.achievementCatchOneFish);
                break;
            case TAG_FARMER_WHEAT:
                colony.triggerAchievement(ModAchievements.achievementHarvestOneWheat);
                break;
            case TAG_FARMER_POTATOES:
                colony.triggerAchievement(ModAchievements.achievementHarvestOnePotato);
                break;
            case TAG_FARMER_CARROTS:
                colony.triggerAchievement(ModAchievements.achievementHarvestOneCarrot);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.triggerAchievement(ModAchievements.achievementPlantOneSapling);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.triggerAchievement(ModAchievements.achievementFellOneTree);
                break;
            default:
                break;
        }
    }

    /**
     *Trigger fifth achievement.
     * @param statistic the statistic.
     * @param colony the colony.
     */
    public static void triggerSecondAchievement(@NotNull String statistic, @NotNull Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.triggerAchievement(ModAchievements.achievementKill25Mobs);
                break;
            case TAG_MINER_ORES:
                colony.triggerAchievement(ModAchievements.achievementMine25Ores);
                break;
            case TAG_MINER_DIAMONDS:
                colony.triggerAchievement(ModAchievements.achievementMine25Diamonds);
                break;
            case TAG_BUILDER_HUTS:
                colony.triggerAchievement(ModAchievements.achievementBuild25Huts);
                break;
            case TAG_FISHERMAN_FISH:
                colony.triggerAchievement(ModAchievements.achievementCatch25Fish);
                break;
            case TAG_FARMER_WHEAT:
                colony.triggerAchievement(ModAchievements.achievementHarvest25Wheat);
                break;
            case TAG_FARMER_POTATOES:
                colony.triggerAchievement(ModAchievements.achievementHarvest25Potatoes);
                break;
            case TAG_FARMER_CARROTS:
                colony.triggerAchievement(ModAchievements.achievementHarvest25Carrots);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.triggerAchievement(ModAchievements.achievementPlant25Saplings);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.triggerAchievement(ModAchievements.achievementFell25Trees);
                break;
            default:
                break;
        }
    }

    /**
     *Trigger fifth achievement.
     * @param statistic the statistic.
     * @param colony the colony.
     */
    public static void triggerThirdAchievement(@NotNull String statistic, @NotNull Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.triggerAchievement(ModAchievements.achievementKill100Mobs);
                break;
            case TAG_MINER_ORES:
                colony.triggerAchievement(ModAchievements.achievementMine100Ores);
                break;
            case TAG_MINER_DIAMONDS:
                colony.triggerAchievement(ModAchievements.achievementMine100Diamonds);
                break;
            case TAG_BUILDER_HUTS:
                colony.triggerAchievement(ModAchievements.achievementBuild100Huts);
                break;
            case TAG_FISHERMAN_FISH:
                colony.triggerAchievement(ModAchievements.achievementCatch100Fish);
                break;
            case TAG_FARMER_WHEAT:
                colony.triggerAchievement(ModAchievements.achievementHarvest100Wheat);
                break;
            case TAG_FARMER_POTATOES:
                colony.triggerAchievement(ModAchievements.achievementHarvest100Potatoes);
                break;
            case TAG_FARMER_CARROTS:
                colony.triggerAchievement(ModAchievements.achievementHarvest100Carrots);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.triggerAchievement(ModAchievements.achievementPlant100Saplings);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.triggerAchievement(ModAchievements.achievementFell100Trees);
                break;
            default:
                break;
        }
    }

    /**
     *Trigger fifth achievement.
     * @param statistic the statistic.
     * @param colony the colony.
     */
    public static void triggerFourthAchievement(@NotNull String statistic, @NotNull Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.triggerAchievement(ModAchievements.achievementKill500Mobs);
                break;
            case TAG_MINER_ORES:
                colony.triggerAchievement(ModAchievements.achievementMine500Ores);
                break;
            case TAG_MINER_DIAMONDS:
                colony.triggerAchievement(ModAchievements.achievementMine500Diamonds);
                break;
            case TAG_BUILDER_HUTS:
                colony.triggerAchievement(ModAchievements.achievementBuild500Huts);
                break;
            case TAG_FISHERMAN_FISH:
                colony.triggerAchievement(ModAchievements.achievementCatch500Fish);
                break;
            case TAG_FARMER_WHEAT:
                colony.triggerAchievement(ModAchievements.achievementHarvest500Wheat);
                break;
            case TAG_FARMER_POTATOES:
                colony.triggerAchievement(ModAchievements.achievementHarvest500Potatoes);
                break;
            case TAG_FARMER_CARROTS:
                colony.triggerAchievement(ModAchievements.achievementHarvest500Carrots);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.triggerAchievement(ModAchievements.achievementPlant500Saplings);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.triggerAchievement(ModAchievements.achievementFell500Trees);
                break;
            default:
                break;
        }
    }

    /**
     *Trigger fifth achievement.
     * @param statistic the statistic.
     * @param colony the colony.
     */
    public static void triggerFifthAchievement(@NotNull String statistic, @NotNull Colony colony)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                colony.triggerAchievement(ModAchievements.achievementKill1000Mobs);
                break;
            case TAG_MINER_ORES:
                colony.triggerAchievement(ModAchievements.achievementMine1000Ores);
                break;
            case TAG_MINER_DIAMONDS:
                colony.triggerAchievement(ModAchievements.achievementMine1000Diamonds);
                break;
            case TAG_BUILDER_HUTS:
                colony.triggerAchievement(ModAchievements.achievementBuild1000Huts);
                break;
            case TAG_FISHERMAN_FISH:
                colony.triggerAchievement(ModAchievements.achievementCatch1000Fish);
                break;
            case TAG_FARMER_WHEAT:
                colony.triggerAchievement(ModAchievements.achievementHarvest1000Wheat);
                break;
            case TAG_FARMER_POTATOES:
                colony.triggerAchievement(ModAchievements.achievementHarvest1000Potatoes);
                break;
            case TAG_FARMER_CARROTS:
                colony.triggerAchievement(ModAchievements.achievementHarvest1000Carrots);
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                colony.triggerAchievement(ModAchievements.achievementPlant1000Saplings);
                break;
            case TAG_LUMBERJACK_TREES:
                colony.triggerAchievement(ModAchievements.achievementFell1000Trees);
                break;
            default:
                break;
        }
    }
}
