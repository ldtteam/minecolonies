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
     * @param supplyChests                if we want supply chests or direct town hall crafting.
     */
    public static void init(final boolean enableInDevelopmentFeatures, final boolean supplyChests)
    {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockConstructionTape, ONE_FORTH_OF_A_STACK), "SWS", "S S", "S S",
          'S', Items.STICK, 'W', new ItemStack(Blocks.WOOL, 1, Constants.YELLOW));
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockConstructionTapeCorner, 1),ModBlocks.blockConstructionTape);
        GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.blockConstructionTape, 1),ModBlocks.blockConstructionTapeCorner);

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

        //Register Scepters
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.scanTool, 1), "  I", " S ", "S  ", 'I', Items.IRON_INGOT, 'S', WOODEN_STICK));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModItems.buildTool, 1), "  C", " S ", "S  ", 'C', "cobblestone", 'S', WOODEN_STICK));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockSubstitution, ONE_FORTH_OF_A_STACK), 
                "XXX", "X#X", "XXX", 'X', PLANK_WOOD, '#', ModItems.scanTool));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockSolidSubstitution, ONE_FORTH_OF_A_STACK),
                "XXX", "X#X", "XXX", 'X', "logWood", '#', ModItems.scanTool));


        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.blockHutField, 1), 
                " Y ", "X#X", " X ", 'X', WOODEN_STICK, '#', Items.LEATHER, 'Y', Blocks.HAY_BLOCK));
        GameRegistry.addRecipe(new ItemStack(Blocks.WEB, 1), "X X", " X ", "X X", 'X', Items.STRING);

        //enableInDevelopmentFeatures(enableInDevelopmentFeatures);
        addSupplyChestRecipes(supplyChests);
    }

    private static void enableInDevelopmentFeatures(final boolean enable)
    {
        if (enable)
        {
            addHutRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "ingotIron");
            addHutRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), Blocks.STONEBRICK);
        }
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
}
