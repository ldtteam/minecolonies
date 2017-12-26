package com.minecolonies.coremod.colony.managers;

import com.minecolonies.coremod.colony.Colony;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IStatisticAchievementManager
{
    /**
     * Reads all stats from nbt.
     * @param compound the compound.
     * @param colony the colony.
     */
    void readFromNBT(@NotNull final NBTTagCompound compound, @NotNull final Colony colony);

    /**
     * Write all stats to nbt.
     * @param statsCompound the compound.
     */
    void writeToNBT(@NotNull final NBTTagCompound statsCompound);

    /**
     * Check all achievements.
     * @param colony the colony.
     */
    void checkAchievements(final Colony colony);

    /**
     * Trigger a certain achievement.
     * @param achievement the achievement.
     * @param colony the colony.
     */
    void triggerAchievement(@NotNull final Achievement achievement, @NotNull final Colony colony);

    /**
     * Increment a statistic.
     * @param stat the statistic.
     * @param colony the colony.
     */
    void incrementStatistic(@NotNull final String stat, final Colony colony);

    /**
     * Get a list of all achievements.
     * @return a copy.
     */
    List<Achievement> getAchievements();
}
