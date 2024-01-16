package com.minecolonies.core.entity.pathfinding;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small block lookup cache, to avoid repeated lookups
 */
public class CachingBlockLookup
{
    /**
     * Center of the cache
     */
    private       int                      centerX;
    private       int                      centerY;
    private       int                      centerZ;

    /**
     * Original world lookup
     */
    private final LevelReader              world;

    /**
     * Temp world access
     */
    private final BlockPos.MutableBlockPos temp   = new BlockPos.MutableBlockPos();

    /**
     * States array
     */
    private final BlockState[][][]         states = new BlockState[5][5][5];

    public CachingBlockLookup(final BlockPos center, final LevelReader world)
    {
        centerX = center.getX() + 2;
        centerY = center.getY() + 2;
        centerZ = center.getZ() + 2;
        this.world = world;
    }

    /**
     * Get a blockstate
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
            BlockState state = states[xPos][yPos][zPos];
            if (state == null)
            {
                state = world.getBlockState(pos);
                states[xPos][yPos][zPos] = state;
            }
            return state;
        }
    }

    /**
     * Get a blockstate
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
            BlockState state = states[xPos][yPos][zPos];
            if (state == null)
            {
                state = world.getBlockState(temp.set(x, y, z));
                states[xPos][yPos][zPos] = state;
            }

            return state;
        }
    }

    /**
     * Resets the cache's position and data
     * @param next
     */
    public void resetToNextPos(final BlockPos next)
    {
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 5; j++)
            {
                for (int k = 0; k < 5; k++)
                {
                    states[i][j][k] = null;
                }
            }
        }

        centerX = next.getX() + 2;
        centerY = next.getY() + 2;
        centerZ = next.getZ() + 2;
    }
}
