package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.IStatisticsManager;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
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
     * Colony reference
     */
    private final IColony colony;

    /**
     * The current stats of the colony.
     */
    private final Map<String, Int2IntLinkedOpenHashMap> stats = new HashMap<>();

    /**
     * Create a new stat manager.
     * @param colony the colony to check.
     */
    public StatisticsManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public void increment(final @NotNull String id)
    {
        incrementBy(id, 1);
    }

    @Override
    public void incrementBy(final @NotNull String id, int qty)
    {
        final Int2IntLinkedOpenHashMap innerMap = stats.computeIfAbsent(id, k -> new Int2IntLinkedOpenHashMap());
        innerMap.addTo(colony.getDay(), qty);
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
        final CompoundTag statsCompound = new CompoundTag();
        writeToNBT(statsCompound);
        buf.writeNbt(statsCompound);
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        readFromNBT(buf.readNbt());
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
