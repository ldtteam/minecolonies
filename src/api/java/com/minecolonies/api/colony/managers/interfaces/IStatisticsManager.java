package com.minecolonies.api.colony.managers.interfaces;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Interface for the statistics manager
 */
public interface IStatisticsManager
{
    //todo read/write to bytebuf

    /**
     * Increment a given statistic.
     * Creates a new if it doesn't exist yet.
     * Assigns a timestamp to the entry.
     * @param id the id of the stat.
     */
    void increment(String id);

    /**
     * Get the total for a given stat,
     * @param id the id of the stat.
     * @return the total since colony creation.
     */
    int getStatTotal(String id);

    /**
     * Get the number of occurrences since a given timestamp.
     * @param id the id of the stat.
     * @param time the timestamp after which to start counting.
     * @return the count.
     */
    int getStatSince(String id, long time);

    /**
     * Serialize to bytebuf.
     * @param buf the buffer to write to.
     */
    void serialize(@NotNull FriendlyByteBuf buf);

    /**
     * Deserialize from bytebuf.
     * @param buf the buffer to read from.
     */
    void deserialize(@NotNull FriendlyByteBuf buf);

    /**
     * Reads the eventManager nbt and creates events from it
     *
     * @param compound the compound to read from.
     */
    void readFromNBT(@NotNull CompoundTag compound);

    /**
     * Write the eventmanager and all events to NBT
     *
     * @param compound the compound to write to.
     */
    void writeToNBT(@NotNull CompoundTag compound);

    /**
     * Getter for the whole stat list.
     * @return the map of stats.
     */
    Map<String, List<Long>> getStats();
}
