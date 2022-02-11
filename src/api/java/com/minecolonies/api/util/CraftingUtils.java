package com.minecolonies.api.util;

import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.ModTags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Utility class that handles crafting duties
 */
public final class CraftingUtils
{

    private CraftingUtils()
    {
        throw new IllegalStateException("Tried to initialize: CraftingUtils but this is a Utility class.");
    }

    /**
     * Calculate the max time a recipe has to be executed.
     *
     * @param outputStack the output stack.
     * @param storage     the storage.
     * @return the quantity.
     */
    public static int calculateMaxCraftingCount(@NotNull final ItemStack outputStack, @NotNull final IRecipeStorage storage)
    {
        //Calculate the initial crafting count from the request and the storage output.
        int craftingCount = (int) Math.ceil(
          Math.max(ItemStackUtils.getSize(outputStack), ItemStackUtils.getSize(storage.getPrimaryOutput())) / (double) ItemStackUtils.getSize(storage.getPrimaryOutput()));

        //Now check if we excede an ingredients max stack size.
        for (final ItemStorage ingredientStorage : storage.getCleanedInput())
        {
            final ItemStack ingredient = ingredientStorage.getItemStack();
            //Calculate the input count for the ingredient.
            final int ingredientInputCount = ItemStackUtils.getSize(ingredient) * craftingCount;
            //Check if we are above the max stacksize.
            if (ingredientInputCount > ingredient.getMaxStackSize())
            {
                //Recalculate the crafting limit using the maxstacksize of the ingredient.
                craftingCount = Math.max(ingredient.getMaxStackSize(), ItemStackUtils.getSize(storage.getPrimaryOutput())) / ItemStackUtils.getSize(storage.getPrimaryOutput());
            }
        }

        return craftingCount;
    }

    /**
     * Calculate the max time a recipe has to be executed.
     *
     * @param count   the count.
     * @param storage the storage.
     * @return the quantity.
     */
    public static int calculateMaxCraftingCount(@NotNull final int count, @NotNull final IRecipeStorage storage)
    {
        //Calculate the crafting count from the request and the storage output.
        return (int) Math.ceil(Math.max(count, ItemStackUtils.getSize(storage.getPrimaryOutput())) / (double) ItemStackUtils.getSize(storage.getPrimaryOutput()));
    }

    /**
     * Generates an {@link OptionalPredicate} that reports whether a particular
     * stack is allowed, banned, or undecided when used as a crafting product
     * by the specified job, based on the associated tags.
     *
     * @param crafterJobName The name of the crafting job (defines which tags to check).
     * @return a validator that checks the crafting product tags.
     */
    public static OptionalPredicate<ItemStack> getProductValidatorBasedOnTags(@NotNull final String crafterJobName)
    {
        return stack ->
        {
            // Check against excluded products
            final ITag<Item> excludedProducts = ModTags.crafterProductExclusions.get(crafterJobName);
            if (excludedProducts != null && excludedProducts.contains(stack.getItem()))
            {
                return Optional.of(false);
            }

            // Check against allowed products
            final ITag<Item> allowedProducts = ModTags.crafterProduct.get(crafterJobName);
            if (allowedProducts != null && allowedProducts.contains(stack.getItem()))
            {
                return Optional.of(true);
            }

            return Optional.empty();
        };
    }

    /**
     * Generates an {@link OptionalPredicate} that reports whether a particular
     * stack is allowed, banned, or undecided when used as a crafting ingredient
     * by the specified job, based on the associated tags.
     *
     * @param crafterJobName The name of the crafting job (defines which tags to check).
     * @return a validator that checks the crafting ingredient tags.
     */
    public static OptionalPredicate<ItemStack> getIngredientValidatorBasedOnTags(@NotNull final String crafterJobName)
    {
        return stack ->
        {
            // Check against excluded ingredients
            final ITag<Item> excludedIngredients = ModTags.crafterIngredientExclusions.get(crafterJobName);
            if (excludedIngredients != null && excludedIngredients.contains(stack.getItem()))
            {
                return Optional.of(false);
            }

            // Check against allowed ingredients
            final ITag<Item> allowedIngredients = ModTags.crafterIngredient.get(crafterJobName);
            if (allowedIngredients != null && allowedIngredients.contains(stack.getItem()))
            {
                return Optional.of(true);
            }

            return Optional.empty();
        };
    }

    /**
     * Checks the tags associated with the specified job to see whether
     * the provided recipe includes products and/or ingredients that are
     * marked as compatible or incompatible with this job.
     *
     * @param recipe The recipe to check.
     * @param crafterJobName The name of the crafting job (defines which tags to check).
     * @return True if the recipe is compatible, false if not compatible,
     *         or empty if no decision either way.
     */
    public static Optional<Boolean> isRecipeCompatibleBasedOnTags(@NotNull final IGenericRecipe recipe, @NotNull final String crafterJobName)
    {
        return OptionalPredicate.combine(recipe.matchesOutput(getProductValidatorBasedOnTags(crafterJobName)),
                () -> recipe.matchesInput(getIngredientValidatorBasedOnTags(crafterJobName)));
    }

}
