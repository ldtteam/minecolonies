package com.minecolonies.items.crafting;

import com.minecolonies.blocks.ModBlocks;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class RecipeHandler
{
    public static void init()
    {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner, 1), "XXX", "X#X", "XXX", Character.valueOf('X'), Blocks.planks, Character.valueOf('#'), Items.wooden_pickaxe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutLumberjack, 1), "XXX", "X#X", "XXX", Character.valueOf('X'), Blocks.planks, Character.valueOf('#'), Items.wooden_axe);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBaker, 1), "XXX", "X#X", "XXX", Character.valueOf('X'), Blocks.planks, Character.valueOf('#'), Items.wheat);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBuilder, 1), "XXX", "X#X", "XXX", Character.valueOf('X'), Blocks.planks, Character.valueOf('#'), Items.wooden_door);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutDeliveryman, 1), "XXX", "X#X", "XXX", Character.valueOf('X'), Blocks.planks, Character.valueOf('#'), Blocks.chest);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutBlacksmith, 1), "XXX", "X#X", "XXX", Character.valueOf('X'), Blocks.planks, Character.valueOf('#'), Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutStonemason, 1), "XXX", "X#X", "XXX", Character.valueOf('X'), Blocks.planks, Character.valueOf('#'), Blocks.stonebrick);
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutFarmer, 1), "XXX", "X#X", "XXX", Character.valueOf('X'), Blocks.planks, Character.valueOf('#'), Items.wooden_hoe);
    }
}
