package com.minecolonies.core.util;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.compatibility.IFurnaceRecipes;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

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
    public void loadRecipes(final RecipeManager recipeManager, final Level level)
    {
        recipes.clear();
        reverseRecipes.clear();
        loadUtilityPredicates();
        recipeManager.getAllRecipesFor(RecipeType.SMELTING).forEach(holder -> {
            final SmeltingRecipe recipe = holder.value();
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
                          recipe.getResultItem(level.registryAccess()),
                          Blocks.FURNACE,
                          holder.id());

                        recipes.put(storage.getCleanedInput().get(0), storage);

                        final ItemStack output = recipe.getResultItem(level.registryAccess()).copy();
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
    public void loadUtilityPredicates()
    {
        ItemStackUtils.IS_SMELTABLE = itemStack -> !ItemStackUtils.isEmpty(instance.getSmeltingResult(itemStack));
        ItemStackUtils.ISCOOKABLE = itemStack -> ItemStackUtils.ISFOOD.test(instance.getSmeltingResult(itemStack));
        ItemStackUtils.CAN_EAT =
                itemStack -> ItemStackUtils.ISFOOD.test(itemStack) && !ItemStackUtils.ISCOOKABLE.test(itemStack);
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
}
