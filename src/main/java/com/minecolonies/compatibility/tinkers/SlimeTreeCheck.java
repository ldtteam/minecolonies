package com.minecolonies.compatibility.tinkers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import slimeknights.tconstruct.shared.TinkerCommons;

/**
 * This class is to store a check to see if a tree is a slime tree.
 */
public final class SlimeTreeCheck extends SlimeTreeProxy
{

    /**
     * Check if tree is Slime Tree.
     * @param world the world.
     * @param pos the position
     * @return if the tree is Slime Tree.
     */
    public static boolean checkTinkersTree(@NotNull final IBlockAccess world, @NotNull final BlockPos pos)
    {
        return new SlimeTreeCheck().checkForTinkersSlimeBlock(world, pos);
    }
    /**
     * Check if tree is Slime Tree.
     * @param world the world.
     * @param pos the position
     * @return if the tree is Slime Tree.
     */
    @Override
    @Optional.Method(modid = "tconstruct")
    public boolean checkForTinkersSlimeBlock(@NotNull final IBlockAccess world, @NotNull final BlockPos pos)
    {
        return world.getBlockState(pos).getBlock() == TinkerCommons.blockSlimeCongealed;
    }
}
