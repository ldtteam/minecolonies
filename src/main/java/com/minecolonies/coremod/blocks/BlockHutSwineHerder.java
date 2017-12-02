package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the shepherd.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutSwinHerder extends AbstractBlockHut
{
    protected BlockHutSwinHerder()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutSwineHerder";
    }
}
