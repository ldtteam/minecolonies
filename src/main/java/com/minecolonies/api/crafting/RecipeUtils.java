package com.minecolonies.api.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.util.context.ContextMap;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.Level;

/**
 * Helpers for Minecraft 26.2 recipe APIs.
 */
public final class RecipeUtils
{
    private RecipeUtils()
    {
        throw new UnsupportedOperationException("utility class");
    }

    /**
     * Builds a representative recipe output without requiring a populated input.
     * Vanilla output templates do not depend on the contents of that input.
     *
     * @param recipe recipe to inspect.
     * @return representative output, or an empty stack when the recipe type is unsupported.
     */
    @NotNull
    public static ItemStack getOutput(@NotNull final Recipe<?> recipe)
    {
        return getOutput(recipe, null);
    }

    /**
     * Builds a representative recipe output using the registry context from a
     * level when a display requires it (for example potion displays).
     *
     * @param recipe recipe to inspect.
     * @param level level whose registry context should resolve the display.
     * @return representative output, or an empty stack when the recipe type is unsupported.
     */
    @NotNull
    public static ItemStack getOutput(@NotNull final Recipe<?> recipe, @Nullable final Level level)
    {
        // Minecraft 26.2 recipes expose display output templates even when
        // their assemble method requires a fully populated input (for
        // example ImbueRecipe). Prefer that data so projections do not need
        // to manufacture an invalid crafting grid.
        final ContextMap context = level == null
                ? new ContextMap.Builder().create(SlotDisplayContext.CONTEXT)
                : SlotDisplayContext.fromLevel(level);
        for (final RecipeDisplay display : recipe.display())
        {
            final ItemStack output = display.result().resolveForFirstStack(context);
            if (!output.isEmpty())
            {
                return output;
            }
        }

        if (recipe instanceof final SingleItemRecipe singleItemRecipe)
        {
            return singleItemRecipe.assemble(new SingleRecipeInput(ItemStack.EMPTY));
        }

        if (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)
        {
            return ((CraftingRecipe) recipe).assemble(CraftingInput.EMPTY);
        }

        return ItemStack.EMPTY;
    }

    /**
     * Gets recipe inputs without relying on the removed generic {@code Recipe#getIngredients} method.
     *
     * @param recipe recipe to inspect.
     * @return the known inputs.
     */
    @NotNull
    public static List<Ingredient> getIngredients(@NotNull final Recipe<?> recipe)
    {
        if (recipe instanceof final ShapedRecipe shapedRecipe)
        {
            return shapedRecipe.getIngredients().stream().flatMap(Optional::stream).toList();
        }
        if (recipe instanceof final ShapelessRecipe shapelessRecipe)
        {
            return shapelessRecipe.placementInfo().ingredients();
        }
        if (recipe instanceof final SingleItemRecipe singleItemRecipe)
        {
            return List.of(singleItemRecipe.input());
        }
        return recipe.placementInfo().ingredients();
    }

    /**
     * Returns JEI's client-side recipe snapshot when it is available.
     *
     * <p>Minecraft 26.2 no longer exposes a {@code RecipeManager} from a
     * client connection. JEI still keeps the complete synchronized recipe
     * map for its vanilla and mod integrations, so client-only MineColonies
     * screens can use it without linking against JEI's implementation jar.
     * Reflection keeps the core/common code loadable when JEI is absent.</p>
     *
     * @return the synchronized client recipe map, or {@code null} when JEI
     *         is not loaded or its snapshot is not ready.
     */
    @Nullable
    public static RecipeMap clientSyncedRecipes()
    {
        try
        {
            final Class<?> internal = Class.forName("mezz.jei.common.Internal");
            if (!(internal.getMethod("hasClientSyncedRecipes").invoke(null) instanceof final Boolean available) || !available)
            {
                return null;
            }
            final Object recipes = internal.getMethod("getClientSyncedRecipes").invoke(null);
            return recipes instanceof final RecipeMap recipeMap ? recipeMap : null;
        }
        catch (final ReflectiveOperationException ignored)
        {
            return null;
        }
    }
}
