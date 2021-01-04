package com.minecolonies.api.crafting;

import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Interface which describes the RecipeStorage.
 */
public interface IRecipeStorage
{
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
     * Also check if the inventory has enough to fulfill the existing requirements.
     *
     * @param qty         the quantity to craft.
     * @param existingRequirements map of existing requirements (pending requests).
     * @param inventories the inventories to check.
     * @return true if possible, else false.
     */
    boolean canFullFillRecipe(final int qty, final Map<ItemStorage, Integer> existingRequirements, @NotNull final IItemHandler... inventories);

    default boolean fullFillRecipe(@NotNull final World world, @NotNull final IItemHandler... inventories)
    {
        return fullfillRecipe(world, Arrays.asList(inventories));
    }

    /**
     * Check for space, remove items, and insert crafted items.
     *
     * @param handlers the handlers to use.
     * @return true if succesful.
     */
    boolean fullfillRecipe(final World world, final List<IItemHandler> handlers);

    /**
     * Get which type this recipe is
     * This type comes from the RecipeTypes registry
     * @return The recipe type
     */
    AbstractRecipeType<IRecipeStorage> getRecipeType();

    /**
     * Get a list of alternates to getPrimaryOutput
     * @return a list if Itemstacks that this recipe can produce instead of getPrimaryOutput
     */
    List<ItemStack> getAlternateOutputs();

    /**
     * Get the classic version of this recipe with GetPrimaryOutput targetted correctly from the chosen alternate
     * @param requiredOutput Which output wanted
     * @return the RecipeStorage that is "right" for that output
     */
    RecipeStorage getClassicForMultiOutput(ItemStack requiredOutput);

    /**
     * Get the classic version of this recipe with GetPrimaryOutput targetted correctly from the chosen alternate
     * @param stackPredicate Predicate to select the right stack
     * @return the RecipeStorage that is "right" for that output
     */
    RecipeStorage getClassicForMultiOutput(final Predicate<ItemStack> stackPredicate);

    /**
     * Source of the recipe, ie registry name.
     * @return
     */
    ResourceLocation getRecipeSource();

    /**
     * Get the secondary (leave behind in grid) outputs
     * @return list of items that weren't consumed during crafting
     */
    List<ItemStack> getSecondaryOutputs();

    /** 
     * Get the location/id of the Loot table used for optional outputs
     * @return the resource location for the table
    */
    ResourceLocation getLootTable();

    /**
     * Get the unique token of the recipe.
     *
     * @return the IToken.
     */
    IToken<?> getToken();
}
