package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * The enchanters station selection module.
 */
public class EnchanterStationsModule extends AbstractBuildingModule implements IBuildingModule, IPersistentModule, IBuildingEventsModule
{
    /**
     * List of buildings the enchanter gathers experience from.
     */
    private Map<BlockPos, Boolean> buildingToGatherFrom = new HashMap<>();

    /**
     * The random variable.
     */
    private Random random = new Random();

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        buildingToGatherFrom.clear();
        NBTUtils.streamCompound(compound.getList(TAG_GATHER_LIST, Tag.TAG_COMPOUND))
          .map(this::deserializeListElement)
          .forEach(t -> buildingToGatherFrom.put(t.getA(), t.getB()));
    }

    @Override
    public void serializeNBT(final CompoundTag compound)
    {
        compound.put(TAG_GATHER_LIST, buildingToGatherFrom.entrySet().stream().map(this::serializeListElement).collect(NBTUtils.toListNBT()));
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeInt(buildingToGatherFrom.size());
        for (final BlockPos pos : buildingToGatherFrom.keySet())
        {
            buf.writeBlockPos(pos);
        }
    }

    /**
     * Helper to deserialize a list element from nbt.
     *
     * @param nbtTagCompound the compound to deserialize from.
     * @return the resulting blockPos/boolean tuple.
     */
    private Tuple<BlockPos, Boolean> deserializeListElement(final CompoundTag nbtTagCompound)
    {
        final BlockPos pos = BlockPosUtil.read(nbtTagCompound, TAG_POS);
        final boolean gatheredAlready = nbtTagCompound.getBoolean(TAG_GATHERED_ALREADY);
        return new Tuple<>(pos, gatheredAlready);
    }

    /**
     * Serialize the element.
     * @param entry the entry to serialize.
     * @return the resulting compound.
     */
    private CompoundTag serializeListElement(final Map.Entry<BlockPos, Boolean> entry)
    {
        final CompoundTag compound = new CompoundTag();
        BlockPosUtil.write(compound, TAG_POS, entry.getKey());
        compound.putBoolean(TAG_GATHERED_ALREADY, entry.getValue());
        return compound;
    }

    /**
     * Return the set of the buildings to gather from.
     *
     * @return a copy of th eset.
     */
    public Set<BlockPos> getBuildingsToGatherFrom()
    {
        return new HashSet<>(buildingToGatherFrom.keySet());
    }

    /**
     * Get a random worker building id to gather xp from.
     *
     * @return the unique pos id of it.
     */
    @Nullable
    public BlockPos getRandomBuildingToDrainFrom()
    {
        final List<BlockPos> buildings = buildingToGatherFrom.entrySet().stream().filter(k -> !k.getValue()).map(Map.Entry::getKey).collect(Collectors.toList());
        if (buildings.isEmpty())
        {
            return null;
        }
        return buildings.get(random.nextInt(buildings.size()));
    }

    /**
     * Set the building as gathered.
     *
     * @param pos the pos of the building.
     */
    public void setAsGathered(final BlockPos pos)
    {
        buildingToGatherFrom.put(pos, true);
    }

    /**
     * Add a new worker to gather xp from.
     *
     * @param blockPos the pos of the building.
     */
    public void addWorker(final BlockPos blockPos)
    {
        buildingToGatherFrom.put(blockPos, false);
        markDirty();
    }

    /**
     * Remove a worker to stop gathering from.
     *
     * @param blockPos the pos of that worker.
     */
    public void removeWorker(final BlockPos blockPos)
    {
        buildingToGatherFrom.remove(blockPos);
        markDirty();
    }

    @Override
    public void onWakeUp()
    {
        final Set<BlockPos> keys = new HashSet<>(buildingToGatherFrom.keySet());
        buildingToGatherFrom.clear();
        keys.forEach(k -> buildingToGatherFrom.put(k, false));
    }
}
