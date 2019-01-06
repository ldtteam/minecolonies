package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the sawmill.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutSawmill extends AbstractBlockHut<BlockHutSawmill>
{
    public BlockHutSawmill()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutSawmill";
    }
}
