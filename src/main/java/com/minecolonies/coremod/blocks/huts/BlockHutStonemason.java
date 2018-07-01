package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the stone mason.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutStonemason extends AbstractBlockHut<BlockHutStonemason>
{
    public BlockHutStonemason()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutStonemason";
    }

}
