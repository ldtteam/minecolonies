package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the stone mason.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutStonemason extends AbstractBlockHut
{
    protected BlockHutStonemason()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutStonemason";
    }
}
