package com.minecolonies.compatibility.tinkers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

public class SlimeTreeProxy
{

    private static final double SLIME_BLOCK_SLIPPERINESS = 0.5D;

    /**
     * Check if tree is Slime Tree.
     *
     * @param world the world.
     * @param pos the position
     * @return if the tree is Slime Tree.
     */
    protected boolean checkForTinkersSlimeBlock(@NotNull final IBlockAccess world, @NotNull final BlockPos pos)
    {
        /*
        This is the fallback for when tinkers is not present!
         */
        return false;

    }
}
