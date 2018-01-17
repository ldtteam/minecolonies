package com.minecolonies.coremod.colony.managers;

import com.minecolonies.coremod.achievements.MineColoniesAchievement;
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
     */
    void readFromNBT(@NotNull final NBTTagCompound compound);

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
     */
    void triggerAchievement(@NotNull final Achievement achievement);

    /**
     * Increment a statistic.
     * @param stat the statistic.
     */
    void incrementStatistic(@NotNull final String stat);

    /**
     * Get a list of all achievements.
     * @return a copy.
     */
    List<Achievement> getAchievements();
}
