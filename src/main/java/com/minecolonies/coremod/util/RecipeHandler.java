package com.minecolonies.coremod.util;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.jetbrains.annotations.NotNull;

/**
 * Recipe storage for minecolonies.
 */
public final class RecipeHandler
{

    /**
     * Wooden stick string description.
     */
    private static final String WOODEN_STICK = "stickWood";

    /**
     * Plank wood string description.
     */
    private static final String PLANK_WOOD = "plankWood";

    /**
     * Paper string description.
     */
    private static final String PAPER = "paper";

    /**
     * The amount of items returned by certain crafting recipes
     */

    private static final int FOUR_BLOCKS = 4;

    private static final int EIGHT_BLOCKS = 8;

    private static final int ONE_FORTH_OF_A_STACK = 16;

    /**
     * Wood types and timber frame types constants
     */
    private static final int META_ZERO = 0;

    private static final int META_ONE = 1;

    private static final int META_TWO = 2;

    private static final int META_THREE = 3;

    private static final int META_FOUR = 4;

    private static final int META_FIVE = 5;

    private static final int META_SIX = 6;

    private static final int META_SEVEN = 7;

    private static final int META_EIGHT = 8;

    private static final int META_NINE = 9;

    /**
     * Private constructor to hide the implicit public one.
     */
    private RecipeHandler()
    {
    }

    /**
     * Initialize all recipes for minecolonies.
     *
     * @param enableInDevelopmentFeatures if we want development recipes.
     * @param supplyChests                if we want supply chests or direct
     *                                    town hall crafting.
     */
    public static void init(final boolean enableInDevelopmentFeatures, final boolean supplyChests)
    {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockConstructionTape, ONE_FORTH_OF_A_STACK), "SWS", "S S", "S S",
          'S', Items.STICK, 'W', new ItemStack(Blocks.WOOL, 1, Constants.YELLOW));
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockConstructionTapeCorner, 1), ModBlocks.blockConstructionTape);
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockConstructionTape, 1), ModBlocks.blockConstructionTapeCorner);

        // Register the hust
        addHutRecipe(new ItemStack(ModBlocks.blockHutMiner, 1), Items.WOODEN_PICKAXE);
        addHutRecipe(new ItemStack(ModBlocks.blockHutMiner, 2), Items.STONE_PICKAXE);
        addHutRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 1), Items.WOODEN_AXE);
        addHutRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 2), Items.STONE_AXE);
        addHutRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), Items.ACACIA_DOOR);
        addHutRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), Items.BIRCH_DOOR);
        addHutRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), Items.DARK_OAK_DOOR);
        addHutRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), Items.JUNGLE_DOOR);
        addHutRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), Items.OAK_DOOR);
        addHutRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), Items.SPRUCE_DOOR);
        addHutRecipe(new ItemStack(ModBlocks.blockHutFarmer, 1), Items.WOODEN_HOE);
        addHutRecipe(new ItemStack(ModBlocks.blockHutFarmer, 2), Items.STONE_HOE);
        addHutRecipe(new ItemStack(ModBlocks.blockHutCitizen, 1), "torch");
        addHutRecipe(new ItemStack(ModBlocks.blockHutFisherman, 1), Items.FISHING_ROD);
        addHutRecipe(new ItemStack(ModBlocks.blockHutGuardTower, 2), Items.BOW);
        addHutRecipe(new ItemStack(ModBlocks.blockHutWareHouse, 1), "chest");
        addHutRecipe(new ItemStack(ModBlocks.blockHutDeliveryman, 1), Items.LEATHER_BOOTS);
        addHutRecipe(new ItemStack(ModBlocks.blockHutBaker, 1), Items.WHEAT);
        addHutRecipe(new ItemStack(ModBlocks.blockHutBarracks, 1), Blocks.IRON_BLOCK);
        addHutRecipe(new ItemStack(ModBlocks.blockHutCook, 1), Items.APPLE);

        //Mod Items
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.scanTool, 1), "  I", " S ", "S  ", 'I', Items.IRON_INGOT, 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.buildTool, 1), "  C", " S ", "S  ", 'C', "cobblestone", 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.permTool, 1), "D", 'D', ModItems.scanTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.scanTool, 1), "B", 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.scanTool, 1), "P", 'P', ModItems.permTool));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockSubstitution, ONE_FORTH_OF_A_STACK),
                                                    "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', ModItems.scanTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockSolidSubstitution, ONE_FORTH_OF_A_STACK),
                                                    "XXX", "X#X", "XXX", 'X', "logWood", '#', ModItems.scanTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockWayPoint, ONE_FORTH_OF_A_STACK),
                                                    "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', ModItems.buildTool));
        GameRegistry.addShapelessRecipe(new ItemStack(ModItems.clipboard, 1), new ItemStack(Items.STICK, 1), new ItemStack(Items.MAP, 1), new ItemStack(Items.DYE));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.clipboard), "WBW", "WLW", "WWW", 'L', Items.LEATHER, 'W', WOODEN_STICK, 'B', ModItems.buildTool));

        //Block Rack
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockRack, 1),
                                                    "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Blocks.IRON_BARS));

        //Building blocks
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockPaperWall, EIGHT_BLOCKS, 0), "WWW", "PPP", "WWW", 'P', PAPER, 'W', new ItemStack(Blocks.PLANKS, 1, 0)));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockPaperWall, EIGHT_BLOCKS, 1), "WWW", "PPP", "WWW", 'P', PAPER, 'W', new ItemStack(Blocks.PLANKS, 1, 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockPaperWall, EIGHT_BLOCKS, 2), "WWW", "PPP", "WWW", 'P', PAPER, 'W', new ItemStack(Blocks.PLANKS, 1, 2)));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockPaperWall, EIGHT_BLOCKS, 3), "WWW", "PPP", "WWW", 'P', PAPER, 'W', new ItemStack(Blocks.PLANKS, 1, 3)));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockShingleOak, EIGHT_BLOCKS, 0), "B  ", "SB ", "PSB", 'B', Items.BRICK, 'P',
                new ItemStack(Blocks.PLANKS, 1, 0), 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockShingleSpruce, EIGHT_BLOCKS, 0), "B  ", "SB ", "PSB", 'B', Items.BRICK, 'P',
                new ItemStack(Blocks.PLANKS, 1, 1), 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockShingleBirch, EIGHT_BLOCKS, 0), "B  ", "SB ", "PSB", 'B', Items.BRICK, 'P',
                new ItemStack(Blocks.PLANKS, 1, 2), 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockShingleJungle, EIGHT_BLOCKS, 0), "B  ", "SB ", "PSB", 'B', Items.BRICK, 'P',
                new ItemStack(Blocks.PLANKS, 1, 3), 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockShingleAcacia, EIGHT_BLOCKS, 0), "B  ", "SB ", "PSB", 'B', Items.BRICK, 'P',
                new ItemStack(Blocks.PLANKS, 1, 4), 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockShingleDarkOak, EIGHT_BLOCKS, 0), "B  ", "SB ", "PSB", 'B', Items.BRICK, 'P',
                new ItemStack(Blocks.PLANKS, 1, 5), 'S', WOODEN_STICK));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockShingleSlab, EIGHT_BLOCKS, 0), "   ", "BBB", "SSS", 'B', Items.BRICK, 'S', WOODEN_STICK));

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_ZERO), " X ", " X ", " XB", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_ONE), "X X", " X ", "XBX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_TWO), "SXS", "XBX", "SXS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_THREE), "SXS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_FOUR ), "SSS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_FIVE), "X  ", " X ", " BX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_SIX), "  X", " X ", "XB ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_SEVEN), "SXS", "SBS", "SSS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_EIGHT),"SSS", "XXX", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ZERO), FOUR_BLOCKS, META_NINE), "   ", "XXX", " B ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ZERO), 'B', ModItems.buildTool));


        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_ZERO), " X ", " X ", " XB", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_ONE), "X X", " X ", "XBX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_TWO), "SXS", "XBX", "SXS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_THREE), "SXS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_FOUR ), "SSS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_FIVE), "X  ", " X ", " BX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_SIX), "  X", " X ", "XB ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_SEVEN), "SXS", "SBS", "SSS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_EIGHT),"SSS", "XXX", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_ONE), FOUR_BLOCKS, META_NINE), "   ", "XXX", " B ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_ONE), 'B', ModItems.buildTool));


        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_ZERO), " X ", " X ", " XB", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_ONE), "X X", " X ", "XBX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_TWO), "SXS", "XBX", "SXS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_THREE), "SXS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_FOUR ), "SSS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_FIVE), "X  ", " X ", " BX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_SIX), "  X", " X ", "XB ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_SEVEN), "SXS", "SBS", "SSS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_EIGHT),"SSS", "XXX", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_TWO), FOUR_BLOCKS, META_NINE), "   ", "XXX", " B ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_TWO), 'B', ModItems.buildTool));


        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_ZERO), " X ", " X ", " XB", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_ONE), "X X", " X ", "XBX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_TWO), "SXS", "XBX", "SXS", 'S',
                WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_THREE), "SXS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_FOUR ), "SSS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_FIVE), "X  ", " X ", " BX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_SIX), "  X", " X ", "XB ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_SEVEN), "SXS", "SBS", "SSS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_EIGHT),"SSS", "XXX", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_THREE), FOUR_BLOCKS, META_NINE), "   ", "XXX", " B ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_THREE), 'B', ModItems.buildTool));


        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_ZERO), " X ", " X ", " XB", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_ONE), "X X", " X ", "XBX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_TWO), "SXS", "XBX", "SXS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_THREE), "SXS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_FOUR ), "SSS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_FIVE), "X  ", " X ", " BX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_SIX), "  X", " X ", "XB ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_SEVEN), "SXS", "SBS", "SSS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_EIGHT),"SSS", "XXX", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FOUR), FOUR_BLOCKS, META_NINE), "   ", "XXX", " B ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FOUR), 'B', ModItems.buildTool));


        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_ZERO), " X ", " X ", " XB", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_ONE), "X X", " X ", "XBX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_TWO), "SXS", "XBX", "SXS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_THREE), "SXS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_FOUR ), "SSS", "SXS", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_FIVE), "X  ", " X ", " BX", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_SIX), "  X", " X ", "XB ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_SEVEN), "SXS", "SBS", "SSS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_EIGHT),"SSS", "XXX", "SBS", 'S', WOODEN_STICK, 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.timberFrames.get(META_FIVE), FOUR_BLOCKS, META_NINE), "   ", "XXX", " B ", 'X',
                new ItemStack(Blocks.PLANKS, 1, META_FIVE), 'B', ModItems.buildTool));

        //Field
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutField, 1),
                                                    " Y ", "X#X", " X ", 'X', WOODEN_STICK, '#', Items.LEATHER, 'Y', Blocks.HAY_BLOCK));
        //Double Fern
        GameRegistry.addShapelessRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, META_THREE), new ItemStack(Blocks.TALLGRASS, 1, META_TWO), new ItemStack(Blocks.TALLGRASS, 1, META_TWO));
    }

    private static void addHutRecipe(@NotNull final ItemStack hutItemStack, @NotNull final Item item)
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(hutItemStack, "XBX", "X#X", "XXX", 'B', ModItems.buildTool, 'X', PLANK_WOOD, '#', item));
    }

    private static void addHutRecipe(@NotNull final ItemStack hutItemStack, @NotNull final String item)
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(hutItemStack, "XBX", "X#X", "XXX", 'B', ModItems.buildTool, 'X', PLANK_WOOD, '#', item));
    }

    private static void addHutRecipe(@NotNull final ItemStack hutItemStack, @NotNull final Block block)
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(hutItemStack, "XBX", "X#X", "XXX", 'B', ModItems.buildTool, 'X', PLANK_WOOD, '#', block));
    }

    private static void addSupplyChestRecipes(final boolean enable)
    {
        if (enable)
        {
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.ACACIA_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.BIRCH_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.DARK_OAK_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.JUNGLE_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.SPRUCE_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyCamp, 1), "B B", "BBB", 'B', Blocks.CHEST);
        }
        else
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutTownHall, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.BOAT));
        }
    }

    private static void enableInDevelopmentFeatures(final boolean enable)
    {
        if (enable)
        {
            addHutRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "ingotIron");
            addHutRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), Blocks.STONEBRICK);
        }
    }
}
