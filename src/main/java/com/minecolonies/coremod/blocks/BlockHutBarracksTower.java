package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Block of the BarracksTower.
 */
public class BlockHutBarracksTower extends AbstractBlockHut
{
    /**
     * Default constructor.
     */
    protected BlockHutBarracksTower()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBarracksTower";
    }
}
