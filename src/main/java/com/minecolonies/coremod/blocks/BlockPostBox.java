package com.minecolonies.coremod.blocks;

import com.minecolonies.api.blocks.AbstractBlockHut;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the PostBox.
 * No different from {@link AbstractBlockHut}
 */
public class BlockPostBox extends AbstractBlockHut<BlockPostBox>
{
    @NotNull
    @Override
    public String getName()
    {
        return "blockpostbox";
    }
}
