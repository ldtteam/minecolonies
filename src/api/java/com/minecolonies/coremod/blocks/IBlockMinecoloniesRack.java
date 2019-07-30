package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.coremod.blocks.types.RackType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.init.Blocks;

public interface IBlockMinecoloniesRack<B extends IBlockMinecoloniesRack<B>> extends IBlockMinecolonies<B>
{
    PropertyEnum<RackType> VARIANT
                                        =
      PropertyEnum.create("variant", RackType.class);
    int                    DEFAULT_META = RackType.DEFAULT.getMetadata();
    int                    FULL_META = RackType.FULL.getMetadata();
    /**
     * The position it faces.
     */
    PropertyDirection      FACING = BlockHorizontal.FACING;

    /**
     * Check if a certain block should be replaced with a rack.
     *
     * @param block the block to check.
     * @return true if so.
     */
    static boolean shouldBlockBeReplacedWithRack(Block block)
    {
        return block == Blocks.CHEST || block instanceof IBlockMinecoloniesRack;
    }
}
