package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the warehouse.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutDeliveryman extends AbstractBlockHut<BlockHutDeliveryman>
{
    public BlockHutDeliveryman()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutDeliveryman";
    }
}
