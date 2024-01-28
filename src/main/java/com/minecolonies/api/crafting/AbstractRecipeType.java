package com.minecolonies.api.crafting;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Base class for RecipeStorage types
 */
public abstract class AbstractRecipeType<R extends IRecipeStorage>
{
    final IRecipeStorage recipe;
    ResourceLocation id; 

    /**
     * Constructor basis for recipe types
     */
    public AbstractRecipeType(final R recipe)
    {
        this.recipe = recipe;
    }

    /**
     * Get the recipe this type instance is associated with
     */
    public IRecipeStorage getRecipe()
    {
        return this.recipe;
    }

    /**
     * Get the ID of this type
     */
    public abstract ResourceLocation getId();

    /**
     * The output display stacks, for rotation through in the views
     */
    public List<ItemStack> getOutputDisplayStacks()
    {
        return ImmutableList.of(recipe.getPrimaryOutput());
    }
}