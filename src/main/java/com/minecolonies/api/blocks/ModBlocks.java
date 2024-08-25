package com.minecolonies.api.blocks;

import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.*;
import com.minecolonies.core.blocks.decorative.*;
import com.minecolonies.core.blocks.huts.*;
import com.minecolonies.core.blocks.schematic.BlockWaypoint;
import com.minecolonies.core.items.ItemCrop;
import com.minecolonies.core.items.ItemGate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Class to create the modBlocks. References to the blocks can be made here
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings({"squid:ClassVariableVisibilityCheck", "squid:S2444", "squid:S1444", "squid:S1820",})
public final class ModBlocks
{
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(Constants.MOD_ID);

    /*
     * Creating objects for all blocks in the mod.
     * References can be made to here.
     */
    public static final DeferredBlock<BlockHutTownHall> blockHutTownHall = simpleBlockItem("blockhuttownhall", BlockHutTownHall::new);
    public static final DeferredBlock<BlockHutCitizen> blockHutHome = simpleBlockItem("blockhutcitizen", BlockHutCitizen::new);
    public static final DeferredBlock<BlockHutMiner> blockHutMiner = simpleBlockItem("blockhutminer", BlockHutMiner::new);
    public static final DeferredBlock<BlockHutLumberjack> blockHutLumberjack = simpleBlockItem("blockhutlumberjack", BlockHutLumberjack::new);
    public static final DeferredBlock<BlockHutBaker> blockHutBaker = simpleBlockItem("blockhutbaker", BlockHutBaker::new);
    public static final DeferredBlock<BlockHutBuilder> blockHutBuilder = simpleBlockItem("blockhutbuilder", BlockHutBuilder::new);
    public static final DeferredBlock<BlockHutDeliveryman> blockHutDeliveryman = simpleBlockItem("blockhutdeliveryman", BlockHutDeliveryman::new);
    public static final DeferredBlock<BlockHutBlacksmith> blockHutBlacksmith = simpleBlockItem("blockhutblacksmith", BlockHutBlacksmith::new);
    public static final DeferredBlock<BlockHutStonemason> blockHutStonemason = simpleBlockItem("blockhutstonemason", BlockHutStonemason::new);
    public static final DeferredBlock<BlockHutFarmer> blockHutFarmer = simpleBlockItem("blockhutfarmer", BlockHutFarmer::new);
    public static final DeferredBlock<BlockHutFisherman> blockHutFisherman = simpleBlockItem("blockhutfisherman", BlockHutFisherman::new);
    public static final DeferredBlock<BlockHutGuardTower> blockHutGuardTower = simpleBlockItem("blockhutguardtower", BlockHutGuardTower::new);
    public static final DeferredBlock<BlockHutWareHouse> blockHutWareHouse = simpleBlockItem("blockhutwarehouse", BlockHutWareHouse::new);
    public static final DeferredBlock<BlockHutShepherd> blockHutShepherd = simpleBlockItem("blockhutshepherd", BlockHutShepherd::new);
    public static final DeferredBlock<BlockHutCowboy> blockHutCowboy = simpleBlockItem("blockhutcowboy", BlockHutCowboy::new);
    public static final DeferredBlock<BlockHutSwineHerder> blockHutSwineHerder = simpleBlockItem("blockhutswineherder", BlockHutSwineHerder::new);
    public static final DeferredBlock<BlockHutChickenHerder> blockHutChickenHerder = simpleBlockItem("blockhutchickenherder", BlockHutChickenHerder::new);
    public static final DeferredBlock<BlockHutBarracks> blockHutBarracks = simpleBlockItem("blockhutbarracks", BlockHutBarracks::new);
    public static final DeferredBlock<BlockHutBarracksTower> blockHutBarracksTower = simpleBlockItem("blockhutbarrackstower", BlockHutBarracksTower::new);
    public static final DeferredBlock<BlockHutCook> blockHutCook = simpleBlockItem("blockhutcook", BlockHutCook::new);
    public static final DeferredBlock<BlockHutSmeltery> blockHutSmeltery = simpleBlockItem("blockhutsmeltery", BlockHutSmeltery::new);
    public static final DeferredBlock<BlockHutComposter> blockHutComposter = simpleBlockItem("blockhutcomposter", BlockHutComposter::new);
    public static final DeferredBlock<BlockHutLibrary> blockHutLibrary = simpleBlockItem("blockhutlibrary", BlockHutLibrary::new);
    public static final DeferredBlock<BlockHutArchery> blockHutArchery = simpleBlockItem("blockhutarchery", BlockHutArchery::new);
    public static final DeferredBlock<BlockHutCombatAcademy> blockHutCombatAcademy = simpleBlockItem("blockhutcombatacademy", BlockHutCombatAcademy::new);
    public static final DeferredBlock<BlockHutSawmill> blockHutSawmill = simpleBlockItem("blockhutsawmill", BlockHutSawmill::new);
    public static final DeferredBlock<BlockHutStoneSmeltery> blockHutStoneSmeltery = simpleBlockItem("blockhutstonesmeltery", BlockHutStoneSmeltery::new);
    public static final DeferredBlock<BlockHutCrusher> blockHutCrusher = simpleBlockItem("blockhutcrusher", BlockHutCrusher::new);
    public static final DeferredBlock<BlockHutSifter> blockHutSifter = simpleBlockItem("blockhutsifter", BlockHutSifter::new);
    public static final DeferredBlock<BlockPostBox> blockPostBox = simpleBlockItem("blockpostbox", BlockPostBox::new);
    public static final DeferredBlock<BlockHutFlorist> blockHutFlorist = simpleBlockItem("blockhutflorist", BlockHutFlorist::new);
    public static final DeferredBlock<BlockHutEnchanter> blockHutEnchanter = simpleBlockItem("blockhutenchanter", BlockHutEnchanter::new);
    public static final DeferredBlock<BlockHutUniversity> blockHutUniversity = simpleBlockItem("blockhutuniversity", BlockHutUniversity::new);
    public static final DeferredBlock<BlockHutHospital> blockHutHospital = simpleBlockItem("blockhuthospital", BlockHutHospital::new);
    public static final DeferredBlock<BlockStash> blockStash = simpleBlockItem("blockstash", BlockStash::new);
    public static final DeferredBlock<BlockHutSchool> blockHutSchool = simpleBlockItem("blockhutschool", BlockHutSchool::new);
    public static final DeferredBlock<BlockHutGlassblower> blockHutGlassblower = simpleBlockItem("blockhutglassblower", BlockHutGlassblower::new);
    public static final DeferredBlock<BlockHutDyer> blockHutDyer = simpleBlockItem("blockhutdyer", BlockHutDyer::new);
    public static final DeferredBlock<BlockHutFletcher> blockHutFletcher = simpleBlockItem("blockhutfletcher", BlockHutFletcher::new);
    public static final DeferredBlock<BlockHutMechanic> blockHutMechanic = simpleBlockItem("blockhutmechanic", BlockHutMechanic::new);
    public static final DeferredBlock<BlockHutPlantation> blockHutPlantation = simpleBlockItem("blockhutplantation", BlockHutPlantation::new);
    public static final DeferredBlock<BlockHutTavern> blockHutTavern = simpleBlockItem("blockhuttavern", BlockHutTavern::new);
    public static final DeferredBlock<BlockHutRabbitHutch> blockHutRabbitHutch = simpleBlockItem("blockhutrabbithutch", BlockHutRabbitHutch::new);
    public static final DeferredBlock<BlockHutConcreteMixer> blockHutConcreteMixer = simpleBlockItem("blockhutconcretemixer", BlockHutConcreteMixer::new);
    public static final DeferredBlock<BlockHutBeekeeper> blockHutBeekeeper = simpleBlockItem("blockhutbeekeeper", BlockHutBeekeeper::new);
    public static final DeferredBlock<BlockHutMysticalSite> blockHutMysticalSite = simpleBlockItem("blockhutmysticalsite", BlockHutMysticalSite::new);
    public static final DeferredBlock<BlockHutGraveyard> blockHutGraveyard = simpleBlockItem("blockhutgraveyard", BlockHutGraveyard::new);
    public static final DeferredBlock<BlockHutNetherWorker> blockHutNetherWorker = simpleBlockItem("blockhutnetherworker", BlockHutNetherWorker::new);
    public static final DeferredBlock<SimpleQuarry> blockSimpleQuarry = simpleBlockItem(ModBuildings.SIMPLE_QUARRY_ID, SimpleQuarry::new);
    public static final DeferredBlock<MediumQuarry> blockMediumQuarry = simpleBlockItem(ModBuildings.MEDIUM_QUARRY_ID, MediumQuarry::new);
    //public static final DeferredBlock<LargeQuarry> blockLargeQuarry = simpleBlockItem("largequarry", LargeQuarry::new);
    public static final DeferredBlock<BlockHutAlchemist> blockHutAlchemist = simpleBlockItem("blockhutalchemist", BlockHutAlchemist::new);
    public static final DeferredBlock<BlockHutKitchen> blockHutKitchen = simpleBlockItem("blockhutkitchen", BlockHutKitchen::new);

    /**
     * Utility blocks.
     */
    public static final DeferredBlock<BlockConstructionTape> blockConstructionTape = simpleBlockItem("blockconstructiontape", BlockConstructionTape::new);
    public static final DeferredBlock<BlockMinecoloniesRack> blockRack = simpleBlockItem("blockminecoloniesrack", BlockMinecoloniesRack::new);
    public static final DeferredBlock<BlockMinecoloniesGrave> blockGrave = simpleBlockItem("blockminecoloniesgrave", BlockMinecoloniesGrave::new);
    public static final DeferredBlock<BlockMinecoloniesNamedGrave> blockNamedGrave = simpleBlockItem("blockminecoloniesnamedgrave", BlockMinecoloniesNamedGrave::new);
    public static final DeferredBlock<BlockWaypoint> blockWayPoint = simpleBlockItem("blockwaypoint", BlockWaypoint::new);
    public static final DeferredBlock<BlockBarrel> blockBarrel = simpleBlockItem("barrel_block", BlockBarrel::new);
    public static final DeferredBlock<BlockDecorationController> blockDecorationPlaceholder = simpleBlockItem("decorationcontroller", BlockDecorationController::new);
    public static final DeferredBlock<BlockScarecrow> blockScarecrow = simpleBlockItem("blockhutfield", BlockScarecrow::new);
    public static final DeferredBlock<BlockPlantationField> blockPlantationField = simpleBlockItem("blockhutplantationfield", BlockPlantationField::new);
    public static final DeferredBlock<BlockCompostedDirt> blockCompostedDirt = simpleBlockItem("composted_dirt", BlockCompostedDirt::new);
    public static final DeferredBlock<MinecoloniesFarmland> farmland = simpleBlockItem("farmland", () -> new MinecoloniesFarmland(false, 15.0));
    public static final DeferredBlock<MinecoloniesFarmland> floodedFarmland = simpleBlockItem("floodedfarmland", () -> new MinecoloniesFarmland(true, 13.0));

    public static final DeferredBlock<BlockGate> blockIronGate = customBlockItem("gate_iron", () -> new BlockGate(5f, 6, 8), ItemGate::new);
    public static final DeferredBlock<BlockGate> blockWoodenGate = customBlockItem("gate_wood", () -> new BlockGate(4f, 6, 5), ItemGate::new);

    /**
     * Items in ModItems
     */
    public static final DeferredBlock<BlockColonyFlagBanner> blockColonyBanner = simple("colony_banner", BlockColonyFlagBanner::new);
    public static final DeferredBlock<BlockColonyFlagWallBanner> blockColonyWallBanner = simple("colony_wall_banner", BlockColonyFlagWallBanner::new);

    public static final DeferredBlock<MinecoloniesCropBlock> blockBellPepper = cropBlock("bell_pepper", ModBlocks.farmland, ModTags.temperateBiomes);
    public static final DeferredBlock<MinecoloniesCropBlock> blockCabbage = cropBlock("cabbage", ModBlocks.farmland, ModTags.coldBiomes);
    public static final DeferredBlock<MinecoloniesCropBlock> blockChickpea = cropBlock("chickpea", ModBlocks.farmland, ModTags.dryBiomes);
    public static final DeferredBlock<MinecoloniesCropBlock> blockDurum = cropBlock("durum", ModBlocks.farmland, null);
    public static final DeferredBlock<MinecoloniesCropBlock> blockEggplant = cropBlock("eggplant", ModBlocks.farmland, null);
    public static final DeferredBlock<MinecoloniesCropBlock> blockGarlic = cropBlock("garlic", ModBlocks.farmland, null);
    public static final DeferredBlock<MinecoloniesCropBlock> blockOnion = cropBlock("onion", ModBlocks.farmland, null);
    public static final DeferredBlock<MinecoloniesCropBlock> blockSoyBean = cropBlock("soybean", ModBlocks.farmland, ModTags.humidBiomes);
    public static final DeferredBlock<MinecoloniesCropBlock> blockTomato = cropBlock("tomato", ModBlocks.farmland, ModTags.temperateBiomes);
    public static final DeferredBlock<MinecoloniesCropBlock> blockRice = cropBlock("rice", ModBlocks.floodedFarmland, ModTags.humidBiomes);

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {

    }

    @NotNull
    public static List<DeferredBlock<? extends AbstractBlockHut<?>>> getHuts()
    {
        return List.of(
          blockHutTownHall,
          blockHutHome,
          blockHutTavern,
          blockHutBuilder,
          blockHutLumberjack,
          blockHutWareHouse,
          blockHutStoneSmeltery,
          blockHutStonemason,
          blockHutGuardTower,
          blockHutArchery,
          blockHutBaker,
          blockHutBarracks,
          blockHutBarracksTower,
          blockHutBlacksmith,
          blockHutChickenHerder,
          blockHutCombatAcademy,
          blockHutComposter,
          blockHutCook,
          blockHutCowboy,
          blockHutCrusher,
          blockHutDeliveryman,
          blockHutFarmer,
          blockHutFisherman,
          blockHutLibrary,
          blockHutMiner,
          blockHutSawmill,
          blockHutSifter,
          blockHutShepherd,
          blockHutSmeltery,
          blockHutSwineHerder,
          blockHutUniversity,
          blockHutHospital,
          blockHutSchool,
          blockHutEnchanter,
          blockHutGlassblower,
          blockHutDyer,
          blockHutFletcher,
          blockHutMechanic,
          blockHutPlantation,
          blockHutRabbitHutch,
          blockHutConcreteMixer,
          blockHutBeekeeper,
          blockHutMysticalSite,
          blockHutFlorist,
          blockPostBox,
          blockStash,
          blockHutGraveyard,
          blockHutNetherWorker,
          blockHutAlchemist,
          blockHutKitchen,
          blockSimpleQuarry,
          blockMediumQuarry
          //blockLargeQuarry
        );
    }

    @NotNull
    public static List<DeferredBlock<MinecoloniesCropBlock>> getCrops()
    {
        return List.of(
        blockBellPepper,
        blockCabbage,
        blockChickpea,
        blockDurum,
        blockEggplant,
        blockGarlic,
        blockOnion,
        blockSoyBean,
        blockTomato,
        blockRice
        );
    }

    private static <B extends Block> DeferredBlock<B> simple(final String name, final Supplier<B> block)
    {
        return REGISTRY.register(name, block);
    }

    private static <B extends Block> DeferredBlock<B> simpleBlockItem(final String name, final Supplier<B> block)
    {
        final DeferredBlock<B> registered = REGISTRY.register(name, block);
        ModItems.REGISTRY.registerSimpleBlockItem(registered);
        return registered;
    }

    /**
     * @implNote inlined version of {@link #simpleBlockItem(String, Supplier)}
     */
    private static <B extends Block> DeferredBlock<B> customBlockItem(final String name,
        final Supplier<B> block,
        final BiFunction<B, Item.Properties, ? extends BlockItem> blockItemFactory)
    {
        final DeferredBlock<B> registered = REGISTRY.register(name, block);
        ModItems.REGISTRY.register(registered.unwrapKey().orElseThrow().location().getPath(),
            key -> blockItemFactory.apply(registered.get(), new Item.Properties()));
        return registered;
    }

    private static DeferredBlock<MinecoloniesCropBlock> cropBlock(final String name, final DeferredBlock<?> preferredFarmland, @Nullable final TagKey<Biome> preferredBiome)
    {
        return customBlockItem(name, () -> new MinecoloniesCropBlock(preferredFarmland, preferredBiome), (b, prop) -> new ItemCrop(b, prop, preferredBiome));
    }
}
