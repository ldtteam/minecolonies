package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the lumberjack.
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
