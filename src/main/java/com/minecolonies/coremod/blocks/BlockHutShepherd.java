package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the shepherd.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutShepherd extends AbstractBlockHut<BlockHutShepherd>
{
    protected BlockHutShepherd()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutShepherd";
    }
}
