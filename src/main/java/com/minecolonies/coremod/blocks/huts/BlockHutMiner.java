package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the miner.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutMiner extends AbstractBlockHut<BlockHutMiner>
{
    public BlockHutMiner()
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
