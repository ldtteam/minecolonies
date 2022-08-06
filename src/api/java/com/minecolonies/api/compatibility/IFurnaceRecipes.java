package com.minecolonies.api.compatibility;

import com.minecolonies.api.crafting.RecipeStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Interface for the new furnace recipes.
 */
public interface IFurnaceRecipes
{
    /**
     * Serialize to a network buffer for synching to the client.
     * @param buf the serialization buffer
     */
    void serialize(@NotNull final FriendlyByteBuf buf);

    /**
     * Deserialize from a network buffer for synching from the server.
     * @param buf the deserialization buffer
     */
    void deserialize(@NotNull final FriendlyByteBuf buf);

    /**
     * Get the smelting result for a certain itemStack.
     *
     * @param itemStack the itemStack to test.
     * @return the result or empty if not existent.
     */
    ItemStack getSmeltingResult(final ItemStack itemStack);

    /**
     * Get the first smelting recipe by result for a certain itemStack predicate.
     *
     * @param stackPredicate the predicate to test.
     * @return the result or null if not existent.
     */
    public RecipeStorage getFirstSmeltingRecipeByResult(final Predicate<ItemStack> stackPredicate);
}
