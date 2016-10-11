package com.minecolonies.blocks;

import net.minecraft.block.Block;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 */
public final class ModBlocks
{
    /*
     * Creating objects for all blocks in the mod.
     * References can be made to here.
     */
    public static final Block blockHutTownHall   = new BlockHutTownHall();
    public static final Block blockHutCitizen    = new BlockHutCitizen();
    public static final Block blockHutMiner      = new BlockHutMiner();
    public static final Block blockHutLumberjack = new BlockHutLumberjack();
    public static final Block blockHutBaker      = new BlockHutBaker();
    public static final Block blockHutBuilder    = new BlockHutBuilder();
    public static final Block blockHutWarehouse  = new BlockHutWarehouse();
    public static final Block blockHutBlacksmith = new BlockHutBlacksmith();
    public static final Block blockHutStonemason = new BlockHutStonemason();
    public static final Block blockHutFarmer     = new BlockHutFarmer();
    public static final Block blockHutFisherman  = new BlockHutFisherman();
    public static final Block blockSubstitution  = new BlockSubstitution();
    public static final Block blockHutField      = new BlockHutField();
    public static final Block blockHutGuardTower = new BlockHutGuardTower();

    // Deactivated for now
    // public static final Block blockBarrel        = new BlockBarrel();

    /**
     * private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {
    }
}
