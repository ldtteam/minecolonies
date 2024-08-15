package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.modules.*;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_BEDS;

/**
 * The class of the citizen hut.
 */
public class BedHandlingModule extends AbstractBuildingModule implements IModuleWithExternalBlocks, IPersistentModule, IBuildingEventsModule
{
    /**
     * List of all beds.
     */
    @NotNull
    private final Set<BlockPos> bedList = new HashSet<>();

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag compound)
    {
        final ListTag bedTagList = compound.getList(TAG_BEDS, Tag.TAG_INT_ARRAY);
        for (int i = 0; i < bedTagList.size(); ++i)
        {
            final Tag bedCompound = bedTagList.getCompound(i);
            final BlockPos bedPos = NBTUtils.readBlockPos(bedCompound);
            bedList.add(bedPos);
        }
    }

    @Override
    public void serializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag compound)
    {
        if (!bedList.isEmpty())
        {
            @NotNull final ListTag bedTagList = new ListTag();
            for (@NotNull final BlockPos pos : bedList)
            {
                bedTagList.add(NBTUtils.writeBlockPos(pos));
            }
            compound.put(TAG_BEDS, bedTagList);
        }
    }

    @Override
    public void onBlockPlacedInBuilding(@NotNull final BlockState blockState, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        BlockPos registrationPosition = pos;
        if (blockState.getBlock() instanceof BedBlock)
        {
            if (blockState.getValue(BedBlock.PART) == BedPart.FOOT)
            {
                registrationPosition = registrationPosition.relative(blockState.getValue(BedBlock.FACING));
            }

            bedList.add(registrationPosition);
        }
    }

    @Override
    public List<BlockPos> getRegisteredBlocks()
    {
        return new ArrayList<>(bedList);
    }

    @Override
    public void onWakeUp()
    {
        final Level world = building.getColony().getWorld();
        if (world == null)
        {
            return;
        }

        for (final BlockPos pos : bedList)
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof BedBlock
                      && state.getValue(BedBlock.OCCUPIED)
                      && state.getValue(BedBlock.PART).equals(BedPart.HEAD))
                {
                    world.setBlock(pos, state.setValue(BedBlock.OCCUPIED, false), 0x03);
                }
            }
        }
    }

}
