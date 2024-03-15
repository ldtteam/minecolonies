package com.minecolonies.core.entity.pathfinding.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

/**
 * Small block lookup cache, to avoid repeated lookups
 */
public class CachingBlockLookup implements BlockGetter
{
    private final static int SIZE         = 5;
    private final static int MIDDLEOFFSET = SIZE / 2;

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
     * States array, exchange is just used for switching over blockstates from previous positions
     */
    private BlockState[] states   = new BlockState[SIZE * SIZE * SIZE];
    private BlockState[] exchange = new BlockState[SIZE * SIZE * SIZE];

    private ChunkAccess chunk = null;

    public CachingBlockLookup(final BlockPos center, final LevelReader world)
    {
        centerX = center.getX() + MIDDLEOFFSET;
        centerY = center.getY() + MIDDLEOFFSET;
        centerZ = center.getZ() + MIDDLEOFFSET;
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
        return getBlockState(pos.getX(), pos.getY(), pos.getZ());
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

        if (xPos < 0 || xPos >= SIZE || yPos < 0 || yPos >= SIZE || zPos < 0 || zPos >= SIZE)
        {
            return world.getBlockState(temp.set(x, y, z));
        }
        else
        {
            final int index = xPos + yPos * SIZE + zPos * SIZE * SIZE;
            BlockState state = states[index];
            if (state == null)
            {
                if (chunk == null || chunk.getPos().x != x >> 4 || chunk.getPos().z != z >> 4)
                {
                    chunk = world.getChunk(x >> 4, z >> 4);
                }

                if (chunk != null)
                {
                    state = chunk.getBlockState(temp.set(x, y, z));
                }
                else
                {
                    state = world.getBlockState(temp.set(x, y, z));
                }

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
        final int xDiff = (x + MIDDLEOFFSET) - centerX;
        final int yDiff = (y + MIDDLEOFFSET) - centerY;
        final int zDiff = (z + MIDDLEOFFSET) - centerZ;

        if (Math.abs(xDiff) >= SIZE || Math.abs(yDiff) >= SIZE || Math.abs(zDiff) >= SIZE)
        {
            for (int i = 0; i < states.length; i++)
            {
                states[i] = null;
            }
        }
        else
        {
            for (int i = 0; i < states.length; i++)
            {
                final BlockState state = states[i];
                if (state != null)
                {
                    final int newIndex = i + (xDiff + (yDiff * SIZE) + (zDiff * SIZE * SIZE));
                    boolean xposDiff = (newIndex % SIZE - i % SIZE) == xDiff;
                    boolean yposDiff = (newIndex % SIZE * SIZE - i % SIZE * SIZE) == yDiff + xDiff;

                    if (newIndex < states.length && newIndex >= 0 && xposDiff && yposDiff)
                    {
                        exchange[newIndex] = state;
                    }

                    states[i] = null;
                }
            }

            final BlockState[] temp = states;
            states = exchange;
            exchange = temp;
        }

        centerX = x + MIDDLEOFFSET;
        centerY = y + MIDDLEOFFSET;
        centerZ = z + MIDDLEOFFSET;
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