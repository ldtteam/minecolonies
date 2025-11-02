package com.minecolonies.api.blocks;

import com.minecolonies.api.blocks.interfaces.IMinecoloniesBlock;
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
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.minecolonies.core.blocks.BlockMinecoloniesCrop.*;

/**
 * Class to create the modBlocks. References to the blocks can be made here
 * <p>
 * We disabled the following finals since we are neither able to mark the items as final, nor do we want to provide public accessors.
 */
@SuppressWarnings("unused")
public final class ModBlocks
{
    public static final DeferredRegister.Blocks DEFERRED_REGISTER = DeferredRegister.createBlocks(Constants.MOD_ID);

    /**
     * The list of hut blocks.
     */
    public static final List<AbstractColonyBlock<?>> HUTS = new ArrayList<>();

    /**
     * The list of crops.
     */
    public static final List<BlockMinecoloniesCrop> CROPS = new ArrayList<>();

    /**
     * Building blocks.
     */
    public static final AbstractBlockHut blockHutTownHall      = registerBlockHut(new BlockHutTownHall());
    public static final AbstractBlockHut blockHutCitizen       = registerBlockHut(new BlockHutCitizen());
    public static final AbstractBlockHut blockHutMiner         = registerBlockHut(new BlockHutMiner());
    public static final AbstractBlockHut blockHutLumberjack    = registerBlockHut(new BlockHutLumberjack());
    public static final AbstractBlockHut blockHutBaker         = registerBlockHut(new BlockHutBaker());
    public static final AbstractBlockHut blockHutBuilder       = registerBlockHut(new BlockHutBuilder());
    public static final AbstractBlockHut blockHutDeliveryman   = registerBlockHut(new BlockHutDeliveryman());
    public static final AbstractBlockHut blockHutBlacksmith    = registerBlockHut(new BlockHutBlacksmith());
    public static final AbstractBlockHut blockHutStonemason    = registerBlockHut(new BlockHutStonemason());
    public static final AbstractBlockHut blockHutFarmer        = registerBlockHut(new BlockHutFarmer());
    public static final AbstractBlockHut blockHutFisherman     = registerBlockHut(new BlockHutFisherman());
    public static final AbstractBlockHut blockHutGuardTower    = registerBlockHut(new BlockHutGuardTower());
    public static final AbstractBlockHut blockHutWareHouse     = registerBlockHut(new BlockHutWareHouse());
    public static final AbstractBlockHut blockHutShepherd      = registerBlockHut(new BlockHutShepherd());
    public static final AbstractBlockHut blockHutCowboy        = registerBlockHut(new BlockHutCowboy());
    public static final AbstractBlockHut blockHutSwineHerder   = registerBlockHut(new BlockHutSwineHerder());
    public static final AbstractBlockHut blockHutChickenHerder = registerBlockHut(new BlockHutChickenHerder());
    public static final AbstractBlockHut blockHutBarracks      = registerBlockHut(new BlockHutBarracks());
    public static final AbstractBlockHut blockHutBarracksTower = registerBlockHut(new BlockHutBarracksTower());
    public static final AbstractBlockHut blockHutCook          = registerBlockHut(new BlockHutCook());
    public static final AbstractBlockHut blockHutSmeltery      = registerBlockHut(new BlockHutSmeltery());
    public static final AbstractBlockHut blockHutComposter     = registerBlockHut(new BlockHutComposter());
    public static final AbstractBlockHut blockHutLibrary       = registerBlockHut(new BlockHutLibrary());
    public static final AbstractBlockHut blockHutArchery       = registerBlockHut(new BlockHutArchery());
    public static final AbstractBlockHut blockHutCombatAcademy = registerBlockHut(new BlockHutCombatAcademy());
    public static final AbstractBlockHut blockHutSawmill       = registerBlockHut(new BlockHutSawmill());
    public static final AbstractBlockHut blockHutStoneSmeltery = registerBlockHut(new BlockHutStoneSmeltery());
    public static final AbstractBlockHut blockHutCrusher       = registerBlockHut(new BlockHutCrusher());
    public static final AbstractBlockHut blockHutSifter        = registerBlockHut(new BlockHutSifter());
    public static final AbstractBlockHut blockHutFlorist       = registerBlockHut(new BlockHutFlorist());
    public static final AbstractBlockHut blockHutEnchanter     = registerBlockHut(new BlockHutEnchanter());
    public static final AbstractBlockHut blockHutUniversity    = registerBlockHut(new BlockHutUniversity());
    public static final AbstractBlockHut blockHutHospital      = registerBlockHut(new BlockHutHospital());
    public static final AbstractBlockHut blockHutSchool        = registerBlockHut(new BlockHutSchool());
    public static final AbstractBlockHut blockHutGlassblower   = registerBlockHut(new BlockHutGlassblower());
    public static final AbstractBlockHut blockHutDyer          = registerBlockHut(new BlockHutDyer());
    public static final AbstractBlockHut blockHutFletcher      = registerBlockHut(new BlockHutFletcher());
    public static final AbstractBlockHut blockHutMechanic      = registerBlockHut(new BlockHutMechanic());
    public static final AbstractBlockHut blockHutPlantation    = registerBlockHut(new BlockHutPlantation());
    public static final AbstractBlockHut blockHutTavern        = registerBlockHut(new BlockHutTavern());
    public static final AbstractBlockHut blockHutRabbitHutch   = registerBlockHut(new BlockHutRabbitHutch());
    public static final AbstractBlockHut blockHutConcreteMixer = registerBlockHut(new BlockHutConcreteMixer());
    public static final AbstractBlockHut blockHutBeekeeper     = registerBlockHut(new BlockHutBeekeeper());
    public static final AbstractBlockHut blockHutMysticalSite  = registerBlockHut(new BlockHutMysticalSite());
    public static final AbstractBlockHut blockHutGraveyard     = registerBlockHut(new BlockHutGraveyard());
    public static final AbstractBlockHut blockHutNetherWorker = registerBlockHut(new BlockHutNetherWorker());
    public static final AbstractBlockHut blockHutSimpleQuarry = registerBlockHut(new BlockHutSimpleQuarry());
    public static final AbstractBlockHut blockHutMediumQuarry = registerBlockHut(new BlockHutMediumQuarry());
    //public static final AbstractBlockHut blockHutLargeQuarry      = registerBlockHut(new BlockHutLargeQuarry());
    public static final AbstractBlockHut blockHutAlchemist    = registerBlockHut(new BlockHutAlchemist());
    public static final AbstractBlockHut blockHutKitchen       = registerBlockHut(new BlockHutKitchen());
    public static final AbstractBlockHut blockHutGateHouse     = registerBlockHut(new BlockHutGateHouse());

    /**
     * Postbox & Stash.
     */
    public static final AbstractColonyBlock<?> blockPostBox = registerBlockHut(new BlockPostBox());
    public static final AbstractColonyBlock<?> blockStash   = registerBlockHut(new BlockStash());

    /**
     * Utility blocks.
     */
    public static final BlockConstructionTape       blockConstructionTape      = registerBlock(new BlockConstructionTape());
    public static final BlockMinecoloniesRack       blockRack                  = registerBlock(new BlockMinecoloniesRack());
    public static final BlockMinecoloniesGrave      blockGrave                 = registerBlock(new BlockMinecoloniesGrave());
    public static final BlockMinecoloniesNamedGrave blockNamedGrave            = registerBlock(new BlockMinecoloniesNamedGrave());
    public static final BlockWaypoint               blockWayPoint              = registerBlock(new BlockWaypoint());
    public static final BlockBarrel                 blockBarrel                = registerBlock(new BlockBarrel());
    public static final BlockDecorationController   blockDecorationPlaceholder = registerBlock(new BlockDecorationController());
    public static final BlockScarecrow              blockScarecrow             = registerBlock(new BlockScarecrow());
    public static final BlockPlantationField        blockPlantationField       = registerBlock(new BlockPlantationField());
    public static final BlockCompostedDirt          blockCompostedDirt         = registerBlock(new BlockCompostedDirt());
    public static final BlockColonyFlagBanner       blockColonyBanner          = registerBlock("colony_banner", new BlockColonyFlagBanner());
    public static final BlockColonyFlagWallBanner   blockColonyWallBanner      = registerBlock("colony_wall_banner", new BlockColonyFlagWallBanner());
    public static final BlockGate                   blockIronGate              = registerBlock(new BlockGate(BlockGate.IRON_GATE, 10f, 6, 8));
    public static final BlockGate                   blockWoodenGate            = registerBlock(new BlockGate(BlockGate.WOODEN_GATE, 7f, 6, 5));
    public static final BlockMinecoloniesFarmland   blockFarmland              = registerBlock(new BlockMinecoloniesFarmland(BlockMinecoloniesFarmland.FARMLAND, false, 15.0));
    public static final BlockMinecoloniesFarmland   blockFloodedFarmland       =
        registerBlock(new BlockMinecoloniesFarmland(BlockMinecoloniesFarmland.FLOODED_FARMLAND, true, 13.0));
    public static final BlockColonySign             blockColonySign            = registerBlock(new BlockColonySign());

    /**
     * Crop blocks.
     */
    public static final BlockMinecoloniesCrop blockBellPepper      =
        registerCrop(new BlockMinecoloniesCrop(BELL_PEPPER, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.temperateBiomes));
    public static final BlockMinecoloniesCrop blockCabbage         =
        registerCrop(new BlockMinecoloniesCrop(CABBAGE, ModBlocks.blockFarmland, List.of(Blocks.FERN), ModTags.coldBiomes));
    public static final BlockMinecoloniesCrop blockChickpea        =
        registerCrop(new BlockMinecoloniesCrop(CHICKPEA, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.DEAD_BUSH), ModTags.dryBiomes));
    public static final BlockMinecoloniesCrop blockDurum           =
        registerCrop(new BlockMinecoloniesCrop(DURUM, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
    public static final BlockMinecoloniesCrop blockEggplant        =
        registerCrop(new BlockMinecoloniesCrop(EGGPLANT, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
    public static final BlockMinecoloniesCrop blockGarlic          =
        registerCrop(new BlockMinecoloniesCrop(GARLIC, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
    public static final BlockMinecoloniesCrop blockOnion           =
        registerCrop(new BlockMinecoloniesCrop(ONION, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
    public static final BlockMinecoloniesCrop blockSoyBean         =
        registerCrop(new BlockMinecoloniesCrop(SOYBEAN, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS, Blocks.FERN), ModTags.humidBiomes));
    public static final BlockMinecoloniesCrop blockTomato          =
        registerCrop(new BlockMinecoloniesCrop(TOMATO, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.temperateBiomes));
    public static final BlockMinecoloniesCrop blockButternutSquash =
        registerCrop(new BlockMinecoloniesCrop(BUTTERNUT_SQUASH, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.coldBiomes));
    public static final BlockMinecoloniesCrop blockCorn            =
        registerCrop(new BlockMinecoloniesCrop(CORN, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.temperateBiomes));
    public static final BlockMinecoloniesCrop blockMint            =
        registerCrop(new BlockMinecoloniesCrop(MINT, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), null));
    public static final BlockMinecoloniesCrop blockNetherPepper    =
        registerCrop(new BlockMinecoloniesCrop(NETHER_PEPPER, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.dryBiomes));
    public static final BlockMinecoloniesCrop blockPeas            =
        registerCrop(new BlockMinecoloniesCrop(PEAS, ModBlocks.blockFarmland, List.of(Blocks.SHORT_GRASS, Blocks.TALL_GRASS), ModTags.humidBiomes));
    public static final BlockMinecoloniesCrop blockRice            =
        registerCrop(new BlockMinecoloniesCrop(RICE, ModBlocks.blockFloodedFarmland, List.of(Blocks.SEAGRASS, Blocks.SMALL_DRIPLEAF), ModTags.humidBiomes));

    /**
     * Private constructor to hide the implicit public one.
     */
    private ModBlocks()
    {
    }

    private static <T extends AbstractColonyBlock<?>> T registerBlockHut(final T block)
    {
        final T blockHut = registerBlock(block);
        HUTS.add(blockHut);
        return block;
    }

    private static <T extends BlockMinecoloniesCrop> T registerCrop(final T block)
    {
        final T cropBlock = registerBlock(block);
        CROPS.add(cropBlock);
        return block;
    }

    private static <T extends Block & IMinecoloniesBlock<?>> T registerBlock(final T block)
    {
        return registerBlock(block.getRegistryName().getPath(), block);
    }

    private static <T extends Block> T registerBlock(final String id, final T block)
    {
        DEFERRED_REGISTER.register(id, () -> block);
        return block;
    }

    @NotNull
    public static Collection<BlockMinecoloniesCrop> getCrops()
    {
        return CROPS;
    }
}
