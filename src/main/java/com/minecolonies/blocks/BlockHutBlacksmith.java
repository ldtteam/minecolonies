package com.minecolonies.blocks;

import javax.annotation.Nonnull;

/**
 * Hut for the blacksmith.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBlacksmith extends AbstractBlockHut
{
    protected BlockHutBlacksmith()
    {
        //No different from Abstract parent
        super();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "blockHutBlacksmith";
    }
}
