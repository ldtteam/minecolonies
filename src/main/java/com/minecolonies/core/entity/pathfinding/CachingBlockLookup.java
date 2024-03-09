package com.minecolonies.core.entity.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

/**
 * Small block lookup cache, to avoid repeated lookups
 */
public class CachingBlockLookup implements BlockGetter
{
    /**
     * Center of the cache
     */
    private int centerX;
    private int centerY;
    private int centerZ;

    /**
     * Original world lookup
     */
    private final LevelReader world;

    /**
     * Temp world access
     */
    private final BlockPos.MutableBlockPos temp = new BlockPos.MutableBlockPos();

    /**
     * States array
     */
    private final BlockState[] states = new BlockState[5 * 5 * 5];

    public CachingBlockLookup(final BlockPos center, final LevelReader world)
    {
        centerX = center.getX() + 2;
        centerY = center.getY() + 2;
        centerZ = center.getZ() + 2;
        this.world = world;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(final BlockPos p_45570_)
    {
        return null;
    }

    /**
     * Get a blockstate
     *
     * @param pos
     * @return
     */
    public BlockState getBlockState(final BlockPos pos)
    {
        final int xPos = centerX - pos.getX();
        final int yPos = centerY - pos.getY();
        final int zPos = centerZ - pos.getZ();

        if (xPos < 0 || xPos > 4 || yPos < 0 || yPos > 4 || zPos < 0 || zPos > 4)
        {
            return world.getBlockState(pos);
        }
        else
        {
            final int index = xPos + yPos * 5 + zPos * 5 * 5;
            BlockState state = states[index];
            if (state == null)
            {
                state = world.getBlockState(pos);
                states[index] = state;
            }

            return state;
        }
    }

    @Override
    public FluidState getFluidState(final BlockPos pos)
    {
        return getBlockState(pos).getFluidState();
    }

    /**
     * Get a blockstate
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public BlockState getBlockState(final int x, final int y, final int z)
    {
        final int xPos = centerX - x;
        final int yPos = centerY - y;
        final int zPos = centerZ - z;

        if (xPos < 0 || xPos > 4 || yPos < 0 || yPos > 4 || zPos < 0 || zPos > 4)
        {
            return world.getBlockState(temp.set(x, y, z));
        }
        else
        {
            final int index = xPos + yPos * 5 + zPos * 5 * 5;
            BlockState state = states[index];
            if (state == null)
            {
                state = world.getBlockState(temp.set(x, y, z));
                states[index] = state;
            }

            return state;
        }
    }

    /**
     * Resets the cache's position and data
     */
    public void resetToNextPos(final int x, final int y, final int z)
    {
        for (int i = 0; i < states.length; i++)
        {
            states[i] = null;
        }

        centerX = x + 2;
        centerY = y + 2;
        centerZ = z + 2;
    }

    @Override
    public int getHeight()
    {
        return world.getHeight();
    }

    @Override
    public int getMinBuildHeight()
    {
        return world.getMinBuildHeight();
    }
}