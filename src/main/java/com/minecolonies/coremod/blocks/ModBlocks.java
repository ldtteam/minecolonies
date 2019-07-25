package com.minecolonies.coremod.blocks;

import com.minecolonies.coremod.blocks.decorative.*;
import com.minecolonies.coremod.blocks.huts.*;
import com.minecolonies.coremod.blocks.schematic.BlockWaypoint;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Class to create the modBlocks.
 * References to the blocks can be made here
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444", "squid:S1820",})

public final class ModBlocks
{
    /*
     * Creating objects for all blocks in the mod.
     * References can be made to here.
     */

    public static BlockHutTownHall               blockHutTownHall;
    public static BlockHutCitizen                blockHutCitizen;
    public static BlockHutMiner                  blockHutMiner;
    public static BlockHutLumberjack             blockHutLumberjack;
    public static BlockHutBaker                  blockHutBaker;
    public static BlockHutBuilder                blockHutBuilder;
    public static BlockHutDeliveryman            blockHutDeliveryman;
    public static BlockHutBlacksmith             blockHutBlacksmith;
    public static BlockHutStonemason             blockHutStonemason;
    public static BlockHutFarmer                 blockHutFarmer;
    public static BlockHutFisherman              blockHutFisherman;
    public static BlockBarracksTowerSubstitution blockBarracksTowerSubstitution;
    public static BlockHutField                  blockHutField;
    public static BlockHutGuardTower             blockHutGuardTower;
    public static BlockHutWareHouse              blockHutWareHouse;
    public static BlockHutShepherd               blockHutShepherd;
    public static BlockHutCowboy                 blockHutCowboy;
    public static BlockHutSwineHerder            blockHutSwineHerder;
    public static BlockHutChickenHerder          blockHutChickenHerder;
    public static BlockHutBarracks               blockHutBarracks;
    public static BlockHutBarracksTower          blockHutBarracksTower;
    public static BlockHutCook                   blockHutCook;
    public static BlockHutSmeltery               blockHutSmeltery;
    public static BlockHutComposter              blockHutComposter;
    public static BlockHutLibrary                blockHutLibrary;
    public static BlockHutArchery                blockHutArchery;
    public static BlockHutCombatAcademy          blockHutCombatAcademy;
    public static BlockHutSawmill                blockHutSawmill;
    public static BlockHutStoneSmeltery          blockHutStoneSmeltery;
    public static BlockHutCrusher                blockHutCrusher;
    public static BlockHutSifter                 blockHutSifter;

    /**
     * Utility blocks.
     */
    public static BlockConstructionTape     blockConstructionTape;
    public static BlockMinecoloniesRack     blockRack;
    public static BlockWaypoint             blockWayPoint;
    public static BlockInfoPoster           blockInfoPoster;
    public static BlockBarrel               blockBarrel;
    public static BlockPostBox              blockPostBox;
    public static BlockDecorationController blockDecorationPlacerholder;

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
        blockHutShepherd = new BlockHutShepherd().registerBlock(registry);
        blockHutCowboy = new BlockHutCowboy().registerBlock(registry);
        blockHutSwineHerder = new BlockHutSwineHerder().registerBlock(registry);
        blockHutChickenHerder = new BlockHutChickenHerder().registerBlock(registry);
        blockHutBarracks = new BlockHutBarracks().registerBlock(registry);
        blockHutBarracksTower = new BlockHutBarracksTower().registerBlock(registry);
        blockHutCook = new BlockHutCook().registerBlock(registry);
        blockHutSmeltery = new BlockHutSmeltery().registerBlock(registry);
        blockHutComposter = new BlockHutComposter().registerBlock(registry);
        blockHutLibrary = new BlockHutLibrary().registerBlock(registry);
        blockHutArchery = new BlockHutArchery().registerBlock(registry);
        blockHutSawmill = new BlockHutSawmill().registerBlock(registry);
        blockHutCombatAcademy = new BlockHutCombatAcademy().registerBlock(registry);
        blockHutStoneSmeltery = new BlockHutStoneSmeltery().registerBlock(registry);
        blockHutCrusher = new BlockHutCrusher().registerBlock(registry);
        blockHutSifter = new BlockHutSifter().registerBlock(registry);

        blockInfoPoster = new BlockInfoPoster().registerBlock(registry);
        blockConstructionTape = new BlockConstructionTape().registerBlock(registry);
        blockBarracksTowerSubstitution = new BlockBarracksTowerSubstitution().registerBlock(registry);
        blockRack = new BlockMinecoloniesRack().registerBlock(registry);
        blockWayPoint = new BlockWaypoint().registerBlock(registry);
        blockPostBox = new BlockPostBox().registerBlock(registry);
        blockDecorationPlacerholder = new BlockDecorationController(Material.WOOD).registerBlock(registry);
        blockBarrel = new BlockBarrel().registerBlock(registry);
    }

    public static void registerBlockItem(final IForgeRegistry<Item> registry)
    {
        blockHutBaker.registerBlockItem(registry);
        blockHutBlacksmith.registerBlockItem(registry);
        blockHutBuilder.registerBlockItem(registry);
        blockHutCitizen.registerBlockItem(registry);
        blockHutDeliveryman.registerBlockItem(registry);
        blockHutFarmer.registerBlockItem(registry);
        blockHutField.registerBlockItem(registry);
        blockHutFisherman.registerBlockItem(registry);
        blockHutGuardTower.registerBlockItem(registry);
        blockHutLumberjack.registerBlockItem(registry);
        blockHutMiner.registerBlockItem(registry);
        blockHutStonemason.registerBlockItem(registry);
        blockHutTownHall.registerBlockItem(registry);
        blockHutWareHouse.registerBlockItem(registry);
        blockHutShepherd.registerBlockItem(registry);
        blockHutCowboy.registerBlockItem(registry);
        blockHutSwineHerder.registerBlockItem(registry);
        blockHutChickenHerder.registerBlockItem(registry);
        blockHutBarracksTower.registerBlockItem(registry);
        blockHutBarracks.registerBlockItem(registry);
        blockHutCook.registerBlockItem(registry);
        blockHutSmeltery.registerBlockItem(registry);
        blockHutComposter.registerBlockItem(registry);
        blockHutLibrary.registerBlockItem(registry);
        blockHutArchery.registerBlockItem(registry);
        blockHutCombatAcademy.registerBlockItem(registry);
        blockHutSawmill.registerBlockItem(registry);
        blockHutStoneSmeltery.registerBlockItem(registry);
        blockHutCrusher.registerBlockItem(registry);
        blockHutSifter.registerBlockItem(registry);

        blockConstructionTape.registerBlockItem(registry);
        blockBarracksTowerSubstitution.registerBlockItem(registry);
        blockRack.registerBlockItem(registry);
        blockWayPoint.registerBlockItem(registry);
        blockInfoPoster.registerBlockItem(registry);
        blockBarrel.registerBlockItem(registry);
        blockPostBox.registerBlockItem(registry);
        blockDecorationPlacerholder.registerBlockItem(registry);
    }
}
