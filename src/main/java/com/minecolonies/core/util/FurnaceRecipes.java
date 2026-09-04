package com.minecolonies.core.util;
import com.minecolonies.api.crafting.RecipeUtils;
import com.minecolonies.api.util.ItemStackUtils;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.compatibility.IFurnaceRecipes;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FurnaceRecipes implements IFurnaceRecipes
{
    /**
     * Furnace recipes.
     */
    private Map<ItemStorage, RecipeStorage> recipes = new HashMap<>();
    private Map<ItemStorage, RecipeStorage> reverseRecipes = new HashMap<>();

    /**
     * Load all the recipes in the recipe storage.
     *
     * @param recipes The vanilla recipes to parse.
     */
    public void loadRecipes(final Collection<RecipeHolder<?>> vanillaRecipes, final Level level)
    {
        recipes.clear();
        reverseRecipes.clear();
        for (final RecipeHolder<?> holder : vanillaRecipes)
        {
            if (!(holder.value() instanceof final SmeltingRecipe recipe)) continue;
            final List<Ingredient> list = RecipeUtils.getIngredients(recipe);
            if (list.size() == 1)
            {
                for(final ItemStack smeltable: ItemStackUtils.getIngredientStacks(list.get(0)))
                {
                    if (!smeltable.isEmpty())
                    {
                        final RecipeStorage storage = RecipeStorage.builder()
                                .withInputs(ImmutableList.of(new ItemStorage(smeltable)))
                                .withPrimaryOutput(RecipeUtils.getOutput(recipe, level))
                                .withGridSize(1)
                                .withIntermediate(Blocks.FURNACE)
                                .withRecipeId(holder.id().identifier())
                                .build();

                        recipes.put(storage.getCleanedInput().get(0), storage);

                        final ItemStack output = RecipeUtils.getOutput(recipe, level).copy();
                        output.setCount(1);
                        reverseRecipes.put(new ItemStorage(output), storage);
                    }
                }
            }
        }
    }

    @Override
    public ItemStack getSmeltingResult(final ItemStack itemStack)
    {
        final RecipeStorage storage = recipes.getOrDefault(new ItemStorage(itemStack), null);
        if (storage != null)
        {
            return storage.getPrimaryOutput();
        }
        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public RecipeStorage getFirstSmeltingRecipeByResult(final ItemStorage storage)
    {
        return reverseRecipes.get(storage);
    }
}
