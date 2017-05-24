package com.minecolonies.coremod.entity.ai.citizen.baker;

import com.minecolonies.coremod.entity.ai.util.RecipeStorage;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all recipes the baker can use.
 */
public class BakerRecipes
{
    /**
     * The grid size the baker can use.
     */
    private static final int GRID_SIZE = 9;

    /**
     * Amount of wheat required for recipe.
     */
    private static final int REQUIRED_WHEAT = 3;

    /**
     * Amount of milk buckets required for the recipe.
     */
    private static final int REQUIRED_MILK = 1;

    /**
     * Amount of sugar required for a recipe.
     */
    private static final int REQUIRED_SUGAR = 2;

    /**
     * Amount of egg required for a recipe.
     */
    private static final int REQUIRED_EGGS = 1;

    /**
     * Amount of cocoa required for a recipe.
     */
    private static final int REQUIRED_COCOA = 1;

    /**
     * Amount of wheat required for a cookie recipe.
     */
    private static final int REQUIRED_WHEAT_COOKIES = 2;

    /**
     * List of recipes the Baker can know.
     */
    private static final List<RecipeStorage> recipes = new ArrayList<>();

    static
    {
        final List<ItemStack> inputCake = new ArrayList<>();
        inputCake.add(new ItemStack(Items.WHEAT, REQUIRED_WHEAT));
        inputCake.add(new ItemStack(Items.MILK_BUCKET, REQUIRED_MILK));
        inputCake.add(new ItemStack(Items.MILK_BUCKET, REQUIRED_MILK));
        inputCake.add(new ItemStack(Items.MILK_BUCKET, REQUIRED_MILK));
        inputCake.add(new ItemStack(Items.SUGAR, REQUIRED_SUGAR));
        inputCake.add(new ItemStack(Items.EGG, REQUIRED_EGGS));

        final List<ItemStack> inputCookie = new ArrayList<>();
        inputCookie.add(new ItemStack(Items.WHEAT, REQUIRED_WHEAT_COOKIES));
        inputCookie.add(new ItemStack(Items.DYE, REQUIRED_COCOA, 3));

        final List<ItemStack> inputBread = new ArrayList<>();
        inputBread.add(new ItemStack(Items.WHEAT, REQUIRED_WHEAT));

        recipes.add(new RecipeStorage(inputCookie, GRID_SIZE, new ItemStack(Items.COOKIE, 1)));
        recipes.add(new RecipeStorage(inputCake, GRID_SIZE, new ItemStack(Items.CAKE, 1), new ItemStack(Items.BUCKET, REQUIRED_MILK)));
        recipes.add(new RecipeStorage(inputBread, GRID_SIZE, new ItemStack(Items.BREAD, 1)));
    }

    /**
     * Get the list of recipes from the class.
     * @return a copy of the recipes.
     */
    public static List<RecipeStorage> getRecipes()
    {
        return new ArrayList<>(recipes);
    }
}
