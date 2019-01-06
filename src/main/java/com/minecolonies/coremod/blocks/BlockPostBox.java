package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the PostBox.
 * No different from {@link AbstractBlockHut}
 */
public class BlockPostBox extends AbstractBlockHut<BlockPostBox>
{
    public BlockPostBox()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockpostbox";
    }
}
