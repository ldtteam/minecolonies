package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the lumberjack.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutLumberjack extends AbstractBlockHut<BlockHutLumberjack>
{
    protected BlockHutLumberjack()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutLumberjack";
    }
}
