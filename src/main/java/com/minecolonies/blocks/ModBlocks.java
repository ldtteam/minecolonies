package com.minecolonies.blocks;

import net.minecraft.block.Block;

public final class ModBlocks
{
    public static Block myBlock;

    public static void init()
    {
        myBlock = new MyBlock();
    }
}
