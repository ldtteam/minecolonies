package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the Smeltery.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutSmeltery extends AbstractBlockHut<BlockHutSmeltery>
{
    protected BlockHutSmeltery()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockhutsmeltery";
    }
}
