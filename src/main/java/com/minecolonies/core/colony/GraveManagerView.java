package com.minecolonies.core.colony;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.IGraveManager;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Client-side read-only copy of the {@link com.minecolonies.core.colony.managers.GraveManager}.
 */
public class GraveManagerView implements IGraveManager
{
    private Map<BlockPos, Boolean> graves = ImmutableMap.of();

    /**
     * This needs to read what {@link com.minecolonies.core.colony.managers.GraveManager#write} wrote.
     *
     * @param compound the compound.
     */
    @Override
    public void read(@NotNull CompoundTag compound)
    {
        final ImmutableMap.Builder<BlockPos, Boolean> graves = ImmutableMap.builder();

        final ListTag gravesTagList = compound.getList(TAG_GRAVE, Tag.TAG_COMPOUND);
        for (int i = 0; i < gravesTagList.size(); ++i)
        {
            final CompoundTag graveCompound = gravesTagList.getCompound(i);
            if (graveCompound.contains(TAG_POS) && graveCompound.contains(TAG_RESERVED))
            {
                graves.put(BlockPosUtil.read(graveCompound, TAG_POS), graveCompound.getBoolean(TAG_RESERVED));
            }
        }

        this.graves = graves.build();
    }

    @Override
    public void write(@NotNull CompoundTag compound)
    {
    }

    @Override
    public void onColonyTick(IColony colony)
    {
    }

    @Override
    public boolean reserveGrave(BlockPos pos)
    {
        return false;
    }

    @Override
    public void unReserveGrave(BlockPos pos)
    {
    }

    @Override
    public BlockPos reserveNextFreeGrave()
    {
        return null;
    }

    @Override
    public void createCitizenGrave(Level world, BlockPos pos, ICitizenData citizenData)
    {
    }

    @NotNull
    @Override
    public Map<BlockPos, Boolean> getGraves()
    {
        return this.graves;
    }

    @Override
    public boolean addNewGrave(@NotNull BlockPos pos)
    {
        return false;
    }

    @Override
    public void removeGrave(@NotNull BlockPos pos)
    {
    }
}
