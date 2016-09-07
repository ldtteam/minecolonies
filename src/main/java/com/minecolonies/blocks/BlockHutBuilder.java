package com.minecolonies.blocks;

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

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBuilder";
    }
}
