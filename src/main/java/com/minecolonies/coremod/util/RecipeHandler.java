package com.minecolonies.coremod.util;

import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.items.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Recipe storage for minecolonies.
 */
public final class RecipeHandler
{

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
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.WOODEN_PICKAXE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner, 2), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.STONE_PICKAXE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.WOODEN_AXE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 2), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.STONE_AXE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.ACACIA_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.BIRCH_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.DARK_OAK_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.JUNGLE_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.OAK_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.SPRUCE_DOOR);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutCitizen, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Blocks.TORCH);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFisherman, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.FISHING_ROD);
        GameRegistry.addRecipe(new ItemStack(ModItems.scanTool, 1), "  I", " S ", "S  ", 'I', Items.IRON_INGOT, 'S', Items.STICK);
        GameRegistry.addRecipe(new ItemStack(ModItems.buildTool, 1), "  C", " S ", "S  ", 'C', Blocks.COBBLESTONE, 'S', Items.STICK);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockSubstitution, 16), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', ModItems.scanTool);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockSolidSubstitution, 16), "XXX", "X#X", "XXX", 'X', Blocks.LOG, '#', ModItems.scanTool);

        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFarmer, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.WOODEN_HOE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFarmer, 2), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.STONE_HOE);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutField, 1), " Y ", "X#X", " X ", 'X', Items.STICK, '#', Items.LEATHER, 'Y', Blocks.HAY_BLOCK);
        GameRegistry.addRecipe(new ItemStack(Blocks.WEB, 1), "X X", " X ", "X X", 'X', Items.STRING);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutGuardTower, 2), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.BOW);

        // Disabled for now
        // GameRegistry.addRecipe(new ItemStack(ModBlocks.blockBarrel, 1), "P P", "P P", " S ", 'P', Blocks.planks, 'S', Blocks.wooden_slab);
        if (enableInDevelopmentFeatures)
        {
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBaker, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.WHEAT);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutWarehouse, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Blocks.CHEST);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.IRON_INGOT);
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Blocks.STONEBRICK);
        }

        if (supplyChests)
        {
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.ACACIA_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.BIRCH_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.DARK_OAK_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.JUNGLE_BOAT);
            GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.SPRUCE_BOAT);
        }
        else
        {
            GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutTownHall, 1), "XXX", "X#X", "XXX", 'X', Blocks.PLANKS, '#', Items.BOAT);
        }
    }
}
