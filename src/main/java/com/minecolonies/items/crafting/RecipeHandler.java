package com.minecolonies.items.crafting;

import com.minecolonies.blocks.ModBlocks;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public final class RecipeHandler
{
    public static void init() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.blockHutMiner), "XXX", "X#X", "XXX", Character.valueOf('X'), Blocks.planks, Character.valueOf('#'));
    }
}
