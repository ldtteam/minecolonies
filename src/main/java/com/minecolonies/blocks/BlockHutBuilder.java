package com.minecolonies.blocks;

import javax.annotation.Nonnull;

/**
 * Hut for the builder.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBuilder extends AbstractBlockHut
{
    protected BlockHutBuilder()
    {
        //No different from Abstract parent
        super();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "blockHutBuilder";
    }
}
