package com.minecolonies.coremod.util;

import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.lib.Constants;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

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
     * The amount of items returned by certain crafting recipes
     */
    private static final int ONE_FORTH_OF_A_STACK = 16;
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
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockConstructionTapeCorner, 1),ModBlocks.blockConstructionTape);
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockConstructionTape, 1),ModBlocks.blockConstructionTapeCorner);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutMiner, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.WOODEN_PICKAXE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutMiner, 2), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.STONE_PICKAXE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.WOODEN_AXE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 2), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.STONE_AXE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.ACACIA_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.BIRCH_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.DARK_OAK_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.JUNGLE_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.OAK_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.SPRUCE_DOOR));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutCitizen, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', "torch"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutFisherman, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.FISHING_ROD));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.scanTool, 1), "  I", " S ", "S  ", 'I', Items.IRON_INGOT, 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.buildTool, 1), "  C", " S ", "S  ", 'C', "cobblestone", 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockSubstitution, ONE_FORTH_OF_A_STACK), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', ModItems.scanTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockSolidSubstitution, ONE_FORTH_OF_A_STACK),
                "XXX", "X#X", "XXX", 'X', "logWood", '#', ModItems.scanTool));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutFarmer, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.WOODEN_HOE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutFarmer, 2), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.STONE_HOE));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutField, 1), " Y ", "X#X", " X ", 'X', WOODEN_STICK, '#', Items.LEATHER, 'Y', Blocks.HAY_BLOCK));
        GameRegistry.addRecipe(new ItemStack(Blocks.WEB, 1), "X X", " X ", "X X", 'X', Items.STRING);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutGuardTower, 2), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.BOW));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutWareHouse, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', "chest")); // check
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutDeliveryman, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.LEATHER_BOOTS));

        enableInDevelopmentFeatures(enableInDevelopmentFeatures);
        addSupplyChestRecipes(supplyChests);
    }

    private static void enableInDevelopmentFeatures(final boolean enable)
    {
        if (enable)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBaker, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', Items.WHEAT));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', "ingotIron"));
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#',
                    Blocks.STONEBRICK));
        }
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
}
