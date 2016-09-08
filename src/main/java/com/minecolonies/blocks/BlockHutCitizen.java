package com.minecolonies.blocks;

import javax.annotation.Nonnull;

/**
 * Hut for the citizen.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutCitizen extends AbstractBlockHut
{
    protected BlockHutCitizen()
    {
        //No different from Abstract parent
        super();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "blockHutCitizen";
    }
}
