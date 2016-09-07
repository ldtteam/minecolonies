package com.minecolonies.blocks;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutMiner";
    }
}
