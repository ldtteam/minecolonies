package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the citizen.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutCitizen extends AbstractBlockHut<BlockHutCitizen>
{
    protected BlockHutCitizen()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutCitizen";
    }
}
