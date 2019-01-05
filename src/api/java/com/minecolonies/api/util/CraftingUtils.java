package com.minecolonies.api.util;

import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
     * @param outputStack the output stack.
     * @param storage the storage.
     * @return the quantity.
     */
    public static int calculateMaxCraftingCount(@NotNull final ItemStack outputStack, @NotNull final IRecipeStorage storage)
    {
        //Calculate the initial crafting count from the request and the storage output.
        int craftingCount = (int)Math.ceil(Math.max(ItemStackUtils.getSize(outputStack), ItemStackUtils.getSize(storage.getPrimaryOutput())) / (double)ItemStackUtils.getSize(storage.getPrimaryOutput()));

        //Now check if we excede an ingredients max stack size.
        for(final ItemStorage ingredientStorage : storage.getCleanedInput())
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
     * @param count the count.
     * @param storage the storage.
     * @return the quantity.
     */
    public static int calculateMaxCraftingCount(@NotNull final int count, @NotNull final IRecipeStorage storage)
    {
        //Calculate the initial crafting count from the request and the storage output.
        int craftingCount = (int)Math.ceil(Math.max(count, ItemStackUtils.getSize(storage.getPrimaryOutput())) / (double)ItemStackUtils.getSize(storage.getPrimaryOutput()));

        //Now check if we excede an ingredients max stack size.
        for(final ItemStorage ingredientStorage : storage.getCleanedInput())
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
}
