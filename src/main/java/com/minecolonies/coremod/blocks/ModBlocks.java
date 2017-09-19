package com.minecolonies.coremod.blocks;

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
    public static final Block blockHutTownHall            = new BlockHutTownHall();
    public static final Block blockHutCitizen             = new BlockHutCitizen();
    public static final Block blockHutMiner               = new BlockHutMiner();
    public static final Block blockHutLumberjack          = new BlockHutLumberjack();
    public static final Block blockHutBaker               = new BlockHutBaker();
    public static final Block blockHutBuilder             = new BlockHutBuilder();
    public static final Block blockHutDeliveryman         = new BlockHutDeliveryman();
    public static final Block blockHutBlacksmith          = new BlockHutBlacksmith();
    public static final Block blockHutStonemason          = new BlockHutStonemason();
    public static final Block blockHutFarmer              = new BlockHutFarmer();
    public static final Block blockHutFisherman           = new BlockHutFisherman();
    public static final Block blockSubstitution           = new BlockSubstitution();
    public static final Block blockSolidSubstitution      = new BlockSolidSubstitution();
    public static final Block blockHutField               = new BlockHutField();
    public static final Block blockHutGuardTower          = new BlockHutGuardTower();
    public static final Block blockHutWareHouse           = new BlockHutWareHouse();
    public static final Block blockConstructionTape       = new BlockConstructionTape();
    public static final Block blockConstructionTapeCorner = new BlockConstructionTapeCorner();
    public static final Block blockTimberFrame            = new BlockTimberFrame();
    public static final Block blockRack                   = new BlockMinecoloniesRack();
    public static final Block blockWayPoint               = new BlockWaypoint();
    public static final Block blockHutBarracks            = new BlockHutBarracks();
    public static final Block blockHutBarracksTower       = new BlockHutBarracksTower();
    public static final Block blockInfoPoster             = new BlockInfoPoster();


    // Deactivated for now
    // public static final Block blockBarrel        = new BlockBarrel();

    /**
     * private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {
    }
}
