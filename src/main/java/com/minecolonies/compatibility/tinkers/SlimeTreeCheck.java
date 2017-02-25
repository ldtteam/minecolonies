package com.minecolonies.compatibility.tinkers;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * This class is to store a check to see if a tree is a slime tree.
 */
public final class SlimeTreeCheck
{
    private static final double SLIME_BLOCK_SLIPPERINESS = 0.5D;
    private SlimeTreeCheck()
    {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Check if tree is Slime Tree.
     * @param world the world.
     * @param pos the position
     * @return if the tree is Slime Tree.
     */
    public static boolean checkTinkersTree(@NotNull final World world, @NotNull final BlockPos pos)
    {
        final Block block = world.getBlockState(pos).getBlock();
        return world.getBlockState(pos).getMaterial() == Material.CLAY && block.slipperiness == SLIME_BLOCK_SLIPPERINESS && block.getSoundType() == SoundType.SLIME;
    }
    /**
     * Check if tree is Slime Tree.
     * @param world the world.
     * @param pos the position
     * @return if the tree is Slime Tree.
     */
    public static boolean checkTinkersTree(@NotNull final IBlockAccess world, @NotNull final BlockPos pos)
    {
        final Block block = world.getBlockState(pos).getBlock();
        return world.getBlockState(pos).getMaterial() == Material.CLAY && block.slipperiness == SLIME_BLOCK_SLIPPERINESS && block.getSoundType() == SoundType.SLIME;
    }
}
