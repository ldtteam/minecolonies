package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the builder.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBuilder extends AbstractBlockHut
{
    protected BlockHutBuilder()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBuilder";
    }
}
