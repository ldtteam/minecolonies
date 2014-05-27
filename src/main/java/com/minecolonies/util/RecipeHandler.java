package com.minecolonies.util;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class RecipeHandler
{
    public static void init()
    {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_pickaxe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_axe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBaker, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wheat);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_door);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutWarehouse, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Blocks.chest);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Blocks.stonebrick);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFarmer, 1), "XXX", "X#X", "XXX", 'X', Blocks.planks, '#', Items.wooden_hoe);

        GameRegistry.addRecipe(new ItemStack(ModItems.supplyChest, 1), "B B", "BBB", 'B', Items.boat);
    }
}
