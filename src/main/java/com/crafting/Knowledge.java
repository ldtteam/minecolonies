package com.crafting;

import com.minecolonies.util.InventoryFunctions;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import java.util.List;

/**
 * One bit of knowledge from the crafting system.
 */
public class Knowledge
{
    private final IRecipe         recipe;
    private final List<ItemStack> requirements;
    private final ItemStack output;



    public Knowledge(final InventoryCrafting craftingGrid, final IRecipe recipe, World world)
    {
        this(recipe, InventoryUtils.getInventoryAsList(craftingGrid), recipe.getRecipeOutput());
        if(!recipe.matches(craftingGrid, world)){
            throw new IllegalStateException("Recipe did not match!");
        }
    }

    private Knowledge(final IRecipe recipe, final List<ItemStack> requirements, final ItemStack output)
    {
        this.recipe = recipe;
        this.requirements = requirements;
        this.output = output;
    }
}
