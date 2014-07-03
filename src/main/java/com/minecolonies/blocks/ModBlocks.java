package com.minecolonies.blocks;

import net.minecraft.block.Block;

public final class ModBlocks
{
    public static Block blockHutTownhall, blockHutCitizen, blockHutMiner, blockHutLumberjack, blockHutBaker, blockHutBuilder, blockHutWarehouse, blockHutBlacksmith, blockHutStonemason, blockHutFarmer;

    public static void init()
    {
        blockHutTownhall = new BlockHutTownHall();
        blockHutCitizen = new BlockHutCitizen();
        blockHutMiner = new BlockHutMiner();
        blockHutLumberjack = new BlockHutLumberjack();
        blockHutBaker = new BlockHutBaker();
        blockHutBuilder = new BlockHutBuilder();
        blockHutWarehouse = new BlockHutWarehouse();
        blockHutBlacksmith = new BlockHutBlacksmith();
        blockHutStonemason = new BlockHutStonemason();
        blockHutFarmer = new BlockHutFarmer();
    }
}
