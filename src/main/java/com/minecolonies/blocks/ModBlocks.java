package com.minecolonies.blocks;

import net.minecraft.block.Block;

public final class ModBlocks
{
    public static Block blockHutTownhall, blockHutMiner;

    public static void init()
    {
        blockHutTownhall = new BlockHutTownHall();
        blockHutMiner = new BlockHutMiner();
    }
}
