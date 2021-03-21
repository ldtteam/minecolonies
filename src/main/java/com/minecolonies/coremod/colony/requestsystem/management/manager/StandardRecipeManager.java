package com.minecolonies.coremod.colony.requestsystem.management.manager;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeManager;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StandardRecipeManager implements IRecipeManager
{
    /**
     * The Tag to store the recipes to NBT.
     */
    private static final String TAG_RECIPES = "recipes";

    /**
     * Map of all recipes which have been discovered globally already.
     */
    private final BiMap<IToken<?>, IRecipeStorage> recipes = HashBiMap.create(4096);

    /**
     * Immutable cache.
     */
    private ImmutableMap<IToken<?>, IRecipeStorage> cache = null;


    /**
     * The list of recipes marked as used this session
     */
    private final Set<IToken<?>> usedRecipes = new HashSet<>();

    @Override
    public ImmutableMap<IToken<?>, IRecipeStorage> getRecipes()
    {
        if (cache == null)
        {
            cache = ImmutableMap.copyOf(recipes);
        }
        return cache;
    }

    @Override
    public IToken<?> addRecipe(final IRecipeStorage storage)
    {
        recipes.put(storage.getToken(), storage);
        registerUse(storage.getToken());
        cache = null;
        return storage.getToken();
    }

    @Override
    public IToken<?> checkOrAddRecipe(final IRecipeStorage storage)
    {
        final IToken<?> token = getRecipeId(storage);
        if (token == null)
        {
            return addRecipe(storage);
        }
        registerUse(token);
        return token;
    }

    @Override
    public IToken<?> getRecipeId(final IRecipeStorage storage)
    {
        IToken<?> candidate = recipes.inverse().get(storage);
        if (candidate == null)
        {
            for(Map.Entry<IToken<?>, IRecipeStorage> tokenEntry : recipes.entrySet())
            {
                if(tokenEntry.getValue().equals(storage))
                {
                    // This should never get hit. But it does. 
                    Log.getLogger().info("Found the hard way: " + storage.getPrimaryOutput().getDisplayName().getString());
                    Log.getLogger().info("Hashes: " + tokenEntry.getValue().hashCode() + " " + storage.hashCode());
                    candidate = tokenEntry.getKey();
                    break;
                }
            }
        }
        return candidate;
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        @NotNull final ListNBT recipesTagList =
          recipes.entrySet().stream().filter(recipeEntry -> usedRecipes.contains(recipeEntry.getKey())).map(entry -> StandardFactoryController.getInstance().serialize(entry.getValue())).collect(NBTUtils.toListNBT());
        compound.put(TAG_RECIPES, recipesTagList);
    }

    @Override
    public void read(@NotNull final CompoundNBT compound)
    {
        final ListNBT list = compound.getList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            IRecipeStorage recipe = StandardFactoryController.getInstance().deserialize(list.getCompound(i));
            if (recipe != null && !recipes.containsKey(recipe.getToken()) && !recipes.containsValue(recipe))
            {
                recipes.put(recipe.getToken(), recipe);
            }
        }
        cache = null;
    }

    @Override
    public void reset()
    {
        recipes.clear();
        usedRecipes.clear();
    }

    @Override
    public void registerUse(final IToken<?> token)
    {
        usedRecipes.add(token);
    }
}
