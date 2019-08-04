package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.api.blocks.AbstractBlockHut;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the baker.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBaker extends AbstractBlockHut<BlockHutBaker>
{
    public BlockHutBaker()
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
