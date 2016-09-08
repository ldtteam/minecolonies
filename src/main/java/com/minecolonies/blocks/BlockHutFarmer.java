package com.minecolonies.blocks;

import javax.annotation.Nonnull;

/**
 * Hut for the Farmer.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutFarmer extends AbstractBlockHut
{
    protected BlockHutFarmer()
    {
        //No different from Abstract parent
        super();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "blockHutFarmer";
    }
}
