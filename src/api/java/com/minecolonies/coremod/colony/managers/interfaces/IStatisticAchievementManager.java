package com.minecolonies.coremod.colony.managers.interfaces;

import com.minecolonies.coremod.achievements.IMineColoniesAchievement;
import net.minecraft.advancements.Advancement;
import net.minecraft.nbt.NBTTagCompound;
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
     */
    void checkAchievements();

    /**
     * Trigger a certain achievement.
     * @param achievement the achievement.
     */
    void triggerAchievement(@NotNull final IMineColoniesAchievement achievement);

    /**
     * Increment a statistic.
     * @param stat the statistic.
     */
    void incrementStatistic(@NotNull final String stat);

    /**
     * Get a list of all achievements.
     * @return a copy.
     */
    List<Advancement> getAchievements();
}
