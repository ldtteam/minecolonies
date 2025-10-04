package com.minecolonies.core.util;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.compatibility.IFurnaceRecipes;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FurnaceRecipes implements IFurnaceRecipes
{
    /**
     * Instance of the furnace recipes.
     */
    private static FurnaceRecipes instance;

    /**
     * Furnace recipes.
     */
    private Map<ItemStorage, RecipeStorage> recipes = new HashMap<>();
    private Map<ItemStorage, RecipeStorage> reverseRecipes = new HashMap<>();

    /**
     * Load all the recipes in the recipe storage.
     *
     * @param recipeManager  The recipe manager to parse.
     */
    public void loadRecipes(final RecipeManager recipeManager, final Level level)
    {
        recipes.clear();
        reverseRecipes.clear();
        recipeManager.byType(RecipeType.SMELTING).values().forEach(recipe -> {
            final NonNullList<Ingredient> list = recipe.getIngredients();
            if (list.size() == 1)
            {
                for(final ItemStack smeltable: list.get(0).getItems())
                {
                    if (!smeltable.isEmpty())
                    {
                        final RecipeStorage storage = RecipeStorage.builder()
                                .withInputs(ImmutableList.of(new ItemStorage(smeltable)))
                                .withPrimaryOutput(recipe.getResultItem(level.registryAccess()))
                                .withGridSize(1)
                                .withIntermediate(Blocks.FURNACE)
                                .withRecipeId(recipe.getId())
                                .build();

                        recipes.put(storage.getCleanedInput().get(0), storage);

                        final ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
                        output.setCount(1);
                        reverseRecipes.put(new ItemStorage(output), storage);
                    }
                }
            }
        });
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

    /**
     * Get the instance of the class.
     *
     * @return the instance.
     */
    public static FurnaceRecipes getInstance()
    {
        if (instance == null)
        {
            instance = new FurnaceRecipes();
        }
        return instance;
    }
}
