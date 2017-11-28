package com.minecolonies.coremod.blocks;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.IForgeRegistry;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 *
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444", "squid:S1820" , })
public final class ModBlocks
{
    /*
     * Creating objects for all blocks in the mod.
     * References can be made to here.
     */
    public static BlockHutTownHall            blockHutTownHall;
    public static BlockHutCitizen             blockHutCitizen;
    public static BlockHutMiner               blockHutMiner;
    public static BlockHutLumberjack          blockHutLumberjack;
    public static BlockHutBaker               blockHutBaker;
    public static BlockHutBuilder             blockHutBuilder;
    public static BlockHutDeliveryman         blockHutDeliveryman;
    public static BlockHutBlacksmith          blockHutBlacksmith;
    public static BlockHutStonemason          blockHutStonemason;
    public static BlockHutFarmer              blockHutFarmer;
    public static BlockHutFisherman           blockHutFisherman;
    public static BlockSubstitution           blockSubstitution;
    public static BlockSolidSubstitution      blockSolidSubstitution;
    public static BlockHutField               blockHutField;
    public static BlockHutGuardTower          blockHutGuardTower;
    public static BlockHutWareHouse           blockHutWareHouse;
    public static BlockConstructionTape       blockConstructionTape;
    public static BlockConstructionTapeCorner blockConstructionTapeCorner;
    public static BlockMinecoloniesRack       blockRack;
    public static BlockTimberFrame            blockTimberFrame;
    public static BlockWaypoint               blockWayPoint;
    public static BlockHutBarracks            blockHutBarracks;
    public static BlockHutBarracksTower       blockHutBarracksTower;
    public static BlockInfoPoster             blockInfoPoster;
    public static BlockPaperwall              blockPaperWall;
    public static BlockHutCook                blockHutCook;


    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Initates all the blocks. At the correct time.
     */
    public static void init(final IForgeRegistry<Block> registry)
    {
        blockConstructionTape = new BlockConstructionTape().registerBlock(registry);
        blockConstructionTapeCorner = new BlockConstructionTapeCorner().registerBlock(registry);
        blockHutBaker = new BlockHutBaker().registerBlock(registry);
        blockHutBlacksmith = new BlockHutBlacksmith().registerBlock(registry);
        blockHutBuilder = new BlockHutBuilder().registerBlock(registry);
        blockHutCitizen = new BlockHutCitizen().registerBlock(registry);
        blockHutDeliveryman = new BlockHutDeliveryman().registerBlock(registry);
        blockHutFarmer = new BlockHutFarmer().registerBlock(registry);
        blockHutField = new BlockHutField().registerBlock(registry);
        blockHutFisherman = new BlockHutFisherman().registerBlock(registry);
        blockHutGuardTower = new BlockHutGuardTower().registerBlock(registry);
        blockHutLumberjack = new BlockHutLumberjack().registerBlock(registry);
        blockHutMiner = new BlockHutMiner().registerBlock(registry);
        blockHutStonemason = new BlockHutStonemason().registerBlock(registry);
        blockHutTownHall = new BlockHutTownHall().registerBlock(registry);
        blockHutWareHouse = new BlockHutWareHouse().registerBlock(registry);
        blockSolidSubstitution = new BlockSolidSubstitution().registerBlock(registry);
        blockSubstitution = new BlockSubstitution().registerBlock(registry);
        blockRack = new BlockMinecoloniesRack().registerBlock(registry);
        blockTimberFrame = new BlockTimberFrame().registerBlock(registry);
        blockWayPoint = new BlockWaypoint().registerBlock(registry);
        blockHutBarracks = new BlockHutBarracks().registerBlock(registry);
        blockHutBarracksTower = new BlockHutBarracksTower().registerBlock(registry);
        blockInfoPoster = new BlockInfoPoster().registerBlock(registry);
        blockPaperWall = new BlockPaperwall().registerBlock(registry);
        blockHutCook   = new BlockHutCook().registerBlock(registry);
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
        blockRack.registerItemBlock(registry);
        blockTimberFrame.registerItemBlock(registry);
        blockWayPoint.registerItemBlock(registry);
        blockHutBarracksTower.registerItemBlock(registry);
        blockHutBarracks.registerItemBlock(registry);
        blockInfoPoster.registerItemBlock(registry);
        blockPaperWall.registerItemBlock(registry);
        blockHutCook.registerItemBlock(registry);
    }
}