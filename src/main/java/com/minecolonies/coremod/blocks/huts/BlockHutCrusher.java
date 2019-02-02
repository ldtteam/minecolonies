package com.minecolonies.coremod.blocks.huts;

import com.minecolonies.coremod.blocks.AbstractBlockHut;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the crusher.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutCrusher extends AbstractBlockHut<BlockHutCrusher>
{
    public BlockHutCrusher()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutCrusher";
    }
}
