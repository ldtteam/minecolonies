package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.blocks.decorative.BlockConstructionTape;
import com.minecolonies.coremod.blocks.huts.*;
import com.minecolonies.coremod.blocks.schematic.BlockWaypoint;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * This class deals with the initialization of blocks and their items.
 */
public final class ModBlocksInitializer
{

    private ModBlocksInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModBlockInitializer but this is a Utility class.");
    }

    /**
     * Initializes {@link ModBlocks} with the block instances.
     *
     * @param registry The registry to register the new blocks.
     */
    public static void init(final IForgeRegistry<Block> registry)
    {
        ModBlocks.blockHutBaker = new BlockHutBaker().registerBlock(registry);
        ModBlocks.blockHutBlacksmith = new BlockHutBlacksmith().registerBlock(registry);
        ModBlocks.blockHutBuilder = new BlockHutBuilder().registerBlock(registry);
        ModBlocks.blockHutCitizen = new BlockHutCitizen().registerBlock(registry);
        ModBlocks.blockHutDeliveryman = new BlockHutDeliveryman().registerBlock(registry);
        ModBlocks.blockHutFarmer = new BlockHutFarmer().registerBlock(registry);
        ModBlocks.blockHutField = new BlockHutField().registerBlock(registry);
        ModBlocks.blockHutFisherman = new BlockHutFisherman().registerBlock(registry);
        ModBlocks.blockHutGuardTower = new BlockHutGuardTower().registerBlock(registry);
        ModBlocks.blockHutLumberjack = new BlockHutLumberjack().registerBlock(registry);
        ModBlocks.blockHutMiner = new BlockHutMiner().registerBlock(registry);
        ModBlocks.blockHutStonemason = new BlockHutStonemason().registerBlock(registry);
        ModBlocks.blockHutTownHall = new BlockHutTownHall().registerBlock(registry);
        ModBlocks.blockHutWareHouse = new BlockHutWareHouse().registerBlock(registry);
        ModBlocks.blockHutShepherd = new BlockHutShepherd().registerBlock(registry);
        ModBlocks.blockHutCowboy = new BlockHutCowboy().registerBlock(registry);
        ModBlocks.blockHutSwineHerder = new BlockHutSwineHerder().registerBlock(registry);
        ModBlocks.blockHutChickenHerder = new BlockHutChickenHerder().registerBlock(registry);
        ModBlocks.blockHutBarracks = new BlockHutBarracks().registerBlock(registry);
        ModBlocks.blockHutBarracksTower = new BlockHutBarracksTower().registerBlock(registry);
        ModBlocks.blockHutCook = new BlockHutCook().registerBlock(registry);
        ModBlocks.blockHutSmeltery = new BlockHutSmeltery().registerBlock(registry);
        ModBlocks.blockHutComposter = new BlockHutComposter().registerBlock(registry);
        ModBlocks.blockHutLibrary = new BlockHutLibrary().registerBlock(registry);
        ModBlocks.blockHutArchery = new BlockHutArchery().registerBlock(registry);
        ModBlocks.blockHutSawmill = new BlockHutSawmill().registerBlock(registry);
        ModBlocks.blockHutCombatAcademy = new BlockHutCombatAcademy().registerBlock(registry);
        ModBlocks.blockHutStoneSmeltery = new BlockHutStoneSmeltery().registerBlock(registry);
        ModBlocks.blockHutCrusher = new BlockHutCrusher().registerBlock(registry);
        ModBlocks.blockHutSifter = new BlockHutSifter().registerBlock(registry);

        ModBlocks.blockInfoPoster = new BlockInfoPoster().registerBlock(registry);
        ModBlocks.blockConstructionTape = new BlockConstructionTape().registerBlock(registry);
        ModBlocks.blockBarracksTowerSubstitution = new BlockBarracksTowerSubstitution().registerBlock(registry);
        ModBlocks.blockRack = new BlockMinecoloniesRack().registerBlock(registry);
        ModBlocks.blockWayPoint = new BlockWaypoint().registerBlock(registry);
        ModBlocks.blockPostBox = new BlockPostBox().registerBlock(registry);
        ModBlocks.blockDecorationPlaceholder = new BlockDecorationController(Material.WOOD).registerBlock(registry);
        ModBlocks.blockBarrel = new BlockBarrel().registerBlock(registry);
    }

    /**
     * Initializes the registry with the relevant {@link net.minecraft.item.BlockItem} produced by the relevant blocks.
     *
     * @param registry The item registry to add the items too.
     */
    public static void registerBlockItem(final IForgeRegistry<Item> registry)
    {
        ModBlocks.blockHutBaker.registerBlockItem(registry);
        ModBlocks.blockHutBlacksmith.registerBlockItem(registry);
        ModBlocks.blockHutBuilder.registerBlockItem(registry);
        ModBlocks.blockHutCitizen.registerBlockItem(registry);
        ModBlocks.blockHutDeliveryman.registerBlockItem(registry);
        ModBlocks.blockHutFarmer.registerBlockItem(registry);
        ModBlocks.blockHutField.registerBlockItem(registry);
        ModBlocks.blockHutFisherman.registerBlockItem(registry);
        ModBlocks.blockHutGuardTower.registerBlockItem(registry);
        ModBlocks.blockHutLumberjack.registerBlockItem(registry);
        ModBlocks.blockHutMiner.registerBlockItem(registry);
        ModBlocks.blockHutStonemason.registerBlockItem(registry);
        ModBlocks.blockHutTownHall.registerBlockItem(registry);
        ModBlocks.blockHutWareHouse.registerBlockItem(registry);
        ModBlocks.blockHutShepherd.registerBlockItem(registry);
        ModBlocks.blockHutCowboy.registerBlockItem(registry);
        ModBlocks.blockHutSwineHerder.registerBlockItem(registry);
        ModBlocks.blockHutChickenHerder.registerBlockItem(registry);
        ModBlocks.blockHutBarracksTower.registerBlockItem(registry);
        ModBlocks.blockHutBarracks.registerBlockItem(registry);
        ModBlocks.blockHutCook.registerBlockItem(registry);
        ModBlocks.blockHutSmeltery.registerBlockItem(registry);
        ModBlocks.blockHutComposter.registerBlockItem(registry);
        ModBlocks.blockHutLibrary.registerBlockItem(registry);
        ModBlocks.blockHutArchery.registerBlockItem(registry);
        ModBlocks.blockHutCombatAcademy.registerBlockItem(registry);
        ModBlocks.blockHutSawmill.registerBlockItem(registry);
        ModBlocks.blockHutStoneSmeltery.registerBlockItem(registry);
        ModBlocks.blockHutCrusher.registerBlockItem(registry);
        ModBlocks.blockHutSifter.registerBlockItem(registry);

        ModBlocks.blockConstructionTape.registerBlockItem(registry);
        ModBlocks.blockBarracksTowerSubstitution.registerBlockItem(registry);
        ModBlocks.blockRack.registerBlockItem(registry);
        ModBlocks.blockWayPoint.registerBlockItem(registry);
        ModBlocks.blockInfoPoster.registerBlockItem(registry);
        ModBlocks.blockBarrel.registerBlockItem(registry);
        ModBlocks.blockPostBox.registerBlockItem(registry);
        ModBlocks.blockDecorationPlaceholder.registerBlockItem(registry);
    }
}
