package com.minecolonies.blocks;

import javax.annotation.Nonnull;

/**
 * Hut for the miner.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutMiner extends AbstractBlockHut
{
    protected BlockHutMiner()
    {
        //No different from Abstract parent
        super();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "blockHutMiner";
    }
}
