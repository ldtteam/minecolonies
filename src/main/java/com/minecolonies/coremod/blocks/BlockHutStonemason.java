package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the stone mason.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutStonemason extends AbstractBlockHut
{
    protected BlockHutStonemason()
    {
        //No different from Abstract parent
        super();
    }

    public String getJobName()
    {
        return "Stonemason";
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutStonemason";
    }
}
