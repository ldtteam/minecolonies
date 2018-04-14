package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Block of the GuardTower hut.
 */
public class BlockHutGuardTower extends AbstractBlockHut<BlockHutGuardTower>
{
    /**
     * Default constructor.
     */
    protected BlockHutGuardTower()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutGuardTower";
    }
}
