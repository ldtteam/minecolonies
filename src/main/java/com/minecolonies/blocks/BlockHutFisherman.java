package com.minecolonies.blocks;

import javax.annotation.Nonnull;

/**
 * Hut for the fisherman.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutFisherman extends AbstractBlockHut
{
    protected BlockHutFisherman()
    {
        //No different from Abstract parent
        super();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "blockHutFisherman";
    }
}
