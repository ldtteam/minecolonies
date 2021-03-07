package com.minecolonies.api.crafting;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * This is a generic recipe wrapper, used to abstract both potential
 * IRecipes (with multiple alternative ingredient sets) and concrete
 * IRecipeStorages (with one specific ingredient set selected by the
 * player).  It is primarily used to make recipe validity checks, but
 * not for actual crafting.
 */
public interface IGenericRecipe
{
    /**
     * Gets the required size of the crafting grid, in number of slots.
     * If this is more than 4, then you can't craft this in a 2x2 grid.
     *
     * @return the number of slots required to craft this recipe.
     */
    int getGridSize();

    /**
     * Gets the primary output item of this recipe.
     *
     * @return The primary output item.
     */
    @NotNull
    ItemStack getPrimaryOutput();

    /**
     * Gets the primary output plus all alternative outputs of a multi-output recipe.
     *
     * @return A list of the possible outputs of this recipe.
     */
    @NotNull
    List<ItemStack> getAllMultiOutputs();

    /**
     * Gets the secondary outputs of this recipe.
     *
     * @return A list of the secondary outputs (additional, not alternative).
     */
    @NotNull
    List<ItemStack> getAdditionalOutputs();

    /**
     * Gets the input ingredient lists of this recipe.
     *
     * @return The list of slots, each of which is a list of alternatives.  Note that some recipes may have been
     *         "compacted" with a count of multiple items required in one slot, so you can't ignore that.
     */
    @NotNull
    List<List<ItemStack>> getInputs();

    /**
     * Checks whether the given predicate matches the output item of this recipe.
     *
     * @param predicate The predicate to test.
     * @return True if the output of this recipe matches the predicate;
     *         false otherwise.
     */
    boolean matchesOutput(@NotNull Predicate<ItemStack> predicate);

    /**
     * Checks whether the given predicate matches any input item of this recipe.
     *
     * @param predicate The predicate to test.
     * @return True if any input (including alternates) matches the predicate;
     *         false otherwise.
     */
    boolean matchesInput(@NotNull Predicate<ItemStack> predicate);

    /**
     * Gets the 'intermediate' block required for crafting, in the same sense
     * as IRecipeStorage.  Usually either AIR or FURNACE, but may be something else.
     *
     * @return The intermediate crafting block.
     */
    @NotNull
    Block getIntermediate();

    /**
     * Gets the identifier of the associated loot table, if any.
     *
     * @return The loot table identifier, or null.
     */
    @Nullable
    ResourceLocation getLootTable();

    /**
     * Gets some human-readable restrictions on when this recipe is valid.
     *
     * @return A list of restrictions.
     */
    @NotNull
    List<ITextComponent> getRestrictions();
}
