package com.minecolonies.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the citizen.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutLumberjack extends AbstractBlockHut
{
    protected BlockHutLumberjack()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutLumberjack";
    }
}
