package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the fisherman.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutFisherman extends AbstractBlockHut
{
    protected BlockHutFisherman()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutFisherman";
    }
}
