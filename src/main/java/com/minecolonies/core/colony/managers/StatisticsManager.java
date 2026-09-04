package com.minecolonies.core.colony.managers;

import com.minecolonies.api.colony.managers.interfaces.IStatisticsManager;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Manager for colony related statistics.
 */
public class StatisticsManager implements IStatisticsManager
{
    /**
     * NBT tags.
     */
    private static final String TAG_STAT_MANAGER = "stat_manager";
    private static final String TAG_STAT         = "stat";

    /**
     * The current stats of the colony.
     */
    private final Map<String, Int2IntLinkedOpenHashMap> stats = new HashMap<>();

    /**
     * The modified and not yet sent stats
     */
    private Set<String> dirtyStats = new HashSet<>();

    @Override
    public void increment(final @NotNull String id, final int day)
    {
        incrementBy(id, 1, day);
    }

    @Override
    public void incrementBy(final @NotNull String id, int qty, final int day)
    {
        final Int2IntLinkedOpenHashMap innerMap = stats.computeIfAbsent(id, k -> new Int2IntLinkedOpenHashMap());
        innerMap.addTo(day, qty);
        dirtyStats.add(id);
    }

    @Override
    public int getStatTotal(final @NotNull String id)
    {
        final Int2IntLinkedOpenHashMap stats = this.stats.getOrDefault(id, new Int2IntLinkedOpenHashMap());
        int totalCount = 0;
        for (final int count : stats.values())
        {
            totalCount += count;
        }
        return totalCount;
    }

    @Override
    public int getStatsInPeriod(final @NotNull String id, final int startDay, final int endDay)
    {
        final Int2IntLinkedOpenHashMap stats = this.stats.getOrDefault(id, new Int2IntLinkedOpenHashMap());
        int count = 0;
        for (int day = startDay; day <= endDay; day++)
        {
            count += stats.get(day);
        }
        return count;
    }

    @Override
    public @NotNull Set<String> getStatTypes()
    {
        return stats.keySet();
    }

    /**
     * Gets all the current stat entries in this manager.
     * @return a set of entries with the id and the stats map.
     */
    @Override
    public @NotNull Set<Map.Entry<String, Int2IntLinkedOpenHashMap>>  getStatEntries()
    {
        return stats.entrySet();
    }

    /**
     * Clear all the statistics, this will remove all the entries from the map
     */
    @Override
    public void clear()
    {
        dirtyStats.addAll(stats.keySet());
        stats.clear();
    }

    @Override
    public void serialize(@NotNull final RegistryFriendlyByteBuf buf, final boolean fullSync)
    {
        buf.writeBoolean(fullSync);
        buf.writeVarInt(fullSync ? stats.size() : dirtyStats.size());

        if (fullSync)
        {
            for (final Map.Entry<String, Int2IntLinkedOpenHashMap> dataEntry : stats.entrySet())
            {
                buf.writeUtf(dataEntry.getKey());
                buf.writeVarInt(dataEntry.getValue().size());

                for (final Int2IntMap.Entry valueEntry : dataEntry.getValue().int2IntEntrySet())
                {
                    buf.writeVarInt(valueEntry.getIntKey());
                    buf.writeVarInt(valueEntry.getIntValue());
                }
            }
        }
        else
        {
            for (final String id : dirtyStats)
            {
                final Int2IntLinkedOpenHashMap dataEntry = stats.get(id);

                buf.writeUtf(id);
                if (dataEntry == null)
                {
                    buf.writeVarInt(0);
                    continue;
                }

                buf.writeVarInt(1);
                buf.writeVarInt(dataEntry.lastIntKey());
                buf.writeVarInt(dataEntry.get(dataEntry.lastIntKey()));
            }
        }

        if (!dirtyStats.isEmpty())
        {
            dirtyStats = new HashSet<>();
        }
    }

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        final boolean fullSync = buf.readBoolean();
        if (fullSync)
        {
            stats.clear();
        }

        final int statSize = buf.readVarInt();
        for (int i = 0; i < statSize; i++)
        {
            final String id = buf.readUtf();
            final int statEntrySize = buf.readVarInt();

            if (!fullSync && statEntrySize == 0)
            {
                stats.remove(id);
                continue;
            }

            final Int2IntLinkedOpenHashMap statValues = (fullSync || !stats.containsKey(id)) ? new Int2IntLinkedOpenHashMap(statEntrySize) : stats.get(id);
            for (int j = 0; j < statEntrySize; j++)
            {
                statValues.put(buf.readVarInt(), buf.readVarInt());
            }

            stats.put(id, statValues);
        }
    }

    @Override
    public void writeToNBT(@NotNull final CompoundTag compound)
    {
        final ListTag statManagerNBT = new ListTag();
        for (final Map.Entry<String, Int2IntLinkedOpenHashMap> stat : stats.entrySet())
        {
            final CompoundTag statCompound = new CompoundTag();
            statCompound.putString(TAG_ID, stat.getKey());

            final ListTag statNBT = new ListTag();
            for (final Int2IntMap.Entry dailyStats : stat.getValue().int2IntEntrySet())
            {
                final CompoundTag timeStampTag = new CompoundTag();

                timeStampTag.putInt(TAG_TIME, dailyStats.getIntKey());
                timeStampTag.putInt(TAG_QUANTITY, dailyStats.getIntValue());

                statNBT.add(timeStampTag);
            }

            statCompound.put(TAG_STAT, statNBT);
            statManagerNBT.add(statCompound);
        }

        compound.put(TAG_STAT_MANAGER, statManagerNBT);
    }

    @Override
    public void readFromNBT(@NotNull final CompoundTag compound)
    {
        stats.clear();
        if (compound.contains(TAG_STAT_MANAGER))
        {
            final ListTag statsNbts = compound.getListOrEmpty(TAG_STAT_MANAGER);
            for (int i = 0; i < statsNbts.size(); i++)
            {
                final CompoundTag statCompound = statsNbts.getCompoundOrEmpty(i);
                final String id = statCompound.getStringOr(TAG_ID, "");
                final ListTag timeStampNbts = statCompound.getListOrEmpty(TAG_STAT);
                final Int2IntLinkedOpenHashMap timeStamps = new Int2IntLinkedOpenHashMap();
                for (int j = 0; j < timeStampNbts.size(); j++)
                {
                    final CompoundTag compoundTag = timeStampNbts.getCompoundOrEmpty(j);
                    final int day = compoundTag.getIntOr(TAG_TIME, 0);
                    final int qty = compoundTag.getIntOr(TAG_QUANTITY, 0);

                    timeStamps.put(day, qty);
                }

                stats.put(id, timeStamps);
            }
        }
    }
}
