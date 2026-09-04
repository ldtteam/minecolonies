package com.minecolonies.api.blocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

/**
 * Abstract class for minecolonies named graves.
 */
public abstract class AbstractBlockMinecoloniesNamedGrave<B extends AbstractBlockMinecoloniesNamedGrave<B>> extends AbstractBlockMinecolonies<B> implements EntityBlock
{
    /**
     * The direction the block is facing.
     */
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public AbstractBlockMinecoloniesNamedGrave(final Properties properties)
    {
        super(properties.noOcclusion());
    }
}
