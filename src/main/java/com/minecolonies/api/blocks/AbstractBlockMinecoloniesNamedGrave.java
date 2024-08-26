package com.minecolonies.api.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Abstract class for minecolonies named graves.
 */
public abstract class AbstractBlockMinecoloniesNamedGrave<B extends AbstractBlockMinecoloniesNamedGrave<B>> extends Block implements EntityBlock
{
    /**
     * The direction the block is facing.
     */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AbstractBlockMinecoloniesNamedGrave(final Properties properties)
    {
        super(properties.noOcclusion());
    }
}
