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
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_pickaxe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner, 2), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.stone_pickaxe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_axe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 2), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.stone_axe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.acacia_door);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.birch_door);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.dark_oak_door);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.jungle_door);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.oak_door);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.spruce_door);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutCitizen, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.stick);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFisherman, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.fishing_rod);
        GameRegistry.addRecipe(new ItemStack(ModItems.scanTool, 1), "  I", " S ", "S  ", 'I', Items.iron_ingot, 'S', Items.stick);
        GameRegistry.addRecipe(new ItemStack(ModItems.buildTool, 1), "  C", " S ", "S  ", 'C', Blocks.cobblestone, 'S', Items.stick);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockSubstitution, 16), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', ModItems.scanTool);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFarmer, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_hoe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFarmer, 2), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.stone_hoe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutField, 1), " Y ", "X#X", " X ", 'X', Items.stick, '#', Items.leather_chestplate, 'Y', Blocks.hay_block);
        GameRegistry.addRecipe(new ItemStack(Blocks.web, 1), "XXX", "XXX", "XXX", 'X', Items.string);

        // Disabled for now
        // GameRegistry.addRecipe(new ItemStack(ModBlocks.blockBarrel, 1), "P P", "P P", " S ", 'P', Blocks.planks, 'S', Blocks.wooden_slab);
        if (enableInDevelopmentFeatures)
        {
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBaker, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wheat);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutWarehouse, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Blocks.chest);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.iron_ingot);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Blocks.stonebrick);
        }

        if (supplyChests)
        {
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.boat);
        }
        else
        {
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutTownHall, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.boat);
        }
    }
}
