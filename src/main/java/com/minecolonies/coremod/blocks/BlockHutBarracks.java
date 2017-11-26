package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Block of the Barracks.
 */
public class BlockHutBarracks extends AbstractBlockHut<BlockHutBarracks>
{
    /**
     * Default constructor.
     */
    protected BlockHutBarracks()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBarracks";
    }
}
