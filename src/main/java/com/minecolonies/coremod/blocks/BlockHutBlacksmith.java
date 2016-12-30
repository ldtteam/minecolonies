package com.minecolonies.coremod.blocks;

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

    public String getJobName()
    {
        return "Blacksmith";
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBlacksmith";
    }
}
