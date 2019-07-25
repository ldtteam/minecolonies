package com.minecolonies.coremod.colony.managers;

import com.minecolonies.coremod.achievements.MineColoniesAchievement;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.TriggerColonyAchievements;
import com.minecolonies.coremod.colony.managers.interfaces.IStatisticAchievementManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.CompoundNBT;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.ColonyConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.Suppression.COMMENTED_OUT_CODE_LINE;

@SuppressWarnings(COMMENTED_OUT_CODE_LINE)
public class StatisticAchievementManager implements IStatisticAchievementManager
{
    /**
     * Statistical values.
     */
    private int minedOres         = 0;
    private int minedDiamonds     = 0;
    private int harvestedWheat    = 0;
    private int harvestedPotatoes = 0;
    private int harvestedCarrots  = 0;
    private int killedMobs        = 0;
    private int builtHuts         = 0;
    private int caughtFish        = 0;
    private int felledTrees       = 0;
    private int plantedSaplings   = 0;

    /**
     * The colony of the manager.
     */
    private final Colony colony;

    /**
     * List of achievements within the colony.
     */
    @NotNull
    private final List<Advancement> colonyAchievements = new ArrayList<>();

    /**
     * Creates the Stat- and AchievementManager for a colony.
     * @param colony the colony.
     */
    public StatisticAchievementManager(final Colony colony)
    {
        this.colony = colony;
    }

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        // Restore colony achievements
        /*final ListNBT achievementTagList = compound.getList(TAG_ACHIEVEMENT_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < achievementTagList.size(); ++i)
        {
            final CompoundNBT achievementCompound = achievementTagList.getCompound(i);
            final String achievementKey = achievementCompound.getString(TAG_ACHIEVEMENT);

            final StatBase statBase = StatList.getOneShotStat(achievementKey);
             if (statBase instanceof Advancement)
            {
                colonyAchievements.add((Advancement) statBase);
            }
        }*/

        //Statistics
        final CompoundNBT statisticsCompound = compound.getCompound(TAG_STATISTICS);
        final CompoundNBT minerStatisticsCompound = statisticsCompound.getCompound(TAG_MINER_STATISTICS);
        final CompoundNBT farmerStatisticsCompound = statisticsCompound.getCompound(TAG_FARMER_STATISTICS);
        final CompoundNBT guardStatisticsCompound = statisticsCompound.getCompound(TAG_FARMER_STATISTICS);
        final CompoundNBT builderStatisticsCompound = statisticsCompound.getCompound(TAG_BUILDER_STATISTICS);
        final CompoundNBT fishermanStatisticsCompound = statisticsCompound.getCompound(TAG_FISHERMAN_STATISTICS);
        final CompoundNBT lumberjackStatisticsCompound = statisticsCompound.getCompound(TAG_LUMBERJACK_STATISTICS);
        minedOres = minerStatisticsCompound.getInt(TAG_MINER_ORES);
        minedDiamonds = minerStatisticsCompound.getInt(TAG_MINER_DIAMONDS);
        harvestedCarrots = farmerStatisticsCompound.getInt(TAG_FARMER_CARROTS);
        harvestedPotatoes = farmerStatisticsCompound.getInt(TAG_FARMER_POTATOES);
        harvestedWheat = farmerStatisticsCompound.getInt(TAG_FARMER_WHEAT);
        killedMobs = guardStatisticsCompound.getInt(TAG_GUARD_MOBS);
        builtHuts = builderStatisticsCompound.getInt(TAG_BUILDER_HUTS);
        caughtFish = fishermanStatisticsCompound.getInt(TAG_FISHERMAN_FISH);
        felledTrees = lumberjackStatisticsCompound.getInt(TAG_LUMBERJACK_TREES);
        plantedSaplings = lumberjackStatisticsCompound.getInt(TAG_LUMBERJACK_SAPLINGS);
    }

    @Override
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        /*//  Achievements
        @NotNull final ListNBT achievementsTagList = new ListNBT();
        for (@NotNull final Advancement achievement : this.colonyAchievements)
        {
            @NotNull final CompoundNBT achievementCompound = new CompoundNBT();
            achievementCompound.putString(TAG_ACHIEVEMENT, achievement.);
            achievementsTagList.add(achievementCompound);
        }
        compound.put(TAG_ACHIEVEMENT_LIST, achievementsTagList);*/

        // Statistics
        @NotNull final CompoundNBT statisticsCompound = new CompoundNBT();
        @NotNull final CompoundNBT minerStatisticsCompound = new CompoundNBT();
        @NotNull final CompoundNBT farmerStatisticsCompound = new CompoundNBT();
        @NotNull final CompoundNBT guardStatisticsCompound = new CompoundNBT();
        @NotNull final CompoundNBT builderStatisticsCompound = new CompoundNBT();
        @NotNull final CompoundNBT fishermanStatisticsCompound = new CompoundNBT();
        @NotNull final CompoundNBT lumberjackStatisticsCompound = new CompoundNBT();
        compound.put(TAG_STATISTICS, statisticsCompound);
        statisticsCompound.put(TAG_MINER_STATISTICS, minerStatisticsCompound);
        minerStatisticsCompound.putInt(TAG_MINER_ORES, minedOres);
        minerStatisticsCompound.putInt(TAG_MINER_DIAMONDS, minedDiamonds);
        statisticsCompound.put(TAG_FARMER_STATISTICS, farmerStatisticsCompound);
        farmerStatisticsCompound.putInt(TAG_FARMER_CARROTS, harvestedCarrots);
        farmerStatisticsCompound.putInt(TAG_FARMER_POTATOES, harvestedPotatoes);
        farmerStatisticsCompound.putInt(TAG_FARMER_WHEAT, harvestedWheat);
        statisticsCompound.put(TAG_GUARD_STATISTICS, guardStatisticsCompound);
        guardStatisticsCompound.putInt(TAG_GUARD_MOBS, killedMobs);
        statisticsCompound.put(TAG_BUILDER_STATISTICS, builderStatisticsCompound);
        builderStatisticsCompound.putInt(TAG_BUILDER_HUTS, builtHuts);
        statisticsCompound.put(TAG_FISHERMAN_STATISTICS, fishermanStatisticsCompound);
        fishermanStatisticsCompound.putInt(TAG_FISHERMAN_FISH, caughtFish);
        statisticsCompound.put(TAG_LUMBERJACK_STATISTICS, lumberjackStatisticsCompound);
        lumberjackStatisticsCompound.putInt(TAG_LUMBERJACK_TREES, felledTrees);
        lumberjackStatisticsCompound.putInt(TAG_LUMBERJACK_SAPLINGS, plantedSaplings);
    }

    @Override
    public void incrementStatistic(@NotNull final String stat)
    {
        final int statisticAmount = this.getStatisticAmount(stat);
        incrementStatisticAmount(stat);
        if (statisticAmount >= NUM_ACHIEVEMENT_FIRST)
        {
            TriggerColonyAchievements.triggerFirstAchievement(stat, colony);
        }
        if (statisticAmount >= NUM_ACHIEVEMENT_SECOND)
        {
            TriggerColonyAchievements.triggerSecondAchievement(stat, colony);
        }
        if (statisticAmount >= NUM_ACHIEVEMENT_THIRD)
        {
            TriggerColonyAchievements.triggerThirdAchievement(stat, colony);
        }
        if (statisticAmount >= NUM_ACHIEVEMENT_FOURTH)
        {
            TriggerColonyAchievements.triggerFourthAchievement(stat, colony);
        }
        if (statisticAmount >= NUM_ACHIEVEMENT_FIFTH)
        {
            TriggerColonyAchievements.triggerFifthAchievement(stat, colony);
        }
    }

    @Override
    public List<Advancement> getAchievements()
    {
        return new ArrayList<>(colonyAchievements);
    }

    /**
     * Get the amount of statistic.
     *
     * @param statistic the statistic.
     * @return amount of statistic.
     */
    private int getStatisticAmount(@NotNull final String statistic)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                return killedMobs;
            case TAG_MINER_ORES:
                return minedOres;
            case TAG_MINER_DIAMONDS:
                return minedDiamonds;
            case TAG_BUILDER_HUTS:
                return builtHuts;
            case TAG_FISHERMAN_FISH:
                return caughtFish;
            case TAG_FARMER_WHEAT:
                return harvestedWheat;
            case TAG_FARMER_POTATOES:
                return harvestedPotatoes;
            case TAG_FARMER_CARROTS:
                return harvestedCarrots;
            case TAG_LUMBERJACK_SAPLINGS:
                return plantedSaplings;
            case TAG_LUMBERJACK_TREES:
                return felledTrees;
            default:
                return 0;
        }
    }

    /**
     * increment statistic amount.
     *
     * @param statistic the statistic.
     */
    private void incrementStatisticAmount(@NotNull final String statistic)
    {
        switch (statistic)
        {
            case TAG_GUARD_MOBS:
                killedMobs++;
                break;
            case TAG_MINER_ORES:
                minedOres++;
                break;
            case TAG_MINER_DIAMONDS:
                minedDiamonds++;
                break;
            case TAG_BUILDER_HUTS:
                builtHuts++;
                break;
            case TAG_FISHERMAN_FISH:
                caughtFish++;
                break;
            case TAG_FARMER_WHEAT:
                harvestedWheat++;
                break;
            case TAG_FARMER_POTATOES:
                harvestedPotatoes++;
                break;
            case TAG_FARMER_CARROTS:
                harvestedCarrots++;
                break;
            case TAG_LUMBERJACK_SAPLINGS:
                plantedSaplings++;
                break;
            case TAG_LUMBERJACK_TREES:
                felledTrees++;
                break;
            default:
                break;
        }
    }

    @Override
    public void checkAchievements()
    {
        // the colonies size
        //final int size = this.citizenManager.getAssignedCitizen().size();

        //todo check those later again
        /*if (size >= ModAchievements.ACHIEVEMENT_SIZE_SETTLEMENT)
        {
            this.triggerAchievement(ModAchievements.achievementSizeSettlement);
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_TOWN)
        {
            this.triggerAchievement(ModAchievements.achievementSizeTown);
        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_CITY)
        {
            this.triggerAchievement(ModAchievements.achievementSizeCity);

        }

        if (size >= ModAchievements.ACHIEVEMENT_SIZE_METROPOLIS)
        {
            this.triggerAchievement(ModAchievements.achievementSizeMetropolis);
        }*/
    }
    
    @Override
    public void triggerAchievement(@NotNull final MineColoniesAchievement achievement)
    {
        /*if (this.colonyAchievements.contains(achievement))
        {
            return;
        }

        this.colonyAchievements.add(achievement);*/

        //AchievementUtils.syncAchievements(colony);
        //colony.markDirty();
    }
}
