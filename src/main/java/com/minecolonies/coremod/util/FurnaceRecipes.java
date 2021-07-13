package com.minecolonies.coremod.util;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.compatibility.IFurnaceRecipes;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.recipes.FoodIngredient;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.NonNullList;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class FurnaceRecipes implements IFurnaceRecipes
{
    /**
     * Furnace recipes.
     */
    private Map<ItemStorage, RecipeStorage> recipes = new HashMap<>();
    private Map<ItemStorage, RecipeStorage> reverseRecipes = new HashMap<>();

    /**
     * Instance of the furnace recipes.
     */
    public static FurnaceRecipes instance;

    /**
     * Load all the recipes in the recipe storage.
     *
     * @param recipeManager  The recipe manager to parse.
     */
    public void loadRecipes(final RecipeManager recipeManager)
    {
        recipes.clear();
        reverseRecipes.clear();
        loadUtilityPredicates();
        recipeManager.byType(IRecipeType.SMELTING).values().forEach(recipe -> {
            final NonNullList<Ingredient> list = recipe.getIngredients();
            if (list.size() == 1)
            {
                for(final ItemStack smeltable: list.get(0).getItems())
                {
                    if (!smeltable.isEmpty())
                    {
                        final RecipeStorage storage = StandardFactoryController.getInstance().getNewInstance(
                          TypeConstants.RECIPE,
                          StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                          ImmutableList.of(new ItemStorage(smeltable)),
                          1,
                          recipe.getResultItem(),
                          Blocks.FURNACE,
                          recipe.getId());

                        recipes.put(storage.getCleanedInput().get(0), storage);

                        final ItemStack output = recipe.getResultItem().copy();
                        output.setCount(1);
                        reverseRecipes.put(new ItemStorage(output), storage);
                    }
                }
            }
        });
    }

    /**
     * Load all the utility predicates.
     */
    private static void loadUtilityPredicates()
    {
        ItemStackUtils.ISFOOD = FoodIngredient.ISFOOD;
        ItemStackUtils.IS_SMELTABLE = itemStack -> !ItemStackUtils.isEmpty(instance.getSmeltingResult(itemStack));
        ItemStackUtils.ISCOOKABLE = itemStack -> ItemStackUtils.ISFOOD.test(instance.getSmeltingResult(itemStack));
        ItemStackUtils.CAN_EAT =
                itemStack -> ItemStackUtils.ISFOOD.test(itemStack) && !ItemStackUtils.ISCOOKABLE.test(itemStack);
    }

    /**
     * Set the map. This is called from the client side message.
     *
     * @param map the map to set.
     */
    public void setMap(final Map<ItemStorage, RecipeStorage> map)
    {
        this.recipes = map;
        if (ItemStackUtils.ISFOOD == null)
        {
            loadUtilityPredicates();
        }
    }

    /**
     * Get the smelting result for a certain itemStack.
     *
     * @param itemStack the itemStack to test.
     * @return the result or empty if not existent.
     */
    public ItemStack getSmeltingResult(final ItemStack itemStack)
    {
        final RecipeStorage storage = recipes.getOrDefault(new ItemStorage(itemStack), null);
        if (storage != null)
        {
            return storage.getPrimaryOutput();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Get the first smelting recipe by result for a certain itemStack predicate.
     *
     * @param stackPredicate the predicate to test.
     * @return the result or null if not existent.
     */
    public RecipeStorage getFirstSmeltingRecipeByResult(final Predicate<ItemStack> stackPredicate)
    {
        Optional<ItemStorage> index = reverseRecipes.keySet().stream().filter(item -> stackPredicate.test(item.getItemStack())).findFirst();
        if(index.isPresent()) {
            return reverseRecipes.getOrDefault(index.get(), null);
        }
        return null;
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

    /**
     * Method to check if the furnace recipes are loaded already.
     *
     * @return true if so.
     */
    public boolean loaded()
    {
        return !recipes.isEmpty();
    }
}
