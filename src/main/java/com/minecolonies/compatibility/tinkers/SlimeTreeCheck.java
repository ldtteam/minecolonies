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
public class SlimeTreeCheck
{
    private SlimeTreeCheck()
    {
        throw new IllegalAccessError("Utility class");
    }

    public static boolean checkTinkersTree(@NotNull final World world, @NotNull final BlockPos pos)
    {
        Block block = world.getBlockState(pos).getBlock();
        return world.getBlockState(pos).getMaterial() == Material.CLAY && block.slipperiness == 0.5f && block.getSoundType() == SoundType.SLIME;
    }
    public static boolean checkTinkersTree(@NotNull final IBlockAccess world, @NotNull final BlockPos pos)
    {
        Block block = world.getBlockState(pos).getBlock();
        return world.getBlockState(pos).getMaterial() == Material.CLAY && block.slipperiness == 0.5f && block.getSoundType() == SoundType.SLIME;
    }
}
