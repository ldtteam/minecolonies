package com.minecolonies.blocks;

import javax.annotation.Nonnull;

/**
 * Hut for the stone mason.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutStonemason extends AbstractBlockHut
{
    protected BlockHutStonemason()
    {
        //No different from Abstract parent
        super();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "blockHutStonemason";
    }
}
