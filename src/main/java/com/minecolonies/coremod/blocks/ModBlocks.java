package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 */
public class ModBlocks
{
    /*
     * Creating objects for all blocks in the mod.
     * References can be made to here.
     */
    public static Block blockHutTownHall;
    public static Block blockHutCitizen;
    public static Block blockHutMiner;
    public static Block blockHutLumberjack;
    public static Block blockHutBaker;
    public static Block blockHutBuilder;
    public static Block blockHutDeliveryman;
    public static Block blockHutBlacksmith;
    public static Block blockHutStonemason;
    public static Block blockHutFarmer;
    public static Block blockHutFisherman;
    public static Block blockSubstitution;
    public static Block blockSolidSubstitution;
    public static Block blockHutField;
    public static Block blockHutGuardTower;
    public static Block blockHutWareHouse;
    public static Block blockConstructionTape;
    public static Block blockConstructionTapeCorner;


    // Deactivated for now
    // public static  Block blockBarrel        = new BlockBarrel();

    /**
     * Private constructor to hide the implicit public one.
     */
    public ModBlocks()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Initates all the blocks. At the correct time.
     */
    public static void init()
    {
        blockConstructionTape = new BlockConstructionTape();
        blockConstructionTapeCorner = new BlockConstructionTapeCorner();
        blockHutBaker = new BlockHutBaker();
        blockHutBlacksmith = new BlockHutBlacksmith();
        blockHutBuilder = new BlockHutBuilder();
        blockHutCitizen = new BlockHutCitizen();
        blockHutDeliveryman = new BlockHutDeliveryman();
        blockHutFarmer = new BlockHutFarmer();
        blockHutField = new BlockHutField();
        blockHutFisherman = new BlockHutFisherman();
        blockHutGuardTower = new BlockHutGuardTower();
        blockHutLumberjack = new BlockHutLumberjack();
        blockHutMiner = new BlockHutMiner();
        blockHutStonemason = new BlockHutStonemason();
        blockHutTownHall = new BlockHutTownHall();
        blockHutWareHouse = new BlockHutWareHouse();
        blockSolidSubstitution = new BlockSolidSubstitution();
        blockSubstitution = new BlockSubstitution();
    }
}
