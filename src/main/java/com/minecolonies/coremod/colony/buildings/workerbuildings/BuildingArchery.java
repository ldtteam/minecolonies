package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Building class for the Archery.
 */
public class BuildingArchery extends AbstractBuilding
{
    /**
     * The Schematic name.
     */
    private static final String SCHEMATIC_NAME = "archery";

    /**
     * List of shooting stands in the building.
     */
    private final List<BlockPos> shootingStands = new ArrayList<>();

    /**
     * List of shooting targets in the building.
     */
    private final List<BlockPos> shootingTargets = new ArrayList<>();

    /**
     * The abstract constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    public BuildingArchery(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }


    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        if (block == Blocks.TARGET)
        {
            shootingTargets.add(pos);
        }
        else if (block == Blocks.GLOWSTONE)
        {
            shootingStands.add(pos);
        }
        super.registerBlockPosition(block, pos, world);
    }

    @Override
    public void deserializeNBT(final CompoundNBT compound)
    {
        super.deserializeNBT(compound);
        shootingTargets.clear();
        shootingStands.clear();

        final ListNBT targetList = compound.getList(TAG_ARCHERY_TARGETS, Constants.NBT.TAG_COMPOUND);
        shootingTargets.addAll(NBTUtils.streamCompound(targetList).map(targetCompound -> BlockPosUtil.read(targetCompound, TAG_TARGET)).collect(Collectors.toList()));

        final ListNBT standTagList = compound.getList(TAG_ARCHERY_STANDS, Constants.NBT.TAG_COMPOUND);
        shootingStands.addAll(NBTUtils.streamCompound(standTagList).map(targetCompound -> BlockPosUtil.read(targetCompound, TAG_STAND)).collect(Collectors.toList()));
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compound = super.serializeNBT();

        final ListNBT targetList = shootingTargets.stream().map(target -> BlockPosUtil.write(new CompoundNBT(), TAG_TARGET, target)).collect(NBTUtils.toListNBT());
        compound.put(TAG_ARCHERY_TARGETS, targetList);

        final ListNBT standTagList = shootingStands.stream().map(target -> BlockPosUtil.write(new CompoundNBT(), TAG_STAND, target)).collect(NBTUtils.toListNBT());
        compound.put(TAG_ARCHERY_STANDS, standTagList);

        return compound;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return SCHEMATIC_NAME;
    }

    /**
     * Get a random position to shoot from.
     *
     * @param random the random obj.
     * @return a random shooting stand position.
     */
    public BlockPos getRandomShootingStandPosition(final Random random)
    {
        final List<BlockPos> tagged = getLocationsFromTag(TAG_WORK);
        if (!tagged.isEmpty())
        {
            return tagged.get(random.nextInt(tagged.size()));
        }
        if (!shootingStands.isEmpty())
        {
            return shootingStands.get(random.nextInt(shootingStands.size()));
        }
        return null;
    }

    /**
     * Get a random position to shoot at.
     *
     * @param random the random obj.
     * @return a random shooting target position.
     */
    public BlockPos getRandomShootingTarget(final Random random)
    {
        if (!shootingTargets.isEmpty())
        {
            return shootingTargets.get(random.nextInt(shootingTargets.size()));
        }
        return null;
    }
}
