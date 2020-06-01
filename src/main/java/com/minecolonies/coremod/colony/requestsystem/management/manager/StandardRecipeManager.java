package com.minecolonies.coremod.colony.requestsystem.management.manager;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeManager;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class StandardRecipeManager implements IRecipeManager
{
    /**
     * The Tag to store the recipes to NBT.
     */
    private static final String TAG_RECIPES = "recipes";

    /**
     * Map of all recipes which have been discovered globally already.
     */
    private final BiMap<IToken, IRecipeStorage> recipes = HashBiMap.create();

    /**
     * Immutable cache.
     */
    private ImmutableMap<IToken, IRecipeStorage> cache = null;

    @Override
    public ImmutableMap<IToken, IRecipeStorage> getRecipes()
    {
         if (cache == null)
        {
            cache = ImmutableMap.copyOf(recipes);
        }
        return cache;
    }

    @Override
    public IToken addRecipe(final IRecipeStorage storage)
    {
        recipes.put(storage.getToken(), storage);
        cache = null;
        return storage.getToken();
    }

    @Override
    public IToken checkOrAddRecipe(final IRecipeStorage storage)
    {
        final IToken token = getRecipeId(storage);
        if(token == null)
        {
            return addRecipe(storage);
        }
        return token;
    }

    @Override
    public IToken getRecipeId(final IRecipeStorage storage)
    {
        for(final Map.Entry<IToken, IRecipeStorage> tempStorage: recipes.entrySet())
        {
            if(tempStorage.getValue().equals(storage))
            {
                return tempStorage.getKey();
            }
        }
        return null;
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        @NotNull final ListNBT recipesTagList =
                recipes.entrySet().stream().map(entry ->  StandardFactoryController.getInstance().serialize(entry.getValue())).collect(NBTUtils.toListNBT());
        compound.put(TAG_RECIPES, recipesTagList);
    }

    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        recipes.putAll(NBTUtils.streamCompound(compound.getList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND))
                .map(recipeCompound -> (IRecipeStorage) StandardFactoryController.getInstance().deserialize(recipeCompound))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(IRecipeStorage::getToken, recipe -> recipe)));
        cache = null;
    }
}
