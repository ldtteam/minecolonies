package com.minecolonies.api.blocks;

import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.blocks.*;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagBanner;
import com.minecolonies.core.blocks.decorative.BlockColonyFlagWallBanner;
import com.minecolonies.core.blocks.decorative.BlockConstructionTape;
import com.minecolonies.core.blocks.decorative.BlockGate;
import com.minecolonies.core.blocks.huts.*;
import com.minecolonies.core.blocks.schematic.BlockWaypoint;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.minecolonies.api.blocks.AbstractBlockHut.DEFAULT_HUT_BLOCK_PROPERTIES;
import static com.minecolonies.core.blocks.BlockMinecoloniesCrop.DEFAULT_CROP_PROPERTIES;

/**
 * Class to create the modBlocks. References to the blocks can be made here
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
public final class ModBlocks
{
    public static final DeferredRegister.Blocks DEFERRED_REGISTER = DeferredRegister.createBlocks(Constants.MOD_ID);

    /**
     * The list of hut blocks.
     */
    public static final List<DeferredBlock<AbstractBlockHut>> HUTS = new ArrayList<>();

    /**
     * The list of crops.
     */
    public static final List<DeferredBlock<BlockMinecoloniesCrop>> CROPS = new ArrayList<>();

    /**
     * Building blocks.
     */
    public static final DeferredBlock<AbstractBlockHut> blockHutTownHall      = registerBlockHut("blockhuttownhall", BlockHutTownHall::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutCitizen       = registerBlockHut("blockhutcitizen", BlockHutCitizen::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutMiner         = registerBlockHut("blockhutminer", BlockHutMiner::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutLumberjack    = registerBlockHut("blockhutlumberjack", BlockHutLumberjack::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutBaker         = registerBlockHut("blockhutbaker", BlockHutBaker::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutBuilder       = registerBlockHut("blockhutbuilder", BlockHutBuilder::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutDeliveryman   = registerBlockHut("blockhutdeliveryman", BlockHutDeliveryman::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutBlacksmith    = registerBlockHut("blockhutblacksmith", BlockHutBlacksmith::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutStonemason    = registerBlockHut("blockhutstonemason", BlockHutStonemason::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutFarmer        = registerBlockHut("blockhutfarmer", BlockHutFarmer::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutFisherman     = registerBlockHut("blockhutfisherman", BlockHutFisherman::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutGuardTower    = registerBlockHut("blockhutguardtower", BlockHutGuardTower::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutWareHouse     = registerBlockHut("blockhutwarehouse", BlockHutWareHouse::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutShepherd      = registerBlockHut("blockhutshepherd", BlockHutShepherd::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutCowboy        = registerBlockHut("blockhutcowboy", BlockHutCowboy::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutSwineHerder   = registerBlockHut("blockhutswineherder", BlockHutSwineHerder::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutChickenHerder = registerBlockHut("blockhutchickenherder", BlockHutChickenHerder::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutBarracks      = registerBlockHut("blockhutbarracks", BlockHutBarracks::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutBarracksTower = registerBlockHut("blockhutbarrackstower", BlockHutBarracksTower::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutCook          = registerBlockHut("blockhutcook", BlockHutCook::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutSmeltery      = registerBlockHut("blockhutsmeltery", BlockHutSmeltery::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutComposter     = registerBlockHut("blockhutcomposter", BlockHutComposter::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutLibrary       = registerBlockHut("blockhutlibrary", BlockHutLibrary::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutArchery       = registerBlockHut("blockhutarchery", BlockHutArchery::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutCombatAcademy = registerBlockHut("blockhutcombatacademy", BlockHutCombatAcademy::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutSawmill       = registerBlockHut("blockhutsawmill", BlockHutSawmill::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutStoneSmeltery = registerBlockHut("blockhutstonesmeltery", BlockHutStoneSmeltery::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutCrusher       = registerBlockHut("blockhutcrusher", BlockHutCrusher::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutSifter        = registerBlockHut("blockhutsifter", BlockHutSifter::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutFlorist       = registerBlockHut("blockhutflorist", BlockHutFlorist::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutEnchanter     = registerBlockHut("blockhutenchanter", BlockHutEnchanter::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutUniversity    = registerBlockHut("blockhutuniversity", BlockHutUniversity::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutHospital      = registerBlockHut("blockhuthospital", BlockHutHospital::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutSchool        = registerBlockHut("blockhutschool", BlockHutSchool::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutGlassblower   = registerBlockHut("blockhutglassblower", BlockHutGlassblower::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutDyer          = registerBlockHut("blockhutdyer", BlockHutDyer::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutFletcher      = registerBlockHut("blockhutfletcher", BlockHutFletcher::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutMechanic      = registerBlockHut("blockhutmechanic", BlockHutMechanic::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutPlantation    = registerBlockHut("blockhutplantation", BlockHutPlantation::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutTavern        = registerBlockHut("blockhuttavern", BlockHutTavern::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutRabbitHutch   = registerBlockHut("blockhutrabbithutch", BlockHutRabbitHutch::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutConcreteMixer = registerBlockHut("blockhutconcretemixer", BlockHutConcreteMixer::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutBeekeeper     = registerBlockHut("blockhutbeekeeper", BlockHutBeekeeper::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutMysticalSite  = registerBlockHut("blockhutmysticalsite", BlockHutMysticalSite::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutGraveyard     = registerBlockHut("blockhutgraveyard", BlockHutGraveyard::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutNetherWorker  = registerBlockHut("blockhutnetherworker", BlockHutNetherWorker::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutSimpleQuarry  = registerBlockHut(ModBuildings.SIMPLE_QUARRY_ID, BlockHutSimpleQuarry::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutMediumQuarry  = registerBlockHut(ModBuildings.MEDIUM_QUARRY_ID, BlockHutMediumQuarry::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    //public static final DeferredBlock<AbstractBlockHut> blockHutLargeQuarry      = registerBlockHut(ModBuildings.LARGE_QUARRY_ID, BlockHutLargeQuarry::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutAlchemist     = registerBlockHut("blockhutalchemist", BlockHutAlchemist::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutKitchen       = registerBlockHut("blockhutkitchen", BlockHutKitchen::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractBlockHut> blockHutGateHouse     = registerBlockHut("blockhutgatehouse", BlockHutGateHouse::new, DEFAULT_HUT_BLOCK_PROPERTIES);

    /**
     * Postbox & Stash.
     */
    public static final DeferredBlock<AbstractColonyBlock> blockPostBox = registerColonyBlock("blockpostbox", BlockPostBox::new, DEFAULT_HUT_BLOCK_PROPERTIES);
    public static final DeferredBlock<AbstractColonyBlock> blockStash   = registerColonyBlock("blockstash", BlockStash::new, DEFAULT_HUT_BLOCK_PROPERTIES);

    /**
     * Utility blocks.
     */
    public static final DeferredBlock<BlockConstructionTape> blockConstructionTape = registerBlock("blockconstructiontape",
        BlockConstructionTape::new,
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .sound(SoundType.WOOD)
            .replaceable()
            .pushReaction(PushReaction.DESTROY)
            .isRedstoneConductor((state, getter, pos) -> false)
            .forceSolidOff()
            .strength(0.0f)
            .noCollission()
            .noLootTable());

    public static final DeferredBlock<BlockMinecoloniesRack> blockRack = registerBlock("blockminecoloniesrack",
        BlockMinecoloniesRack::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(10, Float.POSITIVE_INFINITY));

    public static final DeferredBlock<BlockMinecoloniesGrave> blockGrave =
        registerBlock("blockminecoloniesgrave", BlockMinecoloniesGrave::new, BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(1.5f, 5));

    public static final DeferredBlock<BlockMinecoloniesNamedGrave> blockNamedGrave = registerBlock("blockminecoloniesnamedgrave",
        BlockMinecoloniesNamedGrave::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(5, 1));

    public static final DeferredBlock<BlockWaypoint> blockWayPoint =
        registerBlock("blockwaypoint", BlockWaypoint::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(0, 1).noCollission());

    public static final DeferredBlock<BlockBarrel> blockBarrel =
        registerBlock("barrel_block", BlockBarrel::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(5, 1));

    public static final DeferredBlock<BlockDecorationController> blockDecorationPlaceholder = registerBlock("decorationcontroller",
        BlockDecorationController::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(5, 1).noCollission());

    public static final DeferredBlock<BlockScarecrow> blockScarecrow =
        registerBlock("blockhutfield", BlockScarecrow::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(5, 1));

    public static final DeferredBlock<BlockPlantationField> blockPlantationField =
        registerBlock("blockhutplantationfield", BlockPlantationField::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(5, 1));

    public static final DeferredBlock<BlockCompostedDirt> blockCompostedDirt = registerBlock("composted_dirt",
        BlockCompostedDirt::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).sound(SoundType.ROOTED_DIRT).strength(5, 1).sound(SoundType.GRAVEL));

    public static final DeferredBlock<BlockColonyFlagBanner> blockColonyBanner = registerBlock("colony_banner",
        BlockColonyFlagBanner::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).sound(SoundType.WOOL).noCollission().strength(1F).sound(SoundType.WOOL));

    public static final DeferredBlock<BlockColonyFlagWallBanner> blockColonyWallBanner = registerBlock("colony_wall_banner",
        BlockColonyFlagWallBanner::new,
        BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).sound(SoundType.WOOL).noCollission().strength(1F).sound(SoundType.WOOL));

    public static final DeferredBlock<BlockGate> blockIronGate =
        registerBlock("gate_iron", p -> new BlockGate(p, 10f, 6, 8), BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).noOcclusion());

    public static final DeferredBlock<BlockGate> blockWoodGate =
        registerBlock("gate_wood", p -> new BlockGate(p, 7f, 6, 5), BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<BlockMinecoloniesFarmland> blockFarmland = registerBlock("farmland",
        p -> new BlockMinecoloniesFarmland(p, false, 15.0),
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.DIRT)
            .randomTicks()
            .strength(0.6F)
            .sound(SoundType.GRAVEL)
            .isViewBlocking((s, g, p) -> true)
            .isSuffocating((s, g, p) -> true));

    public static final DeferredBlock<BlockMinecoloniesFarmland> blockFloodedFarmland = registerBlock("floodedfarmland",
        p -> new BlockMinecoloniesFarmland(p, true, 13.0),
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.DIRT)
            .randomTicks()
            .strength(0.6F)
            .sound(SoundType.GRAVEL)
            .isViewBlocking((s, g, p) -> true)
            .isSuffocating((s, g, p) -> true));

    public static final DeferredBlock<BlockColonySign> blockColonySign =
        registerBlock("colonysign", BlockColonySign::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(5, 1).noCollission());

    /**
     * Crop blocks.
     */
    public static final DeferredBlock<BlockMinecoloniesCrop> blockBellPepper = registerCrop("bell_pepper",
        (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.temperateBiomes),
        DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockCabbage    =
        registerCrop("cabbage", (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.FERN), ModTags.coldBiomes), DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockChickpea = registerCrop("chickpea",
        (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.DEAD_BUSH), ModTags.dryBiomes),
        DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockDurum    =
        registerCrop("durum", (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null), DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockEggplant =
        registerCrop("eggplant", (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null), DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockGarlic =
        registerCrop("garlic", (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null), DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockOnion =
        registerCrop("onion", (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null), DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockSoyBean         = registerCrop("soybean",
        (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.FERN), ModTags.humidBiomes),
        DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockTomato          = registerCrop("tomato",
        (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.temperateBiomes),
        DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockButternutSquash = registerCrop("butternut_squash",
        (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.coldBiomes),
        DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockCorn            = registerCrop("corn",
        (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.temperateBiomes),
        DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockMint            =
        registerCrop("mint", (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null), DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockNetherPepper = registerCrop("nether_pepper",
        (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.dryBiomes),
        DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockPeas         = registerCrop("peas",
        (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.humidBiomes),
        DEFAULT_CROP_PROPERTIES);
    public static final DeferredBlock<BlockMinecoloniesCrop> blockRice         = registerCrop("rice",
        (p) -> new BlockMinecoloniesCrop(p, ModBlocks.blockFloodedFarmland, List.of(Blocks.SEAGRASS, Blocks.SMALL_DRIPLEAF), ModTags.humidBiomes),
        DEFAULT_CROP_PROPERTIES);

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {
    }

    private static DeferredBlock<AbstractBlockHut> registerBlockHut(final String id, final Function<BlockBehaviour.Properties, AbstractBlockHut> blockBuilder, final BlockBehaviour.Properties properties)
    {
        final DeferredBlock<AbstractBlockHut> block = registerColonyBlock(id, blockBuilder, properties);
        HUTS.add(block);
        return block;
    }

    private static <T extends AbstractColonyBlock> DeferredBlock<T> registerColonyBlock(final String id, final Function<BlockBehaviour.Properties, T> blockBuilder, final BlockBehaviour.Properties properties)
    {
        return registerBlock(id, blockBuilder, properties);
    }

    private static DeferredBlock<BlockMinecoloniesCrop> registerCrop(final String id, final Function<BlockBehaviour.Properties, BlockMinecoloniesCrop> blockBuilder, final BlockBehaviour.Properties properties)
    {
        final DeferredBlock<BlockMinecoloniesCrop> block = registerBlock(id, blockBuilder, properties);
        CROPS.add(block);
        return block;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(final String id, final Function<BlockBehaviour.Properties, T> blockBuilder, final BlockBehaviour.Properties properties)
    {
        return DEFERRED_REGISTER.registerBlock(id, blockBuilder, properties);
    }
}
