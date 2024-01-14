package com.minecolonies.core.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final Level world)
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
    public void deserializeNBT(final CompoundTag compound)
    {
        super.deserializeNBT(compound);
        shootingTargets.clear();
        shootingStands.clear();

        final ListTag targetList = compound.getList(TAG_ARCHERY_TARGETS, Tag.TAG_COMPOUND);
        shootingTargets.addAll(NBTUtils.streamCompound(targetList).map(targetCompound -> BlockPosUtil.read(targetCompound, TAG_TARGET)).collect(Collectors.toList()));

        final ListTag standTagList = compound.getList(TAG_ARCHERY_STANDS, Tag.TAG_COMPOUND);
        shootingStands.addAll(NBTUtils.streamCompound(standTagList).map(targetCompound -> BlockPosUtil.read(targetCompound, TAG_STAND)).collect(Collectors.toList()));
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = super.serializeNBT();

        final ListTag targetList = shootingTargets.stream().map(target -> BlockPosUtil.write(new CompoundTag(), TAG_TARGET, target)).collect(NBTUtils.toListNBT());
        compound.put(TAG_ARCHERY_TARGETS, targetList);

        final ListTag standTagList = shootingStands.stream().map(target -> BlockPosUtil.write(new CompoundTag(), TAG_STAND, target)).collect(NBTUtils.toListNBT());
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
    public BlockPos getRandomShootingStandPosition(final RandomSource random)
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
    public BlockPos getRandomShootingTarget(final RandomSource random)
    {
        if (!shootingTargets.isEmpty())
        {
            return shootingTargets.get(random.nextInt(shootingTargets.size()));
        }
        return null;
    }
}
