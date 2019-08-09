package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.api.blocks.types.RackType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.init.Blocks;

public abstract class AbstractBlockMinecoloniesRack<B extends AbstractBlockMinecoloniesRack<B>> extends AbstractBlockMinecolonies<B> implements IBlockMinecolonies<B>
{
    public static final PropertyEnum<RackType> VARIANT
                                                            =
      PropertyEnum.create("variant", RackType.class);
    public static final int                    DEFAULT_META = RackType.DEFAULT.getMetadata();
    public static final int                    FULL_META    = RackType.FULL.getMetadata();
    /**
     * The position it faces.
     */
    public static final PropertyDirection      FACING       = BlockHorizontal.FACING;

    public AbstractBlockMinecoloniesRack(final Material blockMaterialIn, final MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
    }

    public AbstractBlockMinecoloniesRack(final Material materialIn)
    {
        super(materialIn);
    }

    /**
     * Check if a certain block should be replaced with a rack.
     *
     * @param block the block to check.
     * @return true if so.
     */
    public static boolean shouldBlockBeReplacedWithRack(final Block block)
    {
        return block == Blocks.CHEST || block instanceof AbstractBlockMinecoloniesRack;
    }
}
