package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the builder.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBuilder extends AbstractBlockHut
{
    protected BlockHutBuilder()
    {
        //No different from Abstract parent
        super();
    }

    public String getJobName()
    {
        return "Builder";
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBuilder";
    }
}
