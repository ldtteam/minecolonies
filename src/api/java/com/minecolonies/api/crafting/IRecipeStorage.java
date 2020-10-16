package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Interface which describes the RecipeStorage.
 */
public interface IRecipeStorage
{
    /**
     * Enum of the different types of storage
     */
    public enum RecipeStorageType
    {
        CLASSIC,
        MULTI_OUTPUT
    }

    /**
     * Get the list of input items. Suppressing Sonar Rule Squid:S2384 The rule thinks we should return a copy of the list and not the list itself. But in this case the rule does
     * not apply because the list is an unmodifiable list already
     *
     * @return the list.
     */
    List<ItemStack> getInput();

    /**
     * Get the cleaned up list of the recipes. Air gets removed and equal items get put together.
     * This returns an list of immutable itemStorage elements that cannot be tempered with.
     * @return the list.
     */
    List<ItemStorage> getCleanedInput();

    /**
     * Getter for the primary output.
     *
     * @return the itemStack to be produced.
     */
    ItemStack getPrimaryOutput();

    /**
     * Get the grid size.
     *
     * @return the integer representing it. (2x2 = 4, 3x3 = 9, etc)
     */
    int getGridSize();

    /**
     * Get the required intermediate for the recipe.
     *
     * @return the block.
     */
    Block getIntermediate();

    /**
     * Method to check if with the help of inventories this recipe can be fullfilled.
     *
     * @param qty         the quantity to craft.
     * @param inventories the inventories to check.
     * @return true if possible, else false.
     */
    boolean canFullFillRecipe(final int qty, @NotNull final IItemHandler... inventories);

    default boolean fullFillRecipe(@NotNull final IItemHandler... inventories)
    {
        return fullfillRecipe(Arrays.asList(inventories));
    }

    /**
     * Check for space, remove items, and insert crafted items.
     *
     * @param handlers the handlers to use.
     * @return true if succesful.
     */
    boolean fullfillRecipe(final List<IItemHandler> handlers);

    /**
     * Get which type this recipe is
     * CLASSIC or MULTI-OUTPUT are the only valid ones currently
     * @return The recipe type
     */
    public RecipeStorageType getRecipeType();

    /**
     * Get a list of alternates to getPrimaryOutput
     * @return a list if Itemstacks that this recipe can produce instead of getPrimaryOutput
     */
    public List<ItemStack> getAlternateOutputs();

    /**
     * Get the classic version of this recipe with GetPrimaryOutput targetted correctly from the chosen alternate
     * @param requiredOutput Which output wanted
     * @return the RecipeStorage that is "right" for that output
     */
    public RecipeStorage getClassicForMultiOutput(ItemStack requiredOutput);

    /**
     * Get the classic version of this recipe with GetPrimaryOutput targetted correctly from the chosen alternate
     * @param stackPredicate Predicate to select the right stack
     * @return the RecipeStorage that is "right" for that output
     */
    public RecipeStorage getClassicForMultiOutput(final Predicate<ItemStack> stackPredicate);

    /**
     * Source of the recipe, ie registry name.
     * @return
     */
    public ResourceLocation getRecipeSource();

    /**
     * Get the secondary (leave behind in grid) outputs
     * @return list of items that weren't consumed during crafting
     */
    public List<ItemStack> getSecondaryOutputs();

    /**
     * Get the unique token of the recipe.
     *
     * @return the IToken.
     */
    IToken<?> getToken();
}
