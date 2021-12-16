package com.minecolonies.coremod.colony;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.IGraveManager;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Client-side read-only copy of the {@link com.minecolonies.coremod.colony.managers.GraveManager}.
 */
public class GraveManagerView implements IGraveManager
{
    private Map<BlockPos, Boolean> graves = ImmutableMap.of();

    /**
     * This needs to read what {@link com.minecolonies.coremod.colony.managers.GraveManager#write} wrote.
     *
     * @param compound the compound.
     */
    @Override
    public void read(@NotNull CompoundNBT compound)
    {
        final ImmutableMap.Builder<BlockPos, Boolean> graves = ImmutableMap.builder();

        final ListNBT gravesTagList = compound.getList(TAG_GRAVE, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < gravesTagList.size(); ++i)
        {
            final CompoundNBT graveCompound = gravesTagList.getCompound(i);
            if (graveCompound.contains(TAG_POS) && graveCompound.contains(TAG_RESERVED))
            {
                graves.put(BlockPosUtil.read(graveCompound, TAG_POS), graveCompound.getBoolean(TAG_RESERVED));
            }
        }

        this.graves = graves.build();
    }

    @Override
    public void write(@NotNull CompoundNBT compound)
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
    public void createCitizenGrave(World world, BlockPos pos, ICitizenData citizenData)
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
