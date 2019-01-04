package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the sawmill.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutSawmill extends AbstractBlockHut<BlockHutSawmill>
{
    protected BlockHutSawmill()
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
