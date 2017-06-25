package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

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
    public static BlockHutTownHall    blockHutTownHall;
    public static BlockHutCitizen     blockHutCitizen;
    public static BlockHutMiner     blockHutMiner;
    public static BlockHutLumberjack  blockHutLumberjack;
    public static BlockHutBaker       blockHutBaker;
    public static BlockHutBuilder     blockHutBuilder;
    public static BlockHutDeliveryman blockHutDeliveryman;
    public static BlockHutBlacksmith       blockHutBlacksmith;
    public static BlockHutStonemason  blockHutStonemason;
    public static BlockHutFarmer      blockHutFarmer;
    public static BlockHutFisherman   blockHutFisherman;
    public static BlockSubstitution      blockSubstitution;
    public static BlockSolidSubstitution blockSolidSubstitution;
    public static BlockHutField          blockHutField;
    public static BlockHutGuardTower     blockHutGuardTower;
    public static BlockHutWareHouse     blockHutWareHouse;
    public static BlockConstructionTape  blockConstructionTape;
    public static BlockConstructionTapeCorner blockConstructionTapeCorner;


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
     * @param registry
     */
    public static void init(final IForgeRegistry<Block> registry)
    {
        blockConstructionTape = new BlockConstructionTape().registerBlock(registry);
        blockConstructionTapeCorner = new BlockConstructionTapeCorner().registerBlock(registry);
        blockHutBaker = (BlockHutBaker) new BlockHutBaker().registerBlock(registry);
        blockHutBlacksmith = (BlockHutBlacksmith) new BlockHutBlacksmith().registerBlock(registry);
        blockHutBuilder = (BlockHutBuilder) new BlockHutBuilder().registerBlock(registry);
        blockHutCitizen = (BlockHutCitizen) new BlockHutCitizen().registerBlock(registry);
        blockHutDeliveryman = (BlockHutDeliveryman) new BlockHutDeliveryman().registerBlock(registry);
        blockHutFarmer = (BlockHutFarmer) new BlockHutFarmer().registerBlock(registry);
        blockHutField = new BlockHutField().registerBlock(registry);
        blockHutFisherman = (BlockHutFisherman) new BlockHutFisherman().registerBlock(registry);
        blockHutGuardTower = (BlockHutGuardTower) new BlockHutGuardTower().registerBlock(registry);
        blockHutLumberjack = (BlockHutLumberjack) new BlockHutLumberjack().registerBlock(registry);
        blockHutMiner = (BlockHutMiner) new BlockHutMiner().registerBlock(registry);
        blockHutStonemason = (BlockHutStonemason) new BlockHutStonemason().registerBlock(registry);
        blockHutTownHall = (BlockHutTownHall) new BlockHutTownHall().registerBlock(registry);
        blockHutWareHouse = (BlockHutWareHouse) new BlockHutWareHouse().registerBlock(registry);
        blockSolidSubstitution = new BlockSolidSubstitution().registerBlock(registry);
        blockSubstitution = new BlockSubstitution().registerBlock(registry);
    }

    public static void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        blockConstructionTape.registerItemBlock(registry);
        blockConstructionTapeCorner.registerItemBlock(registry);
        blockHutBaker.registerItemBlock(registry);
        blockHutBlacksmith.registerItemBlock(registry);
        blockHutBuilder.registerItemBlock(registry);
        blockHutCitizen.registerItemBlock(registry);
        blockHutDeliveryman.registerItemBlock(registry);
        blockHutFarmer.registerItemBlock(registry);
        blockHutField.registerItemBlock(registry);
        blockHutFisherman.registerItemBlock(registry);
        blockHutGuardTower.registerItemBlock(registry);
        blockHutLumberjack.registerItemBlock(registry);
        blockHutMiner.registerItemBlock(registry);
        blockHutStonemason.registerItemBlock(registry);
        blockHutTownHall.registerItemBlock(registry);
        blockHutWareHouse.registerItemBlock(registry);
        blockSolidSubstitution.registerItemBlock(registry);
        blockSubstitution.registerItemBlock(registry);
    }
}
