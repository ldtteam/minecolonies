package com.minecolonies.blocks;

import javax.annotation.Nonnull;

/**
 * Hut for the baker.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBaker extends AbstractBlockHut
{
    protected BlockHutBaker()
    {
        //No different from Abstract parent
        super();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "blockHutBaker";
    }
}
