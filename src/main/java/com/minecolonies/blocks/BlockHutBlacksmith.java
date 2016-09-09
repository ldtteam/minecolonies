package com.minecolonies.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the blacksmith.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBlacksmith extends AbstractBlockHut
{
    protected BlockHutBlacksmith()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBlacksmith";
    }
}
