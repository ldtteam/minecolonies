package com.minecolonies.coremod.colony.requestsystem.management.manager;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeManager;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

    @Override
    public ImmutableMap<IToken, IRecipeStorage> getRecipes()
    {
        return ImmutableMap.copyOf(recipes);
    }

    @Override
    public IToken addRecipe(final IRecipeStorage storage)
    {
        recipes.put(storage.getToken(), storage);
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
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final NBTTagList recipesTagList =
                recipes.entrySet().stream().map(entry ->  StandardFactoryController.getInstance().serialize(entry.getValue())).collect(NBTUtils.toNBTTagList());
        compound.setTag(TAG_RECIPES, recipesTagList);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        recipes.putAll(NBTUtils.streamCompound(compound.getTagList(TAG_RECIPES, Constants.NBT.TAG_COMPOUND))
                .map(recipeCompound -> (IRecipeStorage) StandardFactoryController.getInstance().deserialize(recipeCompound))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(IRecipeStorage::getToken, recipe -> recipe)));
    }
}
