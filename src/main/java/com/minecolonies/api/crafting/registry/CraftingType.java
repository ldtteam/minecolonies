package com.minecolonies.api.crafting.registry;

import com.minecolonies.api.crafting.IGenericRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Class to represent the different types of crafting supported by MineColonies
 */
public abstract class CraftingType
{
    private Identifier registryName;

    protected CraftingType(@NotNull final Identifier id)
    {
        this.registryName = id;
    }

    /**
     * Find all teachable recipes supported by this particular crafting type
     * @param recipeManager the vanilla recipe manager
     * @param world the world (if available)
     * @return the list of teachable recipes
     */
    @NotNull
    public abstract List<IGenericRecipe> findRecipes(@NotNull final RecipeManager recipeManager,
                                                     @Nullable final Level world);

    /**
     * Find recipes from a client-side recipe snapshot.
     *
     * <p>Minecraft 26.2 no longer exposes the server's {@link RecipeManager}
     * on a client connection. Client integrations can still provide the
     * synchronized recipe holders, so recipe types that can use that
     * snapshot override this method.</p>
     *
     * @param recipeHolders recipe holders synchronized to the client
     * @param world the world (if available)
     * @return the list of teachable recipes
     */
    @NotNull
    public List<IGenericRecipe> findRecipes(@NotNull final Collection<RecipeHolder<?>> recipeHolders,
                                            @Nullable final Level world)
    {
        return List.of();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof CraftingType)
        {
            return Objects.equals(registryName, ((CraftingType) obj).registryName);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return registryName.hashCode();
    }
}
