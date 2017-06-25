package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the miner.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutMiner extends AbstractBlockHut
{
    protected BlockHutMiner()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutMiner";
    }
}
