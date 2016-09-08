package com.minecolonies.blocks;

import javax.annotation.Nonnull;

/**
 * Hut for the warehouse.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutWarehouse extends AbstractBlockHut
{
    protected BlockHutWarehouse()
    {
        //No different from Abstract parent
        super();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "blockHutWarehouse";
    }
}
