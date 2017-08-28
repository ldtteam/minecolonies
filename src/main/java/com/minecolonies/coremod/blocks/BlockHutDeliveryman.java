package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the warehouse.
 * No different from {@link AbstractBlockHut}
 */

public class BlockHutDeliveryman extends AbstractBlockHut
{
    protected BlockHutDeliveryman()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutDeliveryman";
    }
}
