package com.minecolonies.blocks;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBaker";
    }
}
