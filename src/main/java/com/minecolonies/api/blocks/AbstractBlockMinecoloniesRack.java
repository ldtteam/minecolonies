package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.types.RackType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public abstract class AbstractBlockMinecoloniesRack<B extends AbstractBlockMinecoloniesRack<B>> extends Block implements EntityBlock
{
    public static final EnumProperty<RackType> VARIANT = EnumProperty.create("variant", RackType.class);

    /**
     * The position it faces.
     */
    public static final DirectionProperty      FACING       = HorizontalDirectionalBlock.FACING;

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
