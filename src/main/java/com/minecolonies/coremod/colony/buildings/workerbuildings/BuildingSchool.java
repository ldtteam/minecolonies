package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.constant.NbtTagConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.CarpetBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

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
    private static final String TAG_CARPET = "carpet";

    /**
     * List of carpets to sit on.
     */
    @NotNull
    private final List<BlockPos> carpet = new ArrayList<>();

    /**
     * Random obj for random calc.
     */
    private final Random random = new Random();

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
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block instanceof CarpetBlock)
        {
            carpet.add(pos);
        }
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        final ListNBT carpetTagList = compound.getList(TAG_CARPET, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < carpetTagList.size(); ++i)
        {
            final CompoundNBT bedCompound = carpetTagList.getCompound(i);
            final BlockPos pos = BlockPosUtil.read(bedCompound, TAG_POS);
            if (!carpet.contains(pos))
            {
                carpet.add(pos);
            }
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();
        if (!carpet.isEmpty())
        {
            @NotNull final ListNBT carpetTagList = new ListNBT();
            for (@NotNull final BlockPos pos : carpet)
            {
                final CompoundNBT carpetCompound = new CompoundNBT();
                BlockPosUtil.write(carpetCompound, NbtTagConstants.TAG_POS, pos);
                carpetTagList.add(carpetCompound);
            }
            compound.put(TAG_CARPET, carpetTagList);
        }

        return compound;
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);
    }

    /**
     * Get a random place to sit from the school.
     *
     * @return the place to sit.
     */
    @Nullable
    public BlockPos getRandomPlaceToSit()
    {
        if (carpet.isEmpty())
        {
            return null;
        }
        final BlockPos returnPos = carpet.get(random.nextInt(carpet.size()));
        if (colony.getWorld().getBlockState(returnPos).getBlock() instanceof CarpetBlock)
        {
            return returnPos;
        }
        carpet.remove(returnPos);
        return null;
    }
}
