package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.SchematicTagConstants.TAG_SITTING;

/**
 * Creates a new building for the school.
 */
public class BuildingSchool extends AbstractBuilding
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String SCHOOL = "school";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * NBT value to store the carpet pos.
     */
    private static final String NBT_CARPET = "carpet";

    /**
     * List of carpets to sit on.
     */
    @NotNull
    private final List<BlockPos> carpet = new ArrayList<>();

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingSchool(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SCHOOL;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void registerBlockPosition(@NotNull final BlockState blockState, @NotNull final BlockPos pos, @NotNull final Level world)
    {
        super.registerBlockPosition(blockState, pos, world);
        if (isCarpet(blockState))
        {
            carpet.add(pos);
        }
    }

    @Override
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        final ListTag carpetTagList = compound.getList(NBT_CARPET, Tag.TAG_COMPOUND);
        for (int i = 0; i < carpetTagList.size(); ++i)
        {
            final CompoundTag bedCompound = carpetTagList.getCompound(i);
            final BlockPos pos = BlockPosUtil.read(bedCompound, TAG_POS);
            if (!carpet.contains(pos))
            {
                carpet.add(pos);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();
        if (!carpet.isEmpty())
        {
            @NotNull final ListTag carpetTagList = new ListTag();
            for (@NotNull final BlockPos pos : carpet)
            {
                final CompoundTag carpetCompound = new CompoundTag();
                BlockPosUtil.write(carpetCompound, NbtTagConstants.TAG_POS, pos);
                carpetTagList.add(carpetCompound);
            }
            compound.put(NBT_CARPET, carpetTagList);
        }

        return compound;
    }

    /**
     * Get a random place to sit from the school.
     *
     * @return the place to sit.
     */
    @Nullable
    public BlockPos getRandomPlaceToSit()
    {
        final List<BlockPos> validSitLocations = getLocationsFromTag(TAG_SITTING);
        if (!validSitLocations.isEmpty())
        {
            return validSitLocations.get(ColonyConstants.rand.nextInt(validSitLocations.size()));
        }

        if (carpet.isEmpty())
        {
            return null;
        }
        final BlockPos returnPos = carpet.get(ColonyConstants.rand.nextInt(carpet.size()));
        if (isCarpet(colony.getWorld().getBlockState(returnPos)))
        {
            return returnPos;
        }
        carpet.remove(returnPos);
        return null;
    }

    /**
     * Check whether a given block is a valid carpet block.
     * @param blockState the input block.
     * @return true if so.
     */
    private boolean isCarpet(final BlockState blockState)
    {
        return blockState.getBlock() instanceof WoolCarpetBlock;
    }
}
