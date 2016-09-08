package com.minecolonies.blocks;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutWarehouse";
    }
}
