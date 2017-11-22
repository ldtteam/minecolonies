package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the Farmer.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutFarmer extends AbstractBlockHut<BlockHutFarmer>
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
