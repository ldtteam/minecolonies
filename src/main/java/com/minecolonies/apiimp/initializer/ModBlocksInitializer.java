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
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final IForgeRegistry<Block> registry)
    {
        ModBlocks.blockHutBaker = new BlockHutBaker().registerBlock(registry);
        ModBlocks.blockHutBlacksmith = new BlockHutBlacksmith().registerBlock(registry);
        ModBlocks.blockHutBuilder = new BlockHutBuilder().registerBlock(registry);
        ModBlocks.blockHutHome = new BlockHutCitizen().registerBlock(registry);
        ModBlocks.blockHutDeliveryman = new BlockHutDeliveryman().registerBlock(registry);
        ModBlocks.blockHutFarmer = new BlockHutFarmer().registerBlock(registry);
        ModBlocks.blockScarecrow = new BlockScarecrow().registerBlock(registry);
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
        ModBlocks.blockHutFlorist = new BlockHutFlorist().registerBlock(registry);
        ModBlocks.blockHutEnchanter = new BlockHutEnchanter().registerBlock(registry);

        ModBlocks.blockInfoPoster = new BlockInfoPoster().registerBlock(registry);
        ModBlocks.blockConstructionTape = new BlockConstructionTape().registerBlock(registry);
        ModBlocks.blockBarracksTowerSubstitution = new BlockBarracksTowerSubstitution().registerBlock(registry);
        ModBlocks.blockRack = new BlockMinecoloniesRack().registerBlock(registry);
        ModBlocks.blockWayPoint = new BlockWaypoint().registerBlock(registry);
        ModBlocks.blockPostBox = new BlockPostBox().registerBlock(registry);
        ModBlocks.blockDecorationPlaceholder = new BlockDecorationController(Material.WOOD).registerBlock(registry);
        ModBlocks.blockBarrel = new BlockBarrel().registerBlock(registry);
        ModBlocks.blockCompostedDirt = new BlockCompostedDirt().registerBlock(registry);
    }

    /**
     * Initializes the registry with the relevant {@link net.minecraft.item.ItemBlock} produced by the relevant blocks.
     *
     * @param registry The item registry to add the items too.
     */
    public static void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        ModBlocks.blockHutBaker.registerItemBlock(registry);
        ModBlocks.blockHutBlacksmith.registerItemBlock(registry);
        ModBlocks.blockHutBuilder.registerItemBlock(registry);
        ModBlocks.blockHutHome.registerItemBlock(registry);
        ModBlocks.blockHutDeliveryman.registerItemBlock(registry);
        ModBlocks.blockHutFarmer.registerItemBlock(registry);
        ModBlocks.blockScarecrow.registerItemBlock(registry);
        ModBlocks.blockHutFisherman.registerItemBlock(registry);
        ModBlocks.blockHutGuardTower.registerItemBlock(registry);
        ModBlocks.blockHutLumberjack.registerItemBlock(registry);
        ModBlocks.blockHutMiner.registerItemBlock(registry);
        ModBlocks.blockHutStonemason.registerItemBlock(registry);
        ModBlocks.blockHutTownHall.registerItemBlock(registry);
        ModBlocks.blockHutWareHouse.registerItemBlock(registry);
        ModBlocks.blockHutShepherd.registerItemBlock(registry);
        ModBlocks.blockHutCowboy.registerItemBlock(registry);
        ModBlocks.blockHutSwineHerder.registerItemBlock(registry);
        ModBlocks.blockHutChickenHerder.registerItemBlock(registry);
        ModBlocks.blockHutBarracksTower.registerItemBlock(registry);
        ModBlocks.blockHutBarracks.registerItemBlock(registry);
        ModBlocks.blockHutCook.registerItemBlock(registry);
        ModBlocks.blockHutSmeltery.registerItemBlock(registry);
        ModBlocks.blockHutComposter.registerItemBlock(registry);
        ModBlocks.blockHutLibrary.registerItemBlock(registry);
        ModBlocks.blockHutArchery.registerItemBlock(registry);
        ModBlocks.blockHutCombatAcademy.registerItemBlock(registry);
        ModBlocks.blockHutSawmill.registerItemBlock(registry);
        ModBlocks.blockHutStoneSmeltery.registerItemBlock(registry);
        ModBlocks.blockHutCrusher.registerItemBlock(registry);
        ModBlocks.blockHutSifter.registerItemBlock(registry);
        ModBlocks.blockHutFlorist.registerItemBlock(registry);
        ModBlocks.blockHutEnchanter.registerItemBlock(registry);

        ModBlocks.blockConstructionTape.registerItemBlock(registry);
        ModBlocks.blockBarracksTowerSubstitution.registerItemBlock(registry);
        ModBlocks.blockRack.registerItemBlock(registry);
        ModBlocks.blockWayPoint.registerItemBlock(registry);
        ModBlocks.blockInfoPoster.registerItemBlock(registry);
        ModBlocks.blockBarrel.registerItemBlock(registry);
        ModBlocks.blockPostBox.registerItemBlock(registry);
        ModBlocks.blockDecorationPlaceholder.registerItemBlock(registry);
        ModBlocks.blockCompostedDirt.registerItemBlock(registry);
    }
}
