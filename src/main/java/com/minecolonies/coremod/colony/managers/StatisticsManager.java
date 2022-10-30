package com.minecolonies.coremod.colony.managers;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.IStatisticsManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    private final Map<String, List<Long>> stats = new HashMap<>();

    /**
     * Create a new stat manager.
     * @param colony the colony to check.
     */
    public StatisticsManager(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public void increment(final String id)
    {
        stats.computeIfAbsent(id, k -> new ArrayList<>()).add(colony.getWorld().getGameTime());
    }

    @Override
    public int getStatTotal(final String id)
    {
        return stats.getOrDefault(id, new ArrayList<>()).size();
    }

    @Override
    public int getStatSince(final String id, final long time)
    {
        final List<Long> stats = this.stats.getOrDefault(id, new ArrayList<>());
        int count = 0;
        for (int i = stats.size() - 1; i >= 0; i--)
        {
            if (stats.get(i) >= time)
            {
                count++;
            }
            else
            {
                break;
            }
        }
        return count;
    }

    @Override
    public Map<String, List<Long>> getStats()
    {
        return stats;
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
                final List<Long> timeStamps = new ArrayList<>();
                for (int j = 0; j < timeStampNbts.size(); j++)
                {
                    timeStamps.add(timeStampNbts.getCompound(i).getLong(TAG_TIME));
                }

                stats.put(id, timeStamps);
            }
        }
    }

    @Override
    public void writeToNBT(@NotNull final CompoundTag compound)
    {
        final ListTag statManagerNBT = new ListTag();
        for (final Map.Entry<String, List<Long>> stats : stats.entrySet())
        {
            final CompoundTag statCompound = new CompoundTag();
            statCompound.putString(TAG_ID, stats.getKey());

            final ListTag statNBT = new ListTag();
            for (final long timeStamp : stats.getValue())
            {
                final CompoundTag timeStampTag = new CompoundTag();
                timeStampTag.putLong(TAG_TIME, timeStamp);
                statNBT.add(timeStampTag);
            }
            statCompound.put(TAG_STAT, statNBT);
            statManagerNBT.add(statCompound);
        }

        compound.put(TAG_STAT_MANAGER, statManagerNBT);
    }
}
