package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the Farmer.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutFarmer extends AbstractBlockHut<BlockHutFarmer>
{
    public BlockHutFarmer()
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
