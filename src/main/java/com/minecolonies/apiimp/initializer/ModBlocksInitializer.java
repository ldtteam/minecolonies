package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.*;
import com.minecolonies.coremod.blocks.decorative.BlockConstructionTape;
import com.minecolonies.coremod.blocks.huts.*;
import com.minecolonies.coremod.blocks.schematic.BlockWaypoint;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * This class deals with the initialization of blocks and their items.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBlocksInitializer
{

    private ModBlocksInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModBlockInitializer but this is a Utility class.");
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        ModBlocksInitializer.init(event.getRegistry());
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
        ModBlocks.blockHutUniversity = new BlockHutUniversity().registerBlock(registry);
        ModBlocks.blockHutHospital = new BlockHutHospital().registerBlock(registry);
        ModBlocks.blockHutSchool = new BlockHutSchool().registerBlock(registry);

        ModBlocks.blockConstructionTape = new BlockConstructionTape().registerBlock(registry);
        ModBlocks.blockBarracksTowerSubstitution = new BlockBarracksTowerSubstitution().registerBlock(registry);
        ModBlocks.blockRack = new BlockMinecoloniesRack().registerBlock(registry);
        ModBlocks.blockWayPoint = new BlockWaypoint().registerBlock(registry);
        ModBlocks.blockPostBox = new BlockPostBox().registerBlock(registry);
        ModBlocks.blockStash = new BlockStash().registerBlock(registry);
        ModBlocks.blockDecorationPlaceholder = new BlockDecorationController().registerBlock(registry);
        ModBlocks.blockBarrel = new BlockBarrel().registerBlock(registry);
        ModBlocks.blockCompostedDirt = new BlockCompostedDirt().registerBlock(registry);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        ModBlocksInitializer.registerBlockItem(event.getRegistry());
    }

    /**
     * Initializes the registry with the relevant {@link net.minecraft.item.BlockItem} produced by the relevant blocks.
     *
     * @param registry The item registry to add the items too.
     */
    public static void registerBlockItem(final IForgeRegistry<Item> registry)
    {
        final Item.Properties properties = new Item.Properties().group(ModCreativeTabs.MINECOLONIES);
        
        ModBlocks.blockHutBaker.registerBlockItem(registry, properties);
        ModBlocks.blockHutBlacksmith.registerBlockItem(registry, properties);
        ModBlocks.blockHutBuilder.registerBlockItem(registry, properties);
        ModBlocks.blockHutHome.registerBlockItem(registry, properties);
        ModBlocks.blockHutDeliveryman.registerBlockItem(registry, properties);
        ModBlocks.blockHutFarmer.registerBlockItem(registry, properties);
        ModBlocks.blockScarecrow.registerBlockItem(registry, properties);
        ModBlocks.blockHutFisherman.registerBlockItem(registry, properties);
        ModBlocks.blockHutGuardTower.registerBlockItem(registry, properties);
        ModBlocks.blockHutLumberjack.registerBlockItem(registry, properties);
        ModBlocks.blockHutMiner.registerBlockItem(registry, properties);
        ModBlocks.blockHutStonemason.registerBlockItem(registry, properties);
        ModBlocks.blockHutTownHall.registerBlockItem(registry, properties);
        ModBlocks.blockHutWareHouse.registerBlockItem(registry, properties);
        ModBlocks.blockHutShepherd.registerBlockItem(registry, properties);
        ModBlocks.blockHutCowboy.registerBlockItem(registry, properties);
        ModBlocks.blockHutSwineHerder.registerBlockItem(registry, properties);
        ModBlocks.blockHutChickenHerder.registerBlockItem(registry, properties);
        ModBlocks.blockHutBarracksTower.registerBlockItem(registry, properties);
        ModBlocks.blockHutBarracks.registerBlockItem(registry, properties);
        ModBlocks.blockHutCook.registerBlockItem(registry, properties);
        ModBlocks.blockHutSmeltery.registerBlockItem(registry, properties);
        ModBlocks.blockHutComposter.registerBlockItem(registry, properties);
        ModBlocks.blockHutLibrary.registerBlockItem(registry, properties);
        ModBlocks.blockHutArchery.registerBlockItem(registry, properties);
        ModBlocks.blockHutCombatAcademy.registerBlockItem(registry, properties);
        ModBlocks.blockHutSawmill.registerBlockItem(registry, properties);
        ModBlocks.blockHutStoneSmeltery.registerBlockItem(registry, properties);
        ModBlocks.blockHutCrusher.registerBlockItem(registry, properties);
        ModBlocks.blockHutSifter.registerBlockItem(registry, properties);
        ModBlocks.blockHutFlorist.registerBlockItem(registry, properties);
        ModBlocks.blockHutEnchanter.registerBlockItem(registry, properties);
        ModBlocks.blockHutUniversity.registerBlockItem(registry, properties);
        ModBlocks.blockHutHospital.registerBlockItem(registry, properties);
        ModBlocks.blockHutSchool.registerBlockItem(registry, properties);

        ModBlocks.blockConstructionTape.registerBlockItem(registry, properties);
        ModBlocks.blockBarracksTowerSubstitution.registerBlockItem(registry, properties);
        ModBlocks.blockRack.registerBlockItem(registry, properties);
        ModBlocks.blockWayPoint.registerBlockItem(registry, properties);
        ModBlocks.blockBarrel.registerBlockItem(registry, properties);
        ModBlocks.blockPostBox.registerBlockItem(registry, properties);
        ModBlocks.blockStash.registerBlockItem(registry, properties);
        ModBlocks.blockDecorationPlaceholder.registerBlockItem(registry, properties);
        ModBlocks.blockCompostedDirt.registerBlockItem(registry, properties);
    }
}
