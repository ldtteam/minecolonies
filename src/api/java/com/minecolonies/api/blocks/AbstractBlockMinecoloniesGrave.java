package com.minecolonies.api.blocks;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.state.DirectionProperty;

public abstract class AbstractBlockMinecoloniesGrave<B extends AbstractBlockMinecoloniesGrave<B>> extends AbstractBlockMinecolonies<B>
{
    /**
     * The position it faces.
     */
    public static final DirectionProperty      FACING       = HorizontalBlock.HORIZONTAL_FACING;

    public AbstractBlockMinecoloniesGrave(final Properties properties)
    {
        super(properties.notSolid());
    }

}
