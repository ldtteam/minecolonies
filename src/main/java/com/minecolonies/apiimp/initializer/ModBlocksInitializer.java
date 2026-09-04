package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.AbstractColonyBlock;
import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.*;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagBanner;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagWallBanner;
import com.minecolonies.core.blocks.decorative.BlockConstructionTape;
import com.minecolonies.core.blocks.decorative.BlockGate;
import com.minecolonies.core.blocks.huts.*;
import com.minecolonies.core.blocks.schematic.BlockWaypoint;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.function.Supplier;

import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.IRON_GATE;
import static com.minecolonies.api.blocks.decorative.AbstractBlockGate.WOODEN_GATE;
import static com.minecolonies.core.blocks.MinecoloniesCropBlock.*;
import static com.minecolonies.core.blocks.MinecoloniesFarmland.FARMLAND;
import static com.minecolonies.core.blocks.MinecoloniesFarmland.FLOODED_FARMLAND;

/**
 * This class deals with the initialization of blocks and their items.
 */
@EventBusSubscriber(modid = Constants.MOD_ID)
public final class ModBlocksInitializer
{

    private ModBlocksInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModBlockInitializer but this is a Utility class.");
    }

    @SubscribeEvent
    public static void registerBlocks(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(Registries.BLOCK))
        {
            ModBlocksInitializer.init(event.getRegistry(Registries.BLOCK));
        }
    }

    /**
     * Initializes {@link ModBlocks} with the block instances.
     *
     * @param registry The registry to register the new blocks.
     */
    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final Registry<Block> registry)
    {
        ModBlocks.blockHutBaker = AbstractColonyBlock.registerColonyBlock(registry, "blockhutbaker", BlockHutBaker::new);
        ModBlocks.blockHutBlacksmith = AbstractColonyBlock.registerColonyBlock(registry, "blockhutblacksmith", BlockHutBlacksmith::new);
        ModBlocks.blockHutBuilder = AbstractColonyBlock.registerColonyBlock(registry, "blockhutbuilder", BlockHutBuilder::new);
        ModBlocks.blockHutHome = AbstractColonyBlock.registerColonyBlock(registry, "blockhutcitizen", BlockHutCitizen::new);
        ModBlocks.blockHutDeliveryman = AbstractColonyBlock.registerColonyBlock(registry, "blockhutdeliveryman", BlockHutDeliveryman::new);
        ModBlocks.blockHutFarmer = AbstractColonyBlock.registerColonyBlock(registry, "blockhutfarmer", BlockHutFarmer::new);
        // BlockScarecrow keeps the historical blockhutfield registry id (the field item,
        // blockstate and quest data all use this id).
        ModBlocks.blockScarecrow = AbstractColonyBlock.registerColonyBlock(registry, "blockhutfield", BlockScarecrow::new);
        ModBlocks.blockHutFisherman = AbstractColonyBlock.registerColonyBlock(registry, "blockhutfisherman", BlockHutFisherman::new);
        ModBlocks.blockHutGuardTower = AbstractColonyBlock.registerColonyBlock(registry, "blockhutguardtower", BlockHutGuardTower::new);
        ModBlocks.blockHutLumberjack = AbstractColonyBlock.registerColonyBlock(registry, "blockhutlumberjack", BlockHutLumberjack::new);
        ModBlocks.blockHutMiner = AbstractColonyBlock.registerColonyBlock(registry, "blockhutminer", BlockHutMiner::new);
        ModBlocks.blockHutStonemason = AbstractColonyBlock.registerColonyBlock(registry, "blockhutstonemason", BlockHutStonemason::new);
        ModBlocks.blockHutTownHall = AbstractColonyBlock.registerColonyBlock(registry, "blockhuttownhall", BlockHutTownHall::new);
        ModBlocks.blockHutWareHouse = AbstractColonyBlock.registerColonyBlock(registry, "blockhutwarehouse", BlockHutWareHouse::new);
        ModBlocks.blockHutShepherd = AbstractColonyBlock.registerColonyBlock(registry, "blockhutshepherd", BlockHutShepherd::new);
        ModBlocks.blockHutCowboy = AbstractColonyBlock.registerColonyBlock(registry, "blockhutcowboy", BlockHutCowboy::new);
        ModBlocks.blockHutSwineHerder = AbstractColonyBlock.registerColonyBlock(registry, "blockhutswineherder", BlockHutSwineHerder::new);
        ModBlocks.blockHutChickenHerder = AbstractColonyBlock.registerColonyBlock(registry, "blockhutchickenherder", BlockHutChickenHerder::new);
        ModBlocks.blockHutBarracks = AbstractColonyBlock.registerColonyBlock(registry, "blockhutbarracks", BlockHutBarracks::new);
        ModBlocks.blockHutBarracksTower = AbstractColonyBlock.registerColonyBlock(registry, "blockhutbarrackstower", BlockHutBarracksTower::new);
        ModBlocks.blockHutCook = AbstractColonyBlock.registerColonyBlock(registry, "blockhutcook", BlockHutCook::new);
        ModBlocks.blockHutSmeltery = AbstractColonyBlock.registerColonyBlock(registry, "blockhutsmeltery", BlockHutSmeltery::new);
        ModBlocks.blockHutComposter = AbstractColonyBlock.registerColonyBlock(registry, "blockhutcomposter", BlockHutComposter::new);
        ModBlocks.blockHutLibrary = AbstractColonyBlock.registerColonyBlock(registry, "blockhutlibrary", BlockHutLibrary::new);
        ModBlocks.blockHutArchery = AbstractColonyBlock.registerColonyBlock(registry, "blockhutarchery", BlockHutArchery::new);
        ModBlocks.blockHutSawmill = AbstractColonyBlock.registerColonyBlock(registry, "blockhutsawmill", BlockHutSawmill::new);
        ModBlocks.blockHutCombatAcademy = AbstractColonyBlock.registerColonyBlock(registry, "blockhutcombatacademy", BlockHutCombatAcademy::new);
        ModBlocks.blockHutStoneSmeltery = AbstractColonyBlock.registerColonyBlock(registry, "blockhutstonesmeltery", BlockHutStoneSmeltery::new);
        ModBlocks.blockHutCrusher = AbstractColonyBlock.registerColonyBlock(registry, "blockhutcrusher", BlockHutCrusher::new);
        ModBlocks.blockHutSifter = AbstractColonyBlock.registerColonyBlock(registry, "blockhutsifter", BlockHutSifter::new);
        ModBlocks.blockHutFlorist = AbstractColonyBlock.registerColonyBlock(registry, "blockhutflorist", BlockHutFlorist::new);
        ModBlocks.blockHutEnchanter = AbstractColonyBlock.registerColonyBlock(registry, "blockhutenchanter", BlockHutEnchanter::new);
        ModBlocks.blockHutUniversity = AbstractColonyBlock.registerColonyBlock(registry, "blockhutuniversity", BlockHutUniversity::new);
        ModBlocks.blockHutHospital = AbstractColonyBlock.registerColonyBlock(registry, "blockhuthospital", BlockHutHospital::new);
        ModBlocks.blockHutSchool = AbstractColonyBlock.registerColonyBlock(registry, "blockhutschool", BlockHutSchool::new);
        ModBlocks.blockHutGlassblower = AbstractColonyBlock.registerColonyBlock(registry, "blockhutglassblower", BlockHutGlassblower::new);
        ModBlocks.blockHutDyer = AbstractColonyBlock.registerColonyBlock(registry, "blockhutdyer", BlockHutDyer::new);
        ModBlocks.blockHutFletcher = AbstractColonyBlock.registerColonyBlock(registry, "blockhutfletcher", BlockHutFletcher::new);
        ModBlocks.blockHutMechanic = AbstractColonyBlock.registerColonyBlock(registry, "blockhutmechanic", BlockHutMechanic::new);
        ModBlocks.blockHutTavern = AbstractColonyBlock.registerColonyBlock(registry, "blockhuttavern", BlockHutTavern::new);
        ModBlocks.blockHutPlantation = AbstractColonyBlock.registerColonyBlock(registry, "blockhutplantation", BlockHutPlantation::new);
        ModBlocks.blockPlantationField = AbstractColonyBlock.registerColonyBlock(registry, "blockhutplantationfield", BlockPlantationField::new);
        ModBlocks.blockHutRabbitHutch = AbstractColonyBlock.registerColonyBlock(registry, "blockhutrabbithutch", BlockHutRabbitHutch::new);
        ModBlocks.blockHutConcreteMixer = AbstractColonyBlock.registerColonyBlock(registry, "blockhutconcretemixer", BlockHutConcreteMixer::new);
        ModBlocks.blockHutBeekeeper = AbstractColonyBlock.registerColonyBlock(registry, "blockhutbeekeeper", BlockHutBeekeeper::new);
        ModBlocks.blockHutMysticalSite = AbstractColonyBlock.registerColonyBlock(registry, "blockhutmysticalsite", BlockHutMysticalSite::new);
        ModBlocks.blockHutGraveyard = AbstractColonyBlock.registerColonyBlock(registry, "blockhutgraveyard", BlockHutGraveyard::new);
        ModBlocks.blockHutNetherWorker = AbstractColonyBlock.registerColonyBlock(registry, "blockhutnetherworker", BlockHutNetherWorker::new);
        ModBlocks.blockHutAlchemist = AbstractColonyBlock.registerColonyBlock(registry, "blockhutalchemist", BlockHutAlchemist::new);
        ModBlocks.blockHutKitchen = AbstractColonyBlock.registerColonyBlock(registry, "blockhutkitchen", BlockHutKitchen::new);
        ModBlocks.blockHutGateHouse = AbstractColonyBlock.registerColonyBlock(registry, "blockhutgatehouse", BlockHutGateHouse::new);
        ModBlocks.blockHutStable = AbstractColonyBlock.registerColonyBlock(registry, "blockhutstable", BlockHutStable::new);

        ModBlocks.blockConstructionTape = AbstractColonyBlock.registerColonyBlock(registry, "blockconstructiontape", BlockConstructionTape::new);
        ModBlocks.blockRack = AbstractColonyBlock.registerColonyBlock(registry, "blockminecoloniesrack", BlockMinecoloniesRack::new);
        ModBlocks.blockGrave = AbstractColonyBlock.registerColonyBlock(registry, "blockminecoloniesgrave", BlockMinecoloniesGrave::new);
        ModBlocks.blockNamedGrave = AbstractColonyBlock.registerColonyBlock(registry, "blockminecoloniesnamedgrave", BlockMinecoloniesNamedGrave::new);
        ModBlocks.blockWayPoint = AbstractColonyBlock.registerColonyBlock(registry, "blockwaypoint", BlockWaypoint::new);
        ModBlocks.blockPostBox = AbstractColonyBlock.registerColonyBlock(registry, "blockpostbox", BlockPostBox::new);
        ModBlocks.blockStash = AbstractColonyBlock.registerColonyBlock(registry, "blockstash", BlockStash::new);
        ModBlocks.blockDecorationPlaceholder = AbstractColonyBlock.registerColonyBlock(registry, "decorationcontroller", BlockDecorationController::new);
        ModBlocks.blockBarrel = AbstractColonyBlock.registerColonyBlock(registry, "barrel_block", BlockBarrel::new);
        ModBlocks.blockCompostedDirt = AbstractColonyBlock.registerColonyBlock(registry, "composted_dirt", BlockCompostedDirt::new);
        ModBlocks.blockColonyBanner = AbstractColonyBlock.registerColonyBlock(registry, "colony_banner", BlockColonyFlagBanner::new);
        ModBlocks.blockColonyWallBanner = AbstractColonyBlock.registerColonyBlock(registry, "colony_wall_banner", BlockColonyFlagWallBanner::new);
        ModBlocks.blockIronGate = registerBlock(registry, IRON_GATE, () -> new BlockGate(IRON_GATE, 10f, 6, 8));
        ModBlocks.blockWoodenGate = registerBlock(registry, WOODEN_GATE, () -> new BlockGate(WOODEN_GATE, 7f, 6, 5));
        ModBlocks.farmland = registerBlock(registry, "farmland", () -> new MinecoloniesFarmland(FARMLAND, false, 15.0));
        ModBlocks.floodedFarmland = registerBlock(registry, FLOODED_FARMLAND, () -> new MinecoloniesFarmland(FLOODED_FARMLAND, true, 13.0));
        ModBlocks.blockColonySign = AbstractColonyBlock.registerColonyBlock(registry, "colonysign", BlockColonySign::new);

        // Could in the future add alternative versions of these crops that can be planted by the player and grow at a slower rate.
        ModBlocks.blockBellPepper = registerBlock(registry, "bell_pepper", () -> new MinecoloniesCropBlock(BELL_PEPPER, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.temperateBiomes));
        ModBlocks.blockCabbage = registerBlock(registry, "cabbage", () -> new MinecoloniesCropBlock(CABBAGE, ModBlocks.farmland, List.of(Blocks.FERN), ModTags.coldBiomes));
        ModBlocks.blockChickpea = registerBlock(registry, "chickpea", () -> new MinecoloniesCropBlock(CHICKPEA, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.DEAD_BUSH), ModTags.dryBiomes));
        ModBlocks.blockDurum = registerBlock(registry, "durum", () -> new MinecoloniesCropBlock(DURUM, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
        ModBlocks.blockEggplant = registerBlock(registry, "eggplant", () -> new MinecoloniesCropBlock(EGGPLANT, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
        ModBlocks.blockGarlic = registerBlock(registry, "garlic", () -> new MinecoloniesCropBlock(GARLIC, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
        ModBlocks.blockOnion = registerBlock(registry, "onion", () -> new MinecoloniesCropBlock(ONION, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
        // MinecoloniesCropBlock derives both its block and item registry id from
        // the crop name.  Keep the registration id aligned with the historical
        // soybean id used by the class and its blockstate/model resources.
        ModBlocks.blockSoyBean = registerBlock(registry, SOYBEAN, () -> new MinecoloniesCropBlock(SOYBEAN, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.FERN), ModTags.humidBiomes));
        ModBlocks.blockTomato = registerBlock(registry, "tomato", () -> new MinecoloniesCropBlock(TOMATO, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.temperateBiomes));
        ModBlocks.blockRice = registerBlock(registry, "rice", () -> new MinecoloniesCropBlock(RICE, ModBlocks.floodedFarmland, List.of(Blocks.SEAGRASS, Blocks.SMALL_DRIPLEAF), ModTags.humidBiomes));

        ModBlocks.blockButternutSquash = registerBlock(registry, "butternut_squash", () -> new MinecoloniesCropBlock(BUTTERNUT_SQUASH, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.coldBiomes));
        ModBlocks.blockCorn = registerBlock(registry, "corn", () -> new MinecoloniesCropBlock(CORN, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.temperateBiomes));
        ModBlocks.blockMint = registerBlock(registry, "mint", () -> new MinecoloniesCropBlock(MINT, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
        ModBlocks.blockNetherPepper = registerBlock(registry, "nether_pepper", () -> new MinecoloniesCropBlock(NETHER_PEPPER, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.dryBiomes));
        ModBlocks.blockPeas = registerBlock(registry, "peas", () -> new MinecoloniesCropBlock(PEAS, ModBlocks.farmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.humidBiomes));

        ModBlocks.blockSimpleQuarry = AbstractColonyBlock.registerColonyBlock(registry, "simplequarry", SimpleQuarry::new);
        ModBlocks.blockMediumQuarry = AbstractColonyBlock.registerColonyBlock(registry, "mediumquarry", MediumQuarry::new);
        //ModBlocks.blockLargeQuarry = AbstractColonyBlock.registerColonyBlock(registry, "largequarry", LargeQuarry::new);
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(Registries.ITEM))
        {
            ModBlocksInitializer.registerBlockItem(event.getRegistry(Registries.ITEM));
        }
    }

    /**
     * Initializes the registry with the relevant item produced by the relevant blocks.
     *
     * @param registry The item registry to add the items too.
     */
    public static void registerBlockItem(final Registry<Item> registry)
    {
        registerBlockItemFor(registry, ModBlocks.blockHutBaker);
        registerBlockItemFor(registry, ModBlocks.blockHutBlacksmith);
        registerBlockItemFor(registry, ModBlocks.blockHutBuilder);
        registerBlockItemFor(registry, ModBlocks.blockHutHome);
        registerBlockItemFor(registry, ModBlocks.blockHutDeliveryman);
        registerBlockItemFor(registry, ModBlocks.blockHutFarmer);
        registerBlockItemFor(registry, ModBlocks.blockScarecrow);
        registerBlockItemFor(registry, ModBlocks.blockHutFisherman);
        registerBlockItemFor(registry, ModBlocks.blockHutGuardTower);
        registerBlockItemFor(registry, ModBlocks.blockHutLumberjack);
        registerBlockItemFor(registry, ModBlocks.blockHutMiner);
        registerBlockItemFor(registry, ModBlocks.blockHutStonemason);
        registerBlockItemFor(registry, ModBlocks.blockHutTownHall);
        registerBlockItemFor(registry, ModBlocks.blockHutWareHouse);
        registerBlockItemFor(registry, ModBlocks.blockHutShepherd);
        registerBlockItemFor(registry, ModBlocks.blockHutCowboy);
        registerBlockItemFor(registry, ModBlocks.blockHutSwineHerder);
        registerBlockItemFor(registry, ModBlocks.blockHutChickenHerder);
        registerBlockItemFor(registry, ModBlocks.blockHutBarracksTower);
        registerBlockItemFor(registry, ModBlocks.blockHutBarracks);
        registerBlockItemFor(registry, ModBlocks.blockHutCook);
        registerBlockItemFor(registry, ModBlocks.blockHutSmeltery);
        registerBlockItemFor(registry, ModBlocks.blockHutComposter);
        registerBlockItemFor(registry, ModBlocks.blockHutLibrary);
        registerBlockItemFor(registry, ModBlocks.blockHutArchery);
        registerBlockItemFor(registry, ModBlocks.blockHutCombatAcademy);
        registerBlockItemFor(registry, ModBlocks.blockHutSawmill);
        registerBlockItemFor(registry, ModBlocks.blockHutStoneSmeltery);
        registerBlockItemFor(registry, ModBlocks.blockHutCrusher);
        registerBlockItemFor(registry, ModBlocks.blockHutSifter);
        registerBlockItemFor(registry, ModBlocks.blockHutFlorist);
        registerBlockItemFor(registry, ModBlocks.blockHutEnchanter);
        registerBlockItemFor(registry, ModBlocks.blockHutUniversity);
        registerBlockItemFor(registry, ModBlocks.blockHutHospital);
        registerBlockItemFor(registry, ModBlocks.blockHutSchool);
        registerBlockItemFor(registry, ModBlocks.blockHutGlassblower);
        registerBlockItemFor(registry, ModBlocks.blockHutDyer);
        registerBlockItemFor(registry, ModBlocks.blockHutFletcher);
        registerBlockItemFor(registry, ModBlocks.blockHutMechanic);
        registerBlockItemFor(registry, ModBlocks.blockHutTavern);
        registerBlockItemFor(registry, ModBlocks.blockHutPlantation);
        registerBlockItemFor(registry, ModBlocks.blockPlantationField);
        registerBlockItemFor(registry, ModBlocks.blockHutRabbitHutch);
        registerBlockItemFor(registry, ModBlocks.blockHutConcreteMixer);
        registerBlockItemFor(registry, ModBlocks.blockHutBeekeeper);
        registerBlockItemFor(registry, ModBlocks.blockHutMysticalSite);
        registerBlockItemFor(registry, ModBlocks.blockHutGraveyard);
        registerBlockItemFor(registry, ModBlocks.blockHutNetherWorker);
        registerBlockItemFor(registry, ModBlocks.blockHutAlchemist);
        registerBlockItemFor(registry, ModBlocks.blockHutKitchen);
        registerBlockItemFor(registry, ModBlocks.blockHutGateHouse);
        registerBlockItemFor(registry, ModBlocks.blockHutStable);

        registerBlockItemFor(registry, ModBlocks.blockConstructionTape);
        registerBlockItemFor(registry, ModBlocks.blockRack);
        registerBlockItemFor(registry, ModBlocks.blockGrave);
        registerBlockItemFor(registry, ModBlocks.blockNamedGrave);
        registerBlockItemFor(registry, ModBlocks.blockWayPoint);
        registerBlockItemFor(registry, ModBlocks.blockBarrel);
        registerBlockItemFor(registry, ModBlocks.blockPostBox);
        registerBlockItemFor(registry, ModBlocks.blockStash);
        registerBlockItemFor(registry, ModBlocks.blockDecorationPlaceholder);
        registerBlockItemFor(registry, ModBlocks.blockCompostedDirt);
        registerBlockItemFor(registry, ModBlocks.farmland);
        registerBlockItemFor(registry, ModBlocks.floodedFarmland);
        registerBlockItemFor(registry, ModBlocks.blockColonySign);

        registerBlockItemFor(registry, ModBlocks.blockBellPepper);
        registerBlockItemFor(registry, ModBlocks.blockCabbage);
        registerBlockItemFor(registry, ModBlocks.blockChickpea);
        registerBlockItemFor(registry, ModBlocks.blockDurum);
        registerBlockItemFor(registry, ModBlocks.blockEggplant);
        registerBlockItemFor(registry, ModBlocks.blockGarlic);
        registerBlockItemFor(registry, ModBlocks.blockOnion);
        registerBlockItemFor(registry, ModBlocks.blockSoyBean);
        registerBlockItemFor(registry, ModBlocks.blockTomato);
        registerBlockItemFor(registry, ModBlocks.blockRice);
        registerBlockItemFor(registry, ModBlocks.blockButternutSquash);
        registerBlockItemFor(registry, ModBlocks.blockCorn);
        registerBlockItemFor(registry, ModBlocks.blockMint);
        registerBlockItemFor(registry, ModBlocks.blockNetherPepper);
        registerBlockItemFor(registry, ModBlocks.blockPeas);

        registerBlockItemFor(registry, ModBlocks.blockSimpleQuarry);
        registerBlockItemFor(registry, ModBlocks.blockMediumQuarry);
        //registerBlockItemFor(registry, ModBlocks.blockLargeQuarry);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerBlockItemFor(final Registry<Item> registry, final IBlockMinecolonies block)
    {
        final Block blockAsBlock = (Block) block;
        block.registerBlockItem(registry, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(blockAsBlock))));
    }

    private static <B extends Block> B registerBlock(final Registry<Block> registry, final String name, final Supplier<B> factory)
    {
        final ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        AbstractBlockMinecolonies.beginRegistration(key);
        try
        {
            final B block = factory.get();
            Registry.register(registry, key, block);
            return block;
        }
        finally
        {
            AbstractBlockMinecolonies.endRegistration();
        }
    }

    private static Item.Properties itemProperties(final String name)
    {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name)));
    }
}
