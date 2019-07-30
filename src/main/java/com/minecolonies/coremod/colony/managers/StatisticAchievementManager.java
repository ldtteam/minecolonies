package com.minecolonies.coremod.colony.managers;

import com.minecolonies.coremod.achievements.MineColoniesAchievement;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.TriggerColonyAchievements;
import com.minecolonies.coremod.colony.managers.interfaces.IStatisticAchievementManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.NBTTagCompound;
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
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        // Restore colony achievements
        /*final NBTTagList achievementTagList = compound.getTagList(TAG_ACHIEVEMENT_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < achievementTagList.tagCount(); ++i)
        {
            final NBTTagCompound achievementCompound = achievementTagList.getCompoundTagAt(i);
            final String achievementKey = achievementCompound.getString(TAG_ACHIEVEMENT);

            final StatBase statBase = StatList.getOneShotStat(achievementKey);
             if (statBase instanceof Advancement)
            {
                colonyAchievements.add((Advancement) statBase);
            }
        }*/

        //Statistics
        final NBTTagCompound statisticsCompound = compound.getCompoundTag(TAG_STATISTICS);
        final NBTTagCompound minerStatisticsCompound = statisticsCompound.getCompoundTag(TAG_MINER_STATISTICS);
        final NBTTagCompound farmerStatisticsCompound = statisticsCompound.getCompoundTag(TAG_FARMER_STATISTICS);
        final NBTTagCompound guardStatisticsCompound = statisticsCompound.getCompoundTag(TAG_FARMER_STATISTICS);
        final NBTTagCompound builderStatisticsCompound = statisticsCompound.getCompoundTag(TAG_BUILDER_STATISTICS);
        final NBTTagCompound fishermanStatisticsCompound = statisticsCompound.getCompoundTag(TAG_FISHERMAN_STATISTICS);
        final NBTTagCompound lumberjackStatisticsCompound = statisticsCompound.getCompoundTag(TAG_LUMBERJACK_STATISTICS);
        minedOres = minerStatisticsCompound.getInteger(TAG_MINER_ORES);
        minedDiamonds = minerStatisticsCompound.getInteger(TAG_MINER_DIAMONDS);
        harvestedCarrots = farmerStatisticsCompound.getInteger(TAG_FARMER_CARROTS);
        harvestedPotatoes = farmerStatisticsCompound.getInteger(TAG_FARMER_POTATOES);
        harvestedWheat = farmerStatisticsCompound.getInteger(TAG_FARMER_WHEAT);
        killedMobs = guardStatisticsCompound.getInteger(TAG_GUARD_MOBS);
        builtHuts = builderStatisticsCompound.getInteger(TAG_BUILDER_HUTS);
        caughtFish = fishermanStatisticsCompound.getInteger(TAG_FISHERMAN_FISH);
        felledTrees = lumberjackStatisticsCompound.getInteger(TAG_LUMBERJACK_TREES);
        plantedSaplings = lumberjackStatisticsCompound.getInteger(TAG_LUMBERJACK_SAPLINGS);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        /*//  Achievements
        @NotNull final NBTTagList achievementsTagList = new NBTTagList();
        for (@NotNull final Advancement achievement : this.colonyAchievements)
        {
            @NotNull final NBTTagCompound achievementCompound = new NBTTagCompound();
            achievementCompound.setString(TAG_ACHIEVEMENT, achievement.);
            achievementsTagList.appendTag(achievementCompound);
        }
        compound.setTag(TAG_ACHIEVEMENT_LIST, achievementsTagList);*/

        // Statistics
        @NotNull final NBTTagCompound statisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound minerStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound farmerStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound guardStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound builderStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound fishermanStatisticsCompound = new NBTTagCompound();
        @NotNull final NBTTagCompound lumberjackStatisticsCompound = new NBTTagCompound();
        compound.setTag(TAG_STATISTICS, statisticsCompound);
        statisticsCompound.setTag(TAG_MINER_STATISTICS, minerStatisticsCompound);
        minerStatisticsCompound.setInteger(TAG_MINER_ORES, minedOres);
        minerStatisticsCompound.setInteger(TAG_MINER_DIAMONDS, minedDiamonds);
        statisticsCompound.setTag(TAG_FARMER_STATISTICS, farmerStatisticsCompound);
        farmerStatisticsCompound.setInteger(TAG_FARMER_CARROTS, harvestedCarrots);
        farmerStatisticsCompound.setInteger(TAG_FARMER_POTATOES, harvestedPotatoes);
        farmerStatisticsCompound.setInteger(TAG_FARMER_WHEAT, harvestedWheat);
        statisticsCompound.setTag(TAG_GUARD_STATISTICS, guardStatisticsCompound);
        guardStatisticsCompound.setInteger(TAG_GUARD_MOBS, killedMobs);
        statisticsCompound.setTag(TAG_BUILDER_STATISTICS, builderStatisticsCompound);
        builderStatisticsCompound.setInteger(TAG_BUILDER_HUTS, builtHuts);
        statisticsCompound.setTag(TAG_FISHERMAN_STATISTICS, fishermanStatisticsCompound);
        fishermanStatisticsCompound.setInteger(TAG_FISHERMAN_FISH, caughtFish);
        statisticsCompound.setTag(TAG_LUMBERJACK_STATISTICS, lumberjackStatisticsCompound);
        lumberjackStatisticsCompound.setInteger(TAG_LUMBERJACK_TREES, felledTrees);
        lumberjackStatisticsCompound.setInteger(TAG_LUMBERJACK_SAPLINGS, plantedSaplings);
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
