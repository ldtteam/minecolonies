package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Hut for the blacksmith.
 * No different from {@link AbstractBlockHut}
 */
public class BlockHutBlacksmith extends AbstractBlockHut
{
    protected BlockHutBlacksmith()
    {
        //No different from Abstract parent
        super();
    }

    @NotNull
    @Override
    public String getName()
    {
        return "blockHutBlacksmith";
    }
}
