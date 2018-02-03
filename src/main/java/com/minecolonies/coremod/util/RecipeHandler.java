package com.minecolonies.coremod.util;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.TimberFrameType;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
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
    private static final int META_TWO = 2;

    private static final int META_THREE          = 3;

    /**
     * Number of paperwall recipes.
     */
    private static final int AMOUNT_OF_PAPERWALL = 4;

    /**
     * Number of shingle recipes.
     */
    private static final int AMOUNT_OF_SHINGLES = 6;

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
        addHutRecipe(new ItemStack(ModBlocks.blockHutSmeltery, 1), Items.IRON_INGOT);
        addHutRecipe(new ItemStack(ModBlocks.blockHutShepherd, 1), Items.SHEARS);
        addHutRecipe(new ItemStack(ModBlocks.blockHutCowboy, 1), Items.LEATHER);
        addHutRecipe(new ItemStack(ModBlocks.blockHutSwineHerder, 1), Items.PORKCHOP);
        addHutRecipe(new ItemStack(ModBlocks.blockHutChickenHerder, 1), Items.EGG);

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
        for(int i = 0; i < AMOUNT_OF_PAPERWALL; i++)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(ModBlocks.blockPaperWall, EIGHT_BLOCKS, i), "WWW", "PPP", "WWW", 'P', PAPER, 'W', new ItemStack(Blocks.PLANKS, 1, i)));
        }

        for(int i = 0; i < AMOUNT_OF_SHINGLES; i++)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(ModBlocks.blockShingleOak, EIGHT_BLOCKS, 0), "B  ", "SB ", "PSB", 'B', Items.BRICK, 'P',
                    new ItemStack(Blocks.PLANKS, 1, i), 'S', WOODEN_STICK));
        }

        GameRegistry.addRecipe(new ShapedOreRecipe(
                new ItemStack(ModBlocks.blockShingleSlab, EIGHT_BLOCKS, 0), "   ", "BBB", "SSS", 'B', Items.BRICK, 'S', WOODEN_STICK));

        for(int i = 0; i < ModBlocks.timberFrames.size(); i++)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(
                    new ItemStack(ModBlocks.timberFrames.get(i), FOUR_BLOCKS, 0), " P ", " P ", " B ", 'P',
                    new ItemStack(Blocks.PLANKS, 1, i), 'B', ModItems.buildTool));

            for(int j = 1; j < TimberFrameType.values().length; j++)
            {
                GameRegistry.addRecipe(new ShapelessOreRecipe(
                        new ItemStack(ModBlocks.timberFrames.get(i), FOUR_BLOCKS, j),
                        new ItemStack(ModBlocks.timberFrames.get(i), FOUR_BLOCKS, j-1), new ItemStack(ModItems.buildTool)));
            }
        }

        //Field
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutField, 1),
                                                    " Y ", "X#X", " X ", 'X', WOODEN_STICK, '#', Items.LEATHER, 'Y', Blocks.HAY_BLOCK));
        //Double Fern
        GameRegistry.addShapelessRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, META_THREE),
                new ItemStack(Blocks.TALLGRASS, 1, META_TWO),
                new ItemStack(Blocks.TALLGRASS, 1, META_TWO));
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
}
