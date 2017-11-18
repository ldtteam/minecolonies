package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the miner.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutMiner extends AbstractBlockHut<BlockHutMiner>
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
