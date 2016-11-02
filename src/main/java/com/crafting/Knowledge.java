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
    private final ItemStack output;



    public Knowledge(@NotNull final InventoryCrafting craftingGrid, @NotNull final IRecipe recipe, @NotNull World world)
    {
        this(recipe, InventoryUtils.getInventoryAsList(craftingGrid), recipe.getRecipeOutput());
        if(!recipe.matches(craftingGrid, world)){
            throw new IllegalStateException("Recipe did not match!");
        }

    }

    private Knowledge(@NotNull final IRecipe recipe, @NotNull final List<ItemStack> requirements, @Nullable final ItemStack output)
    {
        if(output == null){
            throw new IllegalStateException("Recipe did not output anything!");
        }
        this.recipe = recipe;
        this.requirements = requirements;
        this.output = output;
    }
}
