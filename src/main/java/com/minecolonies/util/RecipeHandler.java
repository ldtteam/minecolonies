package com.minecolonies.util;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Recipe storage for minecolonies
 */
public final class RecipeHandler
{

    /**
     * Private constructor to hide the implicit public one
     */
    private RecipeHandler()
    {
    }

    /**
     * Initialize all recipes for minecolonies
     *
     * @param enableInDevelopmentFeatures if we want development recipes
     * @param supplyChests                if we want supply chests or direct town hall crafting
     */
    public static void init(final boolean enableInDevelopmentFeatures, final boolean supplyChests)
    {
        /*GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.WOODEN_PICKAXE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner, 2), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.STONE_PICKAXE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.WOODEN_AXE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 2), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.STONE_AXE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.ACACIA_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.BIRCH_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.DARK_OAK_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.JUNGLE_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.OAK_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.SPRUCE_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutCitizen, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.STICK);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFisherman, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.FISHING_ROD);
        GameRegistry.addRecipe(new ItemStack(ModItems.scanTool, 1), "  I", " S ", "S  ", 'I', Items.IRON_INGOT, 'S', Items.STICK);
        GameRegistry.addRecipe(new ItemStack(ModItems.buildTool, 1), "  C", " S ", "S  ", 'C', Blocks.COBBLESTONE, 'S', Items.STICK);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockSubstitution,16), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', ModItems.scanTool);
        
        if (enableInDevelopmentFeatures)
        {
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBaker, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.WHEAT);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutWarehouse, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Blocks.CHEST);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.IRON_INGOT);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Blocks.STONEBRICK);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFarmer, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.WOODEN_HOE);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFarmer, 2), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.STONE_HOE);
        }

        if (supplyChests)
        {
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.BOAT);
        }
        else
        {
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutTownHall, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.BOAT);
        }*/
    }
}
