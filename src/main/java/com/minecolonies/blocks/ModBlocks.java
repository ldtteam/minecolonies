package com.minecolonies.blocks;

import net.minecraft.block.Block;

public final class ModBlocks
{
    public static Block blockHutTownhall, blockHutMiner, blockHutLumberjack, blockHutBaker, blockHutBuilder, blockHutDeliveryman, blockHutBlacksmith, blockHutStonemason, blockHutFarmer;

    public static void init()
    {
        blockHutTownhall = new BlockHutTownHall();
        blockHutMiner = new BlockHutMiner();
        blockHutLumberjack = new BlockHutLumberjack();
        blockHutBaker = new BlockHutBaker();
        blockHutBuilder = new BlockHutBuilder();
        blockHutDeliveryman = new BlockHutDeliveryman();
        blockHutBlacksmith = new BlockHutBlacksmith();
        blockHutStonemason = new BlockHutStonemason();
        blockHutFarmer = new BlockHutFarmer();
    }
}
