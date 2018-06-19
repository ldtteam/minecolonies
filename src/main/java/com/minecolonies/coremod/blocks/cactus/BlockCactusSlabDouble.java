package com.minecolonies.coremod.blocks.cactus;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Implements the double cactus slab.
 */
public class BlockCactusSlabDouble extends AbstractBlockSlab<BlockCactusSlabDouble>
{
    /**
     * Unlocalized name for the slab.
     */
    private static final String NAME = "blockcactusslab_double";

    /**
     * Constructor for the double slab.
     */
    public BlockCactusSlabDouble()
    {
        super(Material.WOOD);
        setRegistryName(NAME);
        setUnlocalizedName(Constants.MOD_ID.toLowerCase(Locale.US) + "." + NAME);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean isDouble()
    {
        return true;
    }

    @NotNull
    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return BlockFaceShape.SOLID;
    }
}
