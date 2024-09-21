package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.*;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagBanner;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagWallBanner;
import com.minecolonies.core.blocks.decorative.BlockConstructionTape;
import com.minecolonies.core.blocks.decorative.BlockGate;
import com.minecolonies.core.blocks.huts.*;
import com.minecolonies.core.blocks.schematic.BlockWaypoint;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;

import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.IRON_GATE;
import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.WOODEN_GATE;
import static com.minecolonies.core.blocks.MinecoloniesCropBlock.*;
import static com.minecolonies.core.blocks.MinecoloniesFarmland.FARMLAND;
import static com.minecolonies.core.blocks.MinecoloniesFarmland.FLOODED_FARMLAND;

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
    public static void registerBlocks(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS))
        {
            ModBlocksInitializer.init(event.getForgeRegistry());
        }
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
        ModBlocks.blockHutGlassblower = new BlockHutGlassblower().registerBlock(registry);
        ModBlocks.blockHutDyer = new BlockHutDyer().registerBlock(registry);
        ModBlocks.blockHutFletcher = new BlockHutFletcher().registerBlock(registry);
        ModBlocks.blockHutMechanic = new BlockHutMechanic().registerBlock(registry);
        ModBlocks.blockHutTavern = new BlockHutTavern().registerBlock(registry);
        ModBlocks.blockHutPlantation = new BlockHutPlantation().registerBlock(registry);
        ModBlocks.blockPlantationField = new BlockPlantationField().registerBlock(registry);
        ModBlocks.blockHutRabbitHutch = new BlockHutRabbitHutch().registerBlock(registry);
        ModBlocks.blockHutConcreteMixer = new BlockHutConcreteMixer().registerBlock(registry);
        ModBlocks.blockHutBeekeeper = new BlockHutBeekeeper().registerBlock(registry);
        ModBlocks.blockHutMysticalSite = new BlockHutMysticalSite().registerBlock(registry);
        ModBlocks.blockHutGraveyard = new BlockHutGraveyard().registerBlock(registry);
        ModBlocks.blockHutNetherWorker = new BlockHutNetherWorker().registerBlock(registry);
        ModBlocks.blockHutAlchemist = new BlockHutAlchemist().registerBlock(registry);
        ModBlocks.blockHutKitchen = new BlockHutKitchen().registerBlock(registry);

        ModBlocks.blockConstructionTape = new BlockConstructionTape().registerBlock(registry);
        ModBlocks.blockRack = new BlockMinecoloniesRack().registerBlock(registry);
        ModBlocks.blockGrave = new BlockMinecoloniesGrave().registerBlock(registry);
        ModBlocks.blockNamedGrave = new BlockMinecoloniesNamedGrave().registerBlock(registry);
        ModBlocks.blockWayPoint = new BlockWaypoint().registerBlock(registry);
        ModBlocks.blockPostBox = new BlockPostBox().registerBlock(registry);
        ModBlocks.blockStash = new BlockStash().registerBlock(registry);
        ModBlocks.blockDecorationPlaceholder = new BlockDecorationController().registerBlock(registry);
        ModBlocks.blockBarrel = new BlockBarrel().registerBlock(registry);
        ModBlocks.blockCompostedDirt = new BlockCompostedDirt().registerBlock(registry);
        ModBlocks.blockColonyBanner = new BlockColonyFlagBanner().registerBlock(registry);
        ModBlocks.blockColonyWallBanner = new BlockColonyFlagWallBanner().registerBlock(registry);
        ModBlocks.blockIronGate = new BlockGate(IRON_GATE, 5f, 6, 8).registerBlock(registry);
        ModBlocks.blockWoodenGate = new BlockGate(WOODEN_GATE, 4f, 6, 5).registerBlock(registry);
        ModBlocks.farmland = new MinecoloniesFarmland(FARMLAND, false, 15.0).registerBlock(registry);
        ModBlocks.floodedFarmland = new MinecoloniesFarmland(FLOODED_FARMLAND, true, 13.0).registerBlock(registry);

        // Could in the future add alternative versions of these crops that can be planted by the player and grow at a slower rate.
        ModBlocks.blockBellPepper = new MinecoloniesCropBlock(BELL_PEPPER, ModBlocks.farmland, List.of(Blocks.GRASS), ModTags.temperateBiomes).registerBlock(registry);
        ModBlocks.blockCabbage = new MinecoloniesCropBlock(CABBAGE, ModBlocks.farmland, List.of(Blocks.FERN), ModTags.coldBiomes).registerBlock(registry);
        ModBlocks.blockChickpea = new MinecoloniesCropBlock(CHICKPEA, ModBlocks.farmland, List.of(Blocks.GRASS, Blocks.DEAD_BUSH), ModTags.dryBiomes).registerBlock(registry);
        ModBlocks.blockDurum = new MinecoloniesCropBlock(DURUM, ModBlocks.farmland, List.of(Blocks.GRASS), null).registerBlock(registry);
        ModBlocks.blockEggplant = new MinecoloniesCropBlock(EGGPLANT, ModBlocks.farmland, List.of(Blocks.GRASS), null).registerBlock(registry);
        ModBlocks.blockGarlic = new MinecoloniesCropBlock(GARLIC, ModBlocks.farmland, List.of(Blocks.GRASS), null).registerBlock(registry);
        ModBlocks.blockOnion = new MinecoloniesCropBlock(ONION, ModBlocks.farmland, List.of(Blocks.GRASS), null).registerBlock(registry);
        ModBlocks.blockSoyBean = new MinecoloniesCropBlock(SOYBEAN, ModBlocks.farmland, List.of(Blocks.GRASS, Blocks.FERN), ModTags.humidBiomes).registerBlock(registry);
        ModBlocks.blockTomato = new MinecoloniesCropBlock(TOMATO, ModBlocks.farmland, List.of(Blocks.GRASS), ModTags.temperateBiomes).registerBlock(registry);
        ModBlocks.blockRice = new MinecoloniesCropBlock(RICE, ModBlocks.floodedFarmland, List.of(Blocks.SEAGRASS, Blocks.SMALL_DRIPLEAF), ModTags.humidBiomes).registerBlock(registry);

        ModBlocks.blockSimpleQuarry = new SimpleQuarry().registerBlock(registry);
        ModBlocks.blockMediumQuarry = new MediumQuarry().registerBlock(registry);
        //ModBlocks.blockLargeQuarry = new LargeQuarry().registerBlock(registry);
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS))
        {
            ModBlocksInitializer.registerBlockItem(event.getForgeRegistry());
        }
    }

    /**
     * Initializes the registry with the relevant item produced by the relevant blocks.
     *
     * @param registry The item registry to add the items too.
     */
    public static void registerBlockItem(final IForgeRegistry<Item> registry)
    {
        ModBlocks.blockHutBaker.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutBlacksmith.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutBuilder.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutHome.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutDeliveryman.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutFarmer.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockScarecrow.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutFisherman.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutGuardTower.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutLumberjack.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutMiner.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutStonemason.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutTownHall.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutWareHouse.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutShepherd.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutCowboy.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutSwineHerder.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutChickenHerder.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutBarracksTower.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutBarracks.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutCook.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutSmeltery.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutComposter.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutLibrary.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutArchery.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutCombatAcademy.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutSawmill.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutStoneSmeltery.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutCrusher.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutSifter.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutFlorist.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutEnchanter.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutUniversity.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutHospital.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutSchool.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutGlassblower.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutDyer.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutFletcher.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutMechanic.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutTavern.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutPlantation.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockPlantationField.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutRabbitHutch.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutConcreteMixer.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutBeekeeper.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutMysticalSite.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutGraveyard.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutNetherWorker.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutAlchemist.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockHutKitchen.registerBlockItem(registry, new Item.Properties());

        ModBlocks.blockConstructionTape.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockRack.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockGrave.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockNamedGrave.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockWayPoint.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockBarrel.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockPostBox.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockStash.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockDecorationPlaceholder.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockCompostedDirt.registerBlockItem(registry, new Item.Properties());
        ModBlocks.farmland.registerBlockItem(registry, new Item.Properties());
        ModBlocks.floodedFarmland.registerBlockItem(registry, new Item.Properties());

        ModBlocks.blockBellPepper.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockCabbage.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockChickpea.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockDurum.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockEggplant.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockGarlic.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockOnion.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockSoyBean.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockTomato.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockRice.registerBlockItem(registry, new Item.Properties());

        ModBlocks.blockSimpleQuarry.registerBlockItem(registry, new Item.Properties());
        ModBlocks.blockMediumQuarry.registerBlockItem(registry, new Item.Properties());
        //ModBlocks.blockLargeQuarry.registerBlockItem(registry, new Item.Properties());
    }
}
