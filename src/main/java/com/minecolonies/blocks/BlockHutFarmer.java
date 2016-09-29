package com.minecolonies.blocks;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutFarmer";
    }
}
