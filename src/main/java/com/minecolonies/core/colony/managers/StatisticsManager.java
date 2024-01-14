package com.minecolonies.core.colony.managers;

import com.minecolonies.api.colony.managers.interfaces.IStatisticsManager;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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
    private static final String TAG_STAT_MANAGER    = "stat_manager";
    private static final String TAG_STAT            = "stat";

    /**
     * The current stats of the colony.
     */
    private final Map<String, Int2IntLinkedOpenHashMap> stats = new HashMap<>();

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

    @Override
    public void serialize(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeVarInt(stats.size());
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

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        stats.clear();
        final int statSize = buf.readVarInt();
        for (int i = 0; i < statSize; i++)
        {
            final String id = buf.readUtf();
            final int statEntrySize = buf.readVarInt();

            final Int2IntLinkedOpenHashMap statValues = new Int2IntLinkedOpenHashMap(statEntrySize);
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
            for (final Map.Entry<Integer, Integer> dailyStats : stat.getValue().entrySet())
            {
                final CompoundTag timeStampTag = new CompoundTag();

                timeStampTag.putInt(TAG_TIME, dailyStats.getKey());
                timeStampTag.putInt(TAG_QUANTITY, dailyStats.getValue());

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
            final ListTag statsNbts = compound.getList(TAG_STAT_MANAGER, Tag.TAG_COMPOUND);
            for (int i = 0; i < statsNbts.size(); i++)
            {
                final CompoundTag statCompound = statsNbts.getCompound(i);
                final String id = statCompound.getString(TAG_ID);
                final ListTag timeStampNbts = statCompound.getList(TAG_STAT, Tag.TAG_COMPOUND);
                final Int2IntLinkedOpenHashMap timeStamps = new Int2IntLinkedOpenHashMap();
                for (int j = 0; j < timeStampNbts.size(); j++)
                {
                    final CompoundTag compoundTag = timeStampNbts.getCompound(j);
                    final int day = compoundTag.getInt(TAG_TIME);
                    final int qty = compoundTag.getInt(TAG_QUANTITY);

                    timeStamps.put(day, qty);
                }

                stats.put(id, timeStamps);
            }
        }
    }
}
