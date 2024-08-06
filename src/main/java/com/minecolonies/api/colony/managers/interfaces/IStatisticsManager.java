package com.minecolonies.api.colony.managers.interfaces;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Interface for the statistics manager
 */
public interface IStatisticsManager
{
    /**
     * Increment a given statistic.
     * Creates a new if it doesn't exist yet.
     * Assigns a timestamp to the entry.
     * @param id the id of the stat.
     */
    void increment(@NotNull final String id, final int day);

    /**
     * Increment a given statistic by some quantity.
     * Creates a new if it doesn't exist yet.
     * Assigns a timestamp to the entry.
     * @param id the id of the stat.
     * @param qty the quantity.
     */
    void incrementBy(@NotNull String id, final int qty, final int day);

    /**
     * Get the total for a given stat,
     * @param id the id of the stat.
     * @return the total since colony creation.
     */
    int getStatTotal(@NotNull String id);

    /**
     * Get the number of occurrences in a given period.
     * @param id the id of the stat.
     * @param dayStart the start day.
     * @param dayEnd the end day.
     * @return the count.
     */
    int getStatsInPeriod(@NotNull String id, final int dayStart, final int dayEnd);

    /**
     * Serialize to bytebuf.
     *
     * @param buf               the buffer to write to.
     * @param hasNewSubscribers
     */
    void serialize(@NotNull final RegistryFriendlyByteBuf buf, final boolean hasNewSubscribers);

    /**
     * Deserialize from bytebuf.
     * @param buf the buffer to read from.
     */
    void deserialize(@NotNull final RegistryFriendlyByteBuf buf);

    /**
     * Reads the eventManager nbt and creates events from it
     *
     * @param compound the compound to read from.
     */
    void readFromNBT(@NotNull final CompoundTag compound);

    /**
     * Write the eventmanager and all events to NBT
     *
     * @param compound the compound to write to.
     */
    void writeToNBT(@NotNull final CompoundTag compound);

    /**
     * Getter for the whole stat list.
     * @return the map of stats.
     */
    @NotNull
    Set<String> getStatTypes();
}
