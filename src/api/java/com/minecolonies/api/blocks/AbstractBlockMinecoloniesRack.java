package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.types.RackType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;

public abstract class AbstractBlockMinecoloniesRack<B extends AbstractBlockMinecoloniesRack<B>> extends AbstractBlockMinecolonies<B>
{
    public static final EnumProperty<RackType> VARIANT = EnumProperty.create("variant", RackType.class);

    /**
     * The position it faces.
     */
    public static final DirectionProperty      FACING       = HorizontalBlock.FACING;

    public AbstractBlockMinecoloniesRack(final Properties properties)
    {
        super(properties.noOcclusion());
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
