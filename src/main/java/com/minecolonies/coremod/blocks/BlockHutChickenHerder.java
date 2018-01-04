package com.minecolonies.coremod.blocks;

import org.jetbrains.annotations.NotNull;

/**
 * Hut for the shepherd.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutChickenHerder extends AbstractBlockHut<BlockHutChickenHerder>
{
    protected BlockHutChickenHerder()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutChickenHerder";
    }
}
