package com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent;

import com.minecolonies.api.util.Log;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Utils for Colony pirate events
 */
public final class BarbarianEventUtils
{
    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianEventUtils()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Build a platform to sustain the barbarians.
     * @param target the target to build it.
     * @param world the world to build it in.
     */
    public static void buildPlatform(final BlockPos target, final World world)
    {
        Log.getLogger().warn("No ground found for raid, constructing slab platform");
        final IBlockState platformBlock = Blocks.WOODEN_SLAB.getDefaultState();

        for (int z = 0; z < 5; z++)
        {
            for (int x = 0; x < 5; x++)
            {
                final int sum = x * x + z * z;
                if (sum < (5 * 5) / 4)
                {
                    world.setBlockState(new BlockPos(target.getX() + x, target.getY()-1, target.getZ() + z), platformBlock);
                    world.setBlockState(new BlockPos(target.getX() + x, target.getY()-1, target.getZ() -z), platformBlock);
                    world.setBlockState(new BlockPos(target.getX() -x, target.getY()-1, target.getZ() + z), platformBlock);
                    world.setBlockState(new BlockPos(target.getX() -x, target.getY()-1, target.getZ() -z), platformBlock);
                }
            }
        }
    }
}
