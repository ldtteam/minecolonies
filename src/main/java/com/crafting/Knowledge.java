package com.crafting;

import com.minecolonies.util.InventoryUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * One bit of knowledge from the crafting system.
 */
public class Knowledge
{
    @NotNull
    private final IRecipe         recipe;
    @NotNull
    private final List<ItemStack> requirements;
    @NotNull
    private final ItemStack       output;

    public static Knowledge tryFromRecipeAndGrid(@NotNull final InventoryCrafting craftingGrid, @NotNull final IRecipe recipe, @NotNull World world)
    {
        if (!recipe.matches(craftingGrid, world))
        {
            throw new IllegalStateException("Recipe did not match!");
        }
        @Nullable final ItemStack recipeOutput = recipe.getRecipeOutput();
        if(recipeOutput == null){
            throw new IllegalStateException("Recipe did output null!");
        }
        return new Knowledge(recipe, InventoryUtils.getInventoryAsList(craftingGrid), recipeOutput);
    }

    private Knowledge(@NotNull final IRecipe recipe, @NotNull final List<ItemStack> requirements, @NotNull final ItemStack output)
    {
        this.recipe = recipe;
        this.requirements = requirements;
        this.output = output;
    }
}
