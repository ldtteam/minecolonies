package com.minecolonies.api.blocks;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.DirectionProperty;

public abstract class AbstractBlockMinecoloniesNamedGrave<B extends AbstractBlockMinecoloniesNamedGrave<B>> extends AbstractBlockMinecolonies<B>
{
    /**
     * The position it faces.
     */
    public static final DirectionProperty      FACING       = HorizontalBlock.HORIZONTAL_FACING;

    public AbstractBlockMinecoloniesNamedGrave(final Properties properties)
    {
        super(properties.notSolid());
    }
}
