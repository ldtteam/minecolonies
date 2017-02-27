package com.minecolonies.compatibility.tinkers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

/**
 * This is the fallback for when tinkers is not present!
 */
public class SlimeTreeProxy
{
    /**
     * This is the fallback for when tinkers is not present!
     *
     * @param world the world.
     * @param pos the position
     * @return if the tree is Slime Tree.
     */
    protected boolean checkForTinkersSlimeBlock(@NotNull final IBlockAccess world, @NotNull final BlockPos pos)
    {
        return false;

    }
}
